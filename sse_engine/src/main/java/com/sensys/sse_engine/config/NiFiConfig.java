package com.sensys.sse_engine.config;

import com.sensys.sse_engine.exception.TokenRefreshedException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import javax.net.ssl.SSLException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(NiFiProperties.class)
public class NiFiConfig {
    
    private final NiFiProperties nifiProperties;
    private final Jackson2JsonDecoder jsonDecoder;
    private final Jackson2JsonEncoder jsonEncoder;
    
    private final AtomicReference<String> bearerToken = new AtomicReference<>();

    @Bean
    public WebClient nifiWebClient() throws SSLException {
        HttpClient httpClient = createHttpClient();

        WebClient tokenClient = createBaseWebClient(httpClient);

        // Get initial token
        String token = fetchAccessToken(tokenClient)
            .retryWhen(Retry.backoff(nifiProperties.getRetryAttempts(), 
                Duration.ofMillis(nifiProperties.getRetryDelay())))
            .block(Duration.ofSeconds(nifiProperties.getTimeout()));
        bearerToken.set(token);

        return createBaseWebClient(httpClient)
            .mutate()
            .filter(addAuthorizationHeader())
            .filter(handleUnauthorized(tokenClient))
            .filter(ExchangeFilterFunction.ofRequestProcessor(
                clientRequest -> {
                    log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
                    return Mono.just(clientRequest);
                }
            ))
            .build();
    }

    private WebClient createBaseWebClient(HttpClient httpClient) {
        return WebClient.builder()
            .baseUrl(nifiProperties.getUrl())
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .codecs(configurer -> {
                configurer.defaultCodecs().jackson2JsonDecoder(jsonDecoder);
                configurer.defaultCodecs().jackson2JsonEncoder(jsonEncoder);
            })
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    private HttpClient createHttpClient() throws SSLException {
        if (nifiProperties.getUrl().toLowerCase().startsWith("https")) {
            log.info("Configuring SSL WebClient");
            SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
            
            return HttpClient.create()
                .secure(t -> t.sslContext(sslContext))
                .responseTimeout(Duration.ofSeconds(nifiProperties.getTimeout()));
        } else {
            log.info("Configuring non-SSL WebClient");
            return HttpClient.create()
                .responseTimeout(Duration.ofSeconds(nifiProperties.getTimeout()));
        }
    }

    private Mono<String> fetchAccessToken(WebClient client) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", nifiProperties.getUsername());
        formData.add("password", nifiProperties.getPassword());

        return client.post()
            .uri("/nifi-api/access/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .bodyToMono(String.class)
            .doOnSuccess(token -> log.debug("Successfully obtained access token"))
            .doOnError(error -> log.error("Error obtaining access token: {}", error.getMessage()));
    }

    private ExchangeFilterFunction addAuthorizationHeader() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            String token = bearerToken.get();
            if (token != null) {
                return Mono.just(ClientRequest.from(clientRequest)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build());
            }
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction handleUnauthorized(WebClient tokenClient) {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().value() == 401) {
                log.debug("Received 401, refreshing token");
                return fetchAccessToken(tokenClient)
                    .doOnNext(bearerToken::set)
                    .flatMap(token -> clientResponse.releaseBody()
                        .then(Mono.error(new TokenRefreshedException())));
            }
            return Mono.just(clientResponse);
        });
    }
}
package com.sensys.sse_engine.services;

import com.sensys.sse_engine.exception.NiFiClientException;
import com.sensys.sse_engine.exception.NiFiServerException;
import lombok.extern.slf4j.Slf4j;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.ProcessorEntity;
import org.apache.nifi.web.api.entity.ScheduleComponentsEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class NiFiService {
    private final WebClient nifiWebClient;

    public enum ComponentState {
        RUNNING, STOPPED
    }

    public NiFiService(WebClient nifiWebClient) {
        this.nifiWebClient = nifiWebClient;
    }

    public Mono<ProcessGroupEntity> getProcessGroup(String processGroupId) {
        log.debug("Getting process group with ID: {}", processGroupId);
        return nifiWebClient.get()
                .uri("/nifi-api/process-groups/{id}", processGroupId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), 
                    response -> handleClientError(response, "Failed to get process group: " + processGroupId))
                .onStatus(status -> status.is5xxServerError(), 
                    response -> handleServerError(response, "NiFi server error while getting process group"))
                .bodyToMono(ProcessGroupEntity.class)
                .doOnError(error -> log.error("Error getting process group {}: {}", 
                    processGroupId, error.getMessage()))
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                    .filter(this::shouldRetry));
    }

    public Mono<ProcessGroupEntity> startProcessGroup(String processGroupId) {
        log.debug("Starting process group with ID: {}", processGroupId);
        return updateProcessGroupState(processGroupId, ComponentState.RUNNING);
    }

    public Mono<ProcessGroupEntity> stopProcessGroup(String processGroupId) {
        log.debug("Stopping process group with ID: {}", processGroupId);
        return updateProcessGroupState(processGroupId, ComponentState.STOPPED);
    }

    private Mono<ProcessGroupEntity> updateProcessGroupState(String processGroupId, ComponentState state) {
        ScheduleComponentsEntity scheduleComponents = new ScheduleComponentsEntity();
        scheduleComponents.setState(state.toString());
        scheduleComponents.setId(processGroupId);

        return nifiWebClient.put()
                .uri("/nifi-api/process-groups/{id}/run-status", processGroupId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(scheduleComponents), ScheduleComponentsEntity.class)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), 
                    response -> handleClientError(response, "Failed to update process group state: " + processGroupId))
                .onStatus(status -> status.is5xxServerError(), 
                    response -> handleServerError(response, "NiFi server error while updating process group state"))
                .bodyToMono(ProcessGroupEntity.class)
                .doOnError(error -> log.error("Error updating process group {} state to {}: {}", 
                    processGroupId, state, error.getMessage()))
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                    .filter(this::shouldRetry));
    }

    public Mono<ProcessorEntity> getProcessor(String processorId) {
        log.debug("Getting processor with ID: {}", processorId);
        return nifiWebClient.get()
                .uri("/nifi-api/processors/{id}", processorId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), 
                    response -> handleClientError(response, "Failed to get processor: " + processorId))
                .onStatus(status -> status.is5xxServerError(), 
                    response -> handleServerError(response, "NiFi server error while getting processor"))
                .bodyToMono(ProcessorEntity.class)
                .doOnError(error -> log.error("Error getting processor {}: {}", 
                    processorId, error.getMessage()))
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                    .filter(this::shouldRetry));
    }

    private Mono<? extends Throwable> handleClientError(ClientResponse response, String message) {
        return response.bodyToMono(String.class)
                .flatMap(errorBody -> Mono.error(new NiFiClientException(
                        message + " Status: " + response.statusCode() + " Body: " + errorBody)));
    }

    private Mono<? extends Throwable> handleServerError(ClientResponse response, String message) {
        return response.bodyToMono(String.class)
                .flatMap(errorBody -> Mono.error(new NiFiServerException(
                        message + " Status: " + response.statusCode() + " Body: " + errorBody)));
    }

    private boolean shouldRetry(Throwable throwable) {
        return throwable instanceof TimeoutException ||
                throwable instanceof NiFiServerException ||
                (throwable instanceof NiFiClientException &&
                        ((NiFiClientException) throwable).getStatusCode() == HttpStatus.TOO_MANY_REQUESTS);
    }
}
package com.sensys.sse_engine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NiFiOperationsService {
    
    private final WebClient nifiWebClient;

    /**
     * Start a process group
     *
     * @param processGroupId ID of the process group to start
     * @return Mono<Void> indicating success or error
     */
    public Mono<Void> startProcessGroup(String processGroupId) {
        log.debug("Starting process group with ID: {}", processGroupId);
        
        return nifiWebClient.put()
            .uri("/nifi-api/flow/process-groups/{id}", processGroupId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of(
                "id", processGroupId,
                "state", "RUNNING",
                "disconnectedNodeAcknowledged", false
            ))
            .retrieve()
            .bodyToMono(Void.class)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
            .doOnSuccess(result -> log.debug("Successfully started process group: {}", processGroupId))
            .doOnError(error -> log.error("Failed to start process group {}: {}", 
                processGroupId, error.getMessage()));
    }

    /**
     * Stop a process group
     *
     * @param processGroupId ID of the process group to stop
     * @return Mono<Void> indicating success or error
     */
    public Mono<Void> stopProcessGroup(String processGroupId) {
        log.debug("Stopping process group with ID: {}", processGroupId);
        
        return nifiWebClient.put()
            .uri("/nifi-api/flow/process-groups/{id}", processGroupId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of(
                "id", processGroupId,
                "state", "STOPPED",
                "disconnectedNodeAcknowledged", false
            ))
            .retrieve()
            .bodyToMono(Void.class)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
            .doOnSuccess(result -> log.debug("Successfully stopped process group: {}", processGroupId))
            .doOnError(error -> log.error("Failed to stop process group {}: {}", 
                processGroupId, error.getMessage()));
    }
}
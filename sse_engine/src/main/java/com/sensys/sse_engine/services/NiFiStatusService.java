package com.sensys.sse_engine.services;

import com.sensys.sse_engine.dto.ProcessGroupStatus;
import com.sensys.sse_engine.dto.StatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class NiFiStatusService {
    
    private final WebClient nifiWebClient;

    /**
     * Get detailed status for a process group
     *
     * @param processGroupId ID of the process group
     * @return Mono containing process group status
     */
    public Mono<ProcessGroupStatus> getProcessGroupStatus(String processGroupId) {
        return nifiWebClient.get()
            .uri("/nifi-api/flow/process-groups/{id}/status", processGroupId)
            .retrieve()
            .bodyToMono(StatusResponse.class)
            .map(StatusResponse::getProcessGroupStatus)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
            .doOnError(error -> log.error("Failed to fetch status for process group {}: {}", 
                processGroupId, error.getMessage()))
            .doOnSuccess(status -> {
                if (status != null) {
                    log.debug("Successfully fetched status for process group: {} ({})", 
                        status.getName(), processGroupId);
                }
            });
    }

    /**
     * Stream status updates for a process group
     *
     * @param processGroupId ID of the process group
     * @param interval Polling interval in seconds
     * @return Flux of process group status updates
     */
    public Flux<ProcessGroupStatus> streamProcessGroupStatus(String processGroupId, int interval) {
        return Flux.interval(Duration.ZERO, Duration.ofSeconds(interval))
            .flatMap(tick -> getProcessGroupStatus(processGroupId))
            .doOnSubscribe(subscription -> 
                log.debug("Starting status stream for process group: {}", processGroupId))
            .doOnCancel(() -> 
                log.debug("Status stream cancelled for process group: {}", processGroupId))
            .doOnComplete(() -> 
                log.debug("Status stream completed for process group: {}", processGroupId));
    }
}
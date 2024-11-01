package com.sensys.sse_engine.services;

import com.sensys.sse_engine.dto.NiFiResource;
import com.sensys.sse_engine.dto.NiFiResourcesDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class NiFiResourceService {
    
    private final WebClient nifiWebClient;
    
    /**
     * Retrieves all NiFi resources from the API
     *
     * @return Mono containing NiFiResourcesDTO with all resources
     */
    public Mono<NiFiResourcesDTO> getAllResources() {
        return nifiWebClient.get()
            .uri("/nifi-api/resources")
            .retrieve()
            .bodyToMono(NiFiResourcesDTO.class)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
            .onErrorResume(error -> {
                log.error("Failed to fetch NiFi resources: {}", error.getMessage());
                return Mono.empty();
            })
            .doOnSuccess(resources -> {
                if (resources != null) {
                    log.debug("Successfully fetched {} NiFi resources", 
                        resources.getResources().size());
                }
            });
    }

    /**
     * Gets all process groups
     *
     * @return Flux of process group resources
     */
    public Flux<NiFiResource> getProcessGroups() {
        return getAllResources()
            .mapNotNull(dto -> dto.getProcessGroups())
            .flatMapMany(Flux::fromIterable)
            .filter(Objects::nonNull)
            .doOnComplete(() -> log.debug("Completed retrieving process groups"));
    }

    /**
     * Gets all processors
     *
     * @return Flux of processor resources
     */
    public Flux<NiFiResource> getProcessors() {
        return getAllResources()
            .mapNotNull(dto -> dto.getProcessors())
            .flatMapMany(Flux::fromIterable)
            .filter(Objects::nonNull)
            .doOnComplete(() -> log.debug("Completed retrieving processors"));
    }

    /**
     * Finds a process group by its name
     *
     * @param name Name of the process group to find
     * @return Mono containing the matching process group resource, if found
     */
    public Mono<NiFiResource> findProcessGroupByName(String name) {
        if (name == null) {
            log.debug("Process group name is null");
            return Mono.empty();
        }

        return getAllResources()
            .mapNotNull(dto -> dto.findProcessGroupByName(name))
            .doOnNext(pg -> {
                if (pg != null) {
                    log.debug("Found process group: {} with id: {}", name, pg.getId());
                } else {
                    log.debug("Process group not found: {}", name);
                }
            });
    }

    /**
     * Finds a processor by its name
     *
     * @param name Name of the processor to find
     * @return Mono containing the matching processor resource, if found
     */
    public Mono<NiFiResource> findProcessorByName(String name) {
        if (name == null) {
            log.debug("Processor name is null");
            return Mono.empty();
        }

        return getAllResources()
            .mapNotNull(dto -> dto.findProcessorByName(name))
            .doOnNext(processor -> {
                if (processor != null) {
                    log.debug("Found processor: {} with id: {}", name, processor.getId());
                } else {
                    log.debug("Processor not found: {}", name);
                }
            });
    }

    /**
     * Finds a process group by its ID
     *
     * @param id ID of the process group to find
     * @return Mono containing the matching process group resource, if found
     */
    public Mono<NiFiResource> findProcessGroupById(String id) {
        if (id == null) {
            log.debug("Process group ID is null");
            return Mono.empty();
        }

        return getAllResources()
            .mapNotNull(dto -> dto.findResourceById(id))
            .filter(NiFiResource::isProcessGroup)
            .doOnNext(pg -> {
                if (pg != null) {
                    log.debug("Found process group with id: {}", id);
                } else {
                    log.debug("Process group not found with id: {}", id);
                }
            });
    }

    /**
     * Gets all resources within a specific process group
     *
     * @param processGroupId ID of the process group
     * @return Flux of resources within the specified process group
     */
    public Flux<NiFiResource> getResourcesInProcessGroup(String processGroupId) {
        if (processGroupId == null) {
            log.debug("Process group ID is null");
            return Flux.empty();
        }

        return getAllResources()
            .mapNotNull(dto -> dto.getResources())
            .flatMapMany(resources -> Flux.fromIterable(resources)
                .filter(Objects::nonNull)
                .filter(resource -> {
                    String identifier = resource.getIdentifier();
                    return identifier != null && 
                           identifier.contains("/process-groups/" + processGroupId + "/");
                }))
            .doOnComplete(() -> log.debug("Completed retrieving resources in process group: {}", 
                processGroupId));
    }
}
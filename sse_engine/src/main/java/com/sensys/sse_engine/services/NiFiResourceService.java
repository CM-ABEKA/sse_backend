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
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NiFiResourceService {
    
    private final WebClient nifiWebClient;
    
    public Mono<NiFiResourcesDTO> getAllResources() {
        return nifiWebClient.get()
            .uri("/nifi-api/resources")
            .retrieve()
            .bodyToMono(NiFiResourcesDTO.class)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
            .doOnSuccess(resources -> log.debug("Successfully fetched {} NiFi resources", 
                resources.getResources().size()))
            .doOnError(error -> log.error("Error fetching NiFi resources: {}", 
                error.getMessage()));
    }

    public Flux<NiFiResource> getProcessGroups() {
        return getAllResources()
            .mapNotNull(dto -> dto.getProcessGroups())
            .flatMapMany(Flux::fromIterable)
            .doOnComplete(() -> log.debug("Finished retrieving process groups"));
    }

    public Flux<NiFiResource> getProcessors() {
        return getAllResources()
            .mapNotNull(dto -> dto.getProcessors())
            .flatMapMany(Flux::fromIterable)
            .doOnComplete(() -> log.debug("Finished retrieving processors"));
    }

    public Mono<NiFiResource> findProcessGroupByName(String name) {
        if (name == null) {
            return Mono.empty();
        }
        return getAllResources()
            .mapNotNull(dto -> dto.findProcessGroupByName(name))
            .doOnSuccess(pg -> {
                if (pg != null) {
                    log.debug("Found process group: {} with id: {}", name, pg.getIdentifier());
                } else {
                    log.debug("Process group not found: {}", name);
                }
            });
    }

    public Mono<NiFiResource> findProcessorByName(String name) {
        if (name == null) {
            return Mono.empty();
        }
        return getAllResources()
            .mapNotNull(dto -> dto.findProcessorByName(name))
            .doOnSuccess(processor -> {
                if (processor != null) {
                    log.debug("Found processor: {} with id: {}", name, processor.getIdentifier());
                } else {
                    log.debug("Processor not found: {}", name);
                }
            });
    }

    public Mono<NiFiResource> findProcessGroupById(String id) {
        if (id == null) {
            return Mono.empty();
        }
        return getProcessGroups()
            .filter(pg -> pg.extractId().equals(id))
            .next()
            .doOnSuccess(pg -> {
                if (pg != null) {
                    log.debug("Found process group with id: {}", id);
                } else {
                    log.debug("Process group not found with id: {}", id);
                }
            });
    }

    public Mono<NiFiResource> findResourceByIdentifier(String identifier) {
        if (identifier == null) {
            return Mono.empty();
        }
        return getAllResources()
            .mapNotNull(dto -> dto.findResourceByIdentifier(identifier))
            .doOnSuccess(resource -> {
                if (resource != null) {
                    log.debug("Found resource: {} with identifier: {}", 
                        resource.getName(), identifier);
                } else {
                    log.debug("Resource not found with identifier: {}", identifier);
                }
            });
    }

    public Flux<NiFiResource> getResourcesInProcessGroup(String processGroupId) {
        if (processGroupId == null) {
            return Flux.empty();
        }
        return getAllResources()
            .mapNotNull(dto -> dto.getResourcesByProcessGroup(processGroupId))
            .flatMapMany(Flux::fromIterable)
            .doOnComplete(() -> log.debug("Finished retrieving resources in process group: {}", 
                processGroupId));
    }
}
package com.sensys.sse_engine.controller;

import com.sensys.sse_engine.dto.NiFiResource;
import com.sensys.sse_engine.dto.NiFiResourcesDTO;
import com.sensys.sse_engine.services.NiFiResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/nifi/resources")
@RequiredArgsConstructor
@Slf4j
public class NiFiResourceController {

    private final NiFiResourceService nifiResourceService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<NiFiResourcesDTO> getAllResources() {
        return nifiResourceService.getAllResources();
    }

    @GetMapping(path = "/process-groups", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<NiFiResource> getProcessGroups() {
        return nifiResourceService.getProcessGroups();
    }

    @GetMapping(path = "/processors", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<NiFiResource> getProcessors() {
        return nifiResourceService.getProcessors();
    }

    @GetMapping(path = "/process-groups/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<NiFiResource> findProcessGroupByName(@RequestParam String name) {
        return nifiResourceService.findProcessGroupByName(name);
    }

    @GetMapping(path = "/processors/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<NiFiResource> findProcessorByName(@RequestParam String name) {
        return nifiResourceService.findProcessorByName(name);
    }

    @GetMapping(path = "/process-groups/{processGroupId}/resources", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<NiFiResource> getResourcesInProcessGroup(@PathVariable String processGroupId) {
        return nifiResourceService.getResourcesInProcessGroup(processGroupId);
    }
}
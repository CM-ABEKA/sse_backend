package com.sensys.sse_engine.controller;

import com.sensys.sse_engine.services.NiFiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.ProcessorEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/nifi/operations")
@RequiredArgsConstructor
@Slf4j
public class NiFiOperationController {

    private final NiFiService nifiService;

    @GetMapping(path = "/process-groups/{processGroupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ProcessGroupEntity> getProcessGroup(@PathVariable String processGroupId) {
        return nifiService.getProcessGroup(processGroupId);
    }

    @PostMapping(path = "/process-groups/{processGroupId}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ProcessGroupEntity> startProcessGroup(@PathVariable String processGroupId) {
        return nifiService.startProcessGroup(processGroupId);
    }

    @PostMapping(path = "/process-groups/{processGroupId}/stop", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ProcessGroupEntity> stopProcessGroup(@PathVariable String processGroupId) {
        return nifiService.stopProcessGroup(processGroupId);
    }

    @GetMapping(path = "/processors/{processorId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ProcessorEntity> getProcessor(@PathVariable String processorId) {
        return nifiService.getProcessor(processorId);
    }
}
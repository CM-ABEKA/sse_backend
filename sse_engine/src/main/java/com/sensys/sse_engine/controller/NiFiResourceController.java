package com.sensys.sse_engine.controller;

import com.sensys.sse_engine.dto.NiFiResource;
import com.sensys.sse_engine.services.NiFiResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/nifi/resources")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "NiFi Resources", description = "API endpoints for managing NiFi resources")
public class NiFiResourceController {

    private final NiFiResourceService nifiResourceService;

    /**
     * Get all NiFi resources
     *
     * @return Flux of all NiFi resources
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all resources", description = "Retrieves all NiFi resources")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all resources"),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public Flux<NiFiResource> getAllResources() {
        log.debug("GET request received for all NiFi resources");
        return nifiResourceService.getAllResources()
            .mapNotNull(dto -> dto.getResources())
            .flatMapMany(Flux::fromIterable)
            .filter(Objects::nonNull)
            .doOnComplete(() -> log.debug("Completed retrieving all NiFi resources"));
    }

    /**
     * Get all process groups
     *
     * @return Flux of process group resources
     */
    @GetMapping(path = "/process-groups", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all process groups", description = "Retrieves all NiFi process groups")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved process groups"),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public Flux<NiFiResource> getProcessGroups() {
        log.debug("GET request received for all process groups");
        return nifiResourceService.getProcessGroups();
    }

    /**
     * Get all processors
     *
     * @return Flux of processor resources
     */
    @GetMapping(path = "/processors", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all processors", description = "Retrieves all NiFi processors")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved processors"),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public Flux<NiFiResource> getProcessors() {
        log.debug("GET request received for all processors");
        return nifiResourceService.getProcessors();
    }

    /**
     * Find a process group by name
     *
     * @param name Name of the process group to find
     * @return ResponseEntity containing the matching process group resource, if found
     */
    @GetMapping(path = "/process-groups/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Find process group by name", description = "Searches for a process group with the specified name")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved process group"),
        @ApiResponse(responseCode = "404", description = "Process group not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public Mono<ResponseEntity<NiFiResource>> findProcessGroupByName(
            @Parameter(description = "Name of the process group to find", required = true)
            @RequestParam String name) {
        if (name == null || name.trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        log.debug("GET request received to find process group with name: {}", name);
        return nifiResourceService.findProcessGroupByName(name.trim())
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    /**
     * Find a processor by name
     *
     * @param name Name of the processor to find
     * @return ResponseEntity containing the matching processor resource, if found
     */
    @GetMapping(path = "/processors/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Find processor by name", description = "Searches for a processor with the specified name")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved processor"),
        @ApiResponse(responseCode = "404", description = "Processor not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public Mono<ResponseEntity<NiFiResource>> findProcessorByName(
            @Parameter(description = "Name of the processor to find", required = true)
            @RequestParam String name) {
        if (name == null || name.trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        log.debug("GET request received to find processor with name: {}", name);
        return nifiResourceService.findProcessorByName(name.trim())
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    /**
     * Find a process group by ID
     *
     * @param id ID of the process group to find
     * @return ResponseEntity containing the matching process group resource, if found
     */
    @GetMapping(path = "/process-groups/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Find process group by ID", description = "Retrieves a process group with the specified ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved process group"),
        @ApiResponse(responseCode = "404", description = "Process group not found"),
        @ApiResponse(responseCode = "400", description = "Invalid process group ID")
    })
    public Mono<ResponseEntity<NiFiResource>> findProcessGroupById(
            @Parameter(description = "ID of the process group to find", required = true)
            @PathVariable String id) {
        if (id == null || id.trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        log.debug("GET request received to find process group with ID: {}", id);
        return nifiResourceService.findProcessGroupById(id.trim())
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    /**
     * Get all resources within a specific process group
     *
     * @param processGroupId ID of the process group
     * @return Flux of resources within the specified process group
     */
    @GetMapping(path = "/process-groups/{processGroupId}/resources", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get resources in process group", 
              description = "Retrieves all resources within the specified process group")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved resources"),
        @ApiResponse(responseCode = "400", description = "Invalid process group ID"),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public Mono<ResponseEntity<Flux<NiFiResource>>> getResourcesInProcessGroup(
            @Parameter(description = "ID of the process group", required = true)
            @PathVariable String processGroupId) {
        if (processGroupId == null || processGroupId.trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        log.debug("GET request received for resources in process group: {}", processGroupId);
        Flux<NiFiResource> resources = nifiResourceService.getResourcesInProcessGroup(processGroupId.trim());
        return Mono.just(ResponseEntity.ok(resources));
    }
}
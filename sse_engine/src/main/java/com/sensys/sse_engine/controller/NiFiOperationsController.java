package com.sensys.sse_engine.controller;

import com.sensys.sse_engine.services.NiFiOperationsService;
import com.sensys.sse_engine.services.NiFiResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/nifi/operations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "NiFi Operations", description = "API endpoints for NiFi operations")
public class NiFiOperationsController {

    private final NiFiOperationsService nifiOperationsService;
    private final NiFiResourceService nifiResourceService;

    /**
     * Start a process group
     *
     * @param processGroupId ID of the process group to start
     * @return ResponseEntity indicating success or error
     */
    @PutMapping("/process-groups/{processGroupId}/start")
    @Operation(summary = "Start process group", 
              description = "Starts the specified process group and all components within it")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Process group started successfully"),
        @ApiResponse(responseCode = "404", description = "Process group not found"),
        @ApiResponse(responseCode = "400", description = "Invalid process group ID"),
        @ApiResponse(responseCode = "500", description = "Failed to start process group")
    })
    public Mono<ResponseEntity<Void>> startProcessGroup(
            @Parameter(description = "ID of the process group to start", required = true)
            @PathVariable String processGroupId) {
        
        if (processGroupId == null || processGroupId.trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        String trimmedId = processGroupId.trim();
        log.debug("Received request to start process group: {}", trimmedId);

        // First verify the process group exists
        return nifiResourceService.findProcessGroupById(trimmedId)
            .flatMap(pg -> nifiOperationsService.startProcessGroup(trimmedId)
                .then(Mono.just(ResponseEntity.ok().<Void>build())))
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .onErrorResume(error -> {
                log.error("Failed to start process group {}: {}", trimmedId, error.getMessage());
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }

    /**
     * Stop a process group
     *
     * @param processGroupId ID of the process group to stop
     * @return ResponseEntity indicating success or error
     */
    @PutMapping("/process-groups/{processGroupId}/stop")
    @Operation(summary = "Stop process group", 
              description = "Stops the specified process group and all components within it")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Process group stopped successfully"),
        @ApiResponse(responseCode = "404", description = "Process group not found"),
        @ApiResponse(responseCode = "400", description = "Invalid process group ID"),
        @ApiResponse(responseCode = "500", description = "Failed to stop process group")
    })
    public Mono<ResponseEntity<Void>> stopProcessGroup(
            @Parameter(description = "ID of the process group to stop", required = true)
            @PathVariable String processGroupId) {
        
        if (processGroupId == null || processGroupId.trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        String trimmedId = processGroupId.trim();
        log.debug("Received request to stop process group: {}", trimmedId);

        // First verify the process group exists
        return nifiResourceService.findProcessGroupById(trimmedId)
            .flatMap(pg -> nifiOperationsService.stopProcessGroup(trimmedId)
                .then(Mono.just(ResponseEntity.ok().<Void>build())))
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .onErrorResume(error -> {
                log.error("Failed to stop process group {}: {}", trimmedId, error.getMessage());
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }
}
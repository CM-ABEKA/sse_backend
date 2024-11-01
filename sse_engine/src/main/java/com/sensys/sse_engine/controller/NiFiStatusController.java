package com.sensys.sse_engine.controller;

import com.sensys.sse_engine.dto.ProcessGroupStatus;
import com.sensys.sse_engine.dto.ProcessorStatusSnapshot;
import com.sensys.sse_engine.services.NiFiResourceService;
import com.sensys.sse_engine.services.NiFiStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/nifi/status")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "NiFi Status", description = "API endpoints for monitoring NiFi process group status")
public class NiFiStatusController {

    private final NiFiStatusService nifiStatusService;
    private final NiFiResourceService nifiResourceService;

    /**
     * Get current status of a process group
     *
     * @param processGroupId ID of the process group
     * @return ResponseEntity containing process group status
     */
    @GetMapping(path = "/process-groups/{processGroupId}", 
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get process group status", 
        description = "Retrieves current status information for the specified process group including processors, connections, and metrics"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved status"),
        @ApiResponse(responseCode = "404", description = "Process group not found"),
        @ApiResponse(responseCode = "400", description = "Invalid process group ID"),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public Mono<ResponseEntity<ProcessGroupStatus>> getProcessGroupStatus(
            @Parameter(description = "ID of the process group", required = true)
            @PathVariable String processGroupId) {
        
        if (processGroupId == null || processGroupId.trim().isEmpty()) {
            log.debug("Invalid process group ID provided");
            return Mono.just(ResponseEntity.badRequest().build());
        }

        String trimmedId = processGroupId.trim();
        log.debug("GET request received for process group status: {}", trimmedId);

        return nifiResourceService.findProcessGroupById(trimmedId)
            .flatMap(pg -> nifiStatusService.getProcessGroupStatus(trimmedId)
                .map(ResponseEntity::ok))
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .onErrorResume(error -> {
                log.error("Error getting status for process group {}: {}", 
                    trimmedId, error.getMessage());
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }

    /**
     * Stream status updates for a process group
     *
     * @param processGroupId ID of the process group
     * @param interval Optional polling interval in seconds (default: 5)
     * @return Server-sent events stream of process group status updates
     */
    @GetMapping(
        path = "/process-groups/{processGroupId}/stream", 
        produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    @Operation(
        summary = "Stream process group status", 
        description = "Streams real-time status updates for the specified process group using server-sent events (SSE)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status stream established successfully"),
        @ApiResponse(responseCode = "404", description = "Process group not found"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public Mono<ResponseEntity<Flux<ProcessGroupStatus>>> streamProcessGroupStatus(
            @Parameter(description = "ID of the process group", required = true)
            @PathVariable String processGroupId,
            @Parameter(description = "Polling interval in seconds (1-60)", example = "5")
            @RequestParam(defaultValue = "5") Integer interval) {
        
        // Validate process group ID
        if (processGroupId == null || processGroupId.trim().isEmpty()) {
            log.debug("Invalid process group ID provided");
            return Mono.just(ResponseEntity.badRequest().build());
        }

        // Validate interval
        if (interval < 1 || interval > 60) {
            log.debug("Invalid interval provided: {}. Must be between 1 and 60 seconds", interval);
            return Mono.just(ResponseEntity.badRequest().build());
        }

        String trimmedId = processGroupId.trim();
        log.debug("Stream request received for process group status: {} with interval: {}s", 
            trimmedId, interval);

        // Verify process group exists before starting stream
        return nifiResourceService.findProcessGroupById(trimmedId)
            .map(pg -> ResponseEntity.ok(
                nifiStatusService.streamProcessGroupStatus(trimmedId, interval)
                    .doOnError(error -> log.error("Error in status stream for process group {}: {}", 
                        trimmedId, error.getMessage()))
            ))
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .onErrorResume(error -> {
                log.error("Error setting up status stream for process group {}: {}", 
                    trimmedId, error.getMessage());
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }

    /**
     * Get processor status within a process group
     *
     * @param processGroupId ID of the process group
     * @param processorId ID of the processor
     * @return ResponseEntity containing processor status
     */
    @GetMapping(
        path = "/process-groups/{processGroupId}/processors/{processorId}", 
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Get processor status", 
        description = "Retrieves current status information for a specific processor within a process group"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved processor status"),
        @ApiResponse(responseCode = "404", description = "Process group or processor not found"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public Mono<ResponseEntity<ProcessorStatusSnapshot>> getProcessorStatus(
            @Parameter(description = "ID of the process group", required = true)
            @PathVariable String processGroupId,
            @Parameter(description = "ID of the processor", required = true)
            @PathVariable String processorId) {
        
        if (processGroupId == null || processGroupId.trim().isEmpty() || 
            processorId == null || processorId.trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        String trimmedGroupId = processGroupId.trim();
        String trimmedProcessorId = processorId.trim();
        
        log.debug("GET request received for processor status: {} in process group: {}", 
            trimmedProcessorId, trimmedGroupId);

        return nifiStatusService.getProcessGroupStatus(trimmedGroupId)
            .flatMap(status -> Mono.justOrEmpty(
                status.getAggregateSnapshot().getProcessorStatusSnapshots().stream()
                    .filter(entity -> entity.getId().equals(trimmedProcessorId))
                    .findFirst()
                    .map(entity -> entity.getProcessorStatusSnapshot())
            ))
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .onErrorResume(error -> {
                log.error("Error getting processor status: {} in process group {}: {}", 
                    trimmedProcessorId, trimmedGroupId, error.getMessage());
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }
}
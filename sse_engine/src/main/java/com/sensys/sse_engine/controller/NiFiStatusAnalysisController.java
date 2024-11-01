package com.sensys.sse_engine.controller;

import com.sensys.sse_engine.dto.ProcessGroupAnalysis;
import com.sensys.sse_engine.services.NiFiStatusAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/nifi/analysis")
@RequiredArgsConstructor
@Slf4j
public class NiFiStatusAnalysisController {

    private final NiFiStatusAnalysisService analysisService;

    @GetMapping(path = "/process-groups/{processGroupId}", 
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Analyze process group status",
              description = "Analyzes queue status and processor states in the specified process group")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Analysis completed successfully"),
        @ApiResponse(responseCode = "404", description = "Process group not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public Mono<ResponseEntity<ProcessGroupAnalysis>> analyzeProcessGroup(
            @Parameter(description = "ID of the process group", required = true)
            @PathVariable String processGroupId) {
        
        return analysisService.analyzeProcessGroupStatus(processGroupId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .onErrorResume(error -> {
                log.error("Error analyzing process group {}: {}", 
                    processGroupId, error.getMessage());
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }
}
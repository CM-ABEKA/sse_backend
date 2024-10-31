package com.sensys.sse_engine.controller;

import java.sql.SQLException;

import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.ProcessorEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sensys.sse_engine.DatabaseService;
import com.sensys.sse_engine.exception.NiFiClientException;
import com.sensys.sse_engine.exception.NiFiServerException;
import com.sensys.sse_engine.model.CompareRequest;
import com.sensys.sse_engine.model.TableComparisonResult;
import com.sensys.sse_engine.services.NiFiService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/")
@Slf4j
public class SSEController {

    private final DatabaseService databaseService;
    private final NiFiService nifiService;

    public SSEController(DatabaseService databaseService, NiFiService nifiService) {
        this.databaseService = databaseService;
        this.nifiService = nifiService;
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "Nifi Backend service is healthy!";
    }

    @PostMapping("/db/compare-tables")
    public TableComparisonResult compareTables(@RequestBody CompareRequest request) throws SQLException {
        return databaseService.compareTables(request.getSourceConfig(), request.getDestConfig(), request.isCompareByTableNamesOnly());
    }

    @PostMapping("/db/seed_nifi")
    public String seedNifi() {
        return "Seeding Data...";
    }

    @GetMapping("/nifi/process-groups/{processGroupId}")
    public Mono<ProcessGroupEntity> getProcessGroup(@PathVariable String processGroupId) {
        return nifiService.getProcessGroup(processGroupId);
    }

    @PostMapping("/nifi/process-groups/{processGroupId}/start")
    public Mono<ProcessGroupEntity> startProcessGroup(@PathVariable String processGroupId) {
        return nifiService.startProcessGroup(processGroupId);
    }

    @PostMapping("/nifi/process-groups/{processGroupId}/stop")
    public Mono<ProcessGroupEntity> stopProcessGroup(@PathVariable String processGroupId) {
        return nifiService.stopProcessGroup(processGroupId);
    }

    @GetMapping("/nifi/processors/{processorId}")
    public Mono<ProcessorEntity> getProcessor(@PathVariable String processorId) {
        return nifiService.getProcessor(processorId);
    }

    @ExceptionHandler(NiFiClientException.class)
    public ResponseEntity<String> handleNiFiClientException(NiFiClientException ex) {
        log.error("NiFi client error: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode())
                .body(ex.getMessage());
    }

    @ExceptionHandler(NiFiServerException.class)
    public ResponseEntity<String> handleNiFiServerException(NiFiServerException ex) {
        log.error("NiFi server error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ex.getMessage());
    }
}

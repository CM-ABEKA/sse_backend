package com.sensys.sse_engine.controller;

import com.sensys.sse_engine.DatabaseService;
import com.sensys.sse_engine.model.CompareRequest;
import com.sensys.sse_engine.model.TableComparisonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/db")
@RequiredArgsConstructor
@Slf4j
public class DatabaseController {

    private final DatabaseService databaseService;

    @PostMapping("/compare-tables")
    public TableComparisonResult compareTables(@RequestBody CompareRequest request) throws SQLException {
        return databaseService.compareTables(request.getSourceConfig(), request.getDestConfig(), request.isCompareByTableNamesOnly());
    }

    @PostMapping("/seed-nifi")
    public ResponseEntity<String> seedNifi() {
        log.info("Initiating NiFi seeding process");
        return ResponseEntity.ok("Seeding Data...");
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<String> handleSQLException(SQLException ex) {
        log.error("Database error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Database operation failed: " + ex.getMessage());
    }
}
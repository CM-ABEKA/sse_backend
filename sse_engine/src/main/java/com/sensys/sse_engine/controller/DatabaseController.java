// DatabaseController.java
package com.sensys.sse_engine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sensys.sse_engine.DatabaseService;
import com.sensys.sse_engine.model.DatabaseConfig;
import com.sensys.sse_engine.model.SchemaTransferRequest;

@RestController
@RequestMapping("/api")
public class DatabaseController {

    @Autowired
    private DatabaseService databaseService;

    @GetMapping("/health")
    public String healthCheck() {
        return "Database service is healthy!";
    }

    @PostMapping("/db/migrate")
    public ResponseEntity<String> migrateDatabase(@RequestBody DatabaseConfig config) {
        try {
            databaseService.migrateDatabase(config);
            return ResponseEntity.ok("Database migrated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error during database migration: " + e.getMessage());
        }
    }

    @PostMapping("/db/transfer")
    public ResponseEntity<String> transferSchema(@RequestBody SchemaTransferRequest request) {
        try {
            databaseService.transferSchemaUsingFlyway(
                    request.getSourceConfig(),
                    request.getDestConfig(),
                    request.isDropTablesIfExists()
            );
            return ResponseEntity.ok("Schema transferred successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error during schema transfer: " + e.getMessage());
        }
    }
}
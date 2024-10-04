package com.sensys.sse_engine.controller;

import java.io.IOException;
import java.sql.SQLException;

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
    public String getMethodName() {
        return "Database Check is Up";
    }

    @PostMapping("/db_init")
    public ResponseEntity<String> transferSchema(@RequestBody SchemaTransferRequest request) {
        try {
            DatabaseConfig sourceConfig = request.getSourceConfig();
            DatabaseConfig destConfig = request.getDestConfig();

            databaseService.transferSchemaUsingDump(sourceConfig, destConfig, request.isIncludeData(), request.isDropTablesIfExists());

             return ResponseEntity.ok("Schema transfer completed successfully");
    } catch (IOException | InterruptedException | SQLException e) { 
        return ResponseEntity.status(500).body("Error during schema transfer: " + e.getMessage());
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage()); 
    }
    }
}
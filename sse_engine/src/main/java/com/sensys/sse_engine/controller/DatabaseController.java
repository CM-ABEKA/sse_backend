package com.sensys.sse_engine.controller;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sensys.sse_engine.DatabaseService;
import com.sensys.sse_engine.model.CompareRequest;
import com.sensys.sse_engine.model.TableComparisonResult;

@RestController
@RequestMapping("/api/db/")
public class DatabaseController {

    @Autowired
    private DatabaseService databaseService;

    @GetMapping("/health")
    public String healthCheck() {
        return "Database service is healthy!";
    }

    @PostMapping("/compare-tables")
    public TableComparisonResult compareTables(@RequestBody CompareRequest request) throws SQLException {
        return databaseService.compareTables(request.getSourceConfig(), request.getDestConfig(), request.isCompareByTableNamesOnly());
    }

    @PostMapping("/seed_nifi")
    public String seedNifi() {
    return "Seeding Data...";
    }

}

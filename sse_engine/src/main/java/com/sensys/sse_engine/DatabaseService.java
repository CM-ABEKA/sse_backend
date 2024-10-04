package com.sensys.sse_engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sensys.sse_engine.model.DatabaseConfig;

@Service
public class DatabaseService {

    public Connection connectToDatabase(DatabaseConfig config) throws SQLException {
        String url = buildJdbcUrl(config);
        return DriverManager.getConnection(url, config.getUsername(), config.getPassword());
    }

    private String buildJdbcUrl(DatabaseConfig config) {
        String baseUrl = config.getDatabaseType().equals("mysql")
                ? "jdbc:mysql://"
                : "jdbc:postgresql://"; 
        return baseUrl + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();
    }

    public void transferSchemaUsingDump(DatabaseConfig sourceConfig, DatabaseConfig destConfig,
                                        boolean includeData, boolean dropTablesIfExists) 
                                        throws IOException, InterruptedException, SQLException {

        // 1. Build the mysqldump command 
        List<String> command = new ArrayList<>(Arrays.asList(
            "mysqldump",
            "-u", sourceConfig.getUsername(),
            "-p" + sourceConfig.getPassword(), 
            "-h", sourceConfig.getHost(),
            "-P", String.valueOf(sourceConfig.getPort()),
            sourceConfig.getDatabase() 
        ));

        if (!includeData) {
            command.add("-d"); // Add -d flag for schema only
        }

        // 2. Execute mysqldump and capture output 
        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();

        StringBuilder dumpOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                dumpOutput.append(line).append("\n");
            }
        }

        process.waitFor();

        if (process.exitValue() != 0) {
            throw new RuntimeException("mysqldump failed! Error: " + dumpOutput.toString());
        }

        // 3. Connect to the destination database
        try (Connection destConnection = connectToDatabase(destConfig)) { 

            // 4. (Optional) Drop existing tables
            if (dropTablesIfExists) {
                dropExistingTables(destConnection, sourceConfig.getDatabase());
            }

            // 5. Build the mysql import command
            List<String> importCommand = new ArrayList<>(Arrays.asList(
                "mysql",
                "-u", destConfig.getUsername(),
                "-p" + destConfig.getPassword(),
                "-h", destConfig.getHost(),
                "-P", String.valueOf(destConfig.getPort()),
                destConfig.getDatabase()
            ));

            // 6. Execute mysql import, piping the dump output 
            ProcessBuilder importPb = new ProcessBuilder(importCommand);
            Process importProcess = importPb.start();

            try (OutputStream os = importProcess.getOutputStream();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))) {
                writer.write(dumpOutput.toString()); 
            }

            importProcess.waitFor(); 

            if (importProcess.exitValue() != 0) {
                throw new RuntimeException("mysql import failed!"); 
            }
        } 
    }

    private void dropExistingTables(Connection destConnection, String databaseName) throws SQLException {
        try (Statement stmt = destConnection.createStatement()) {
            String sql = "DROP TABLE IF EXISTS " + databaseName + ".*";
            stmt.execute(sql);
        }
    }
}

// DatabaseService.java
package com.sensys.sse_engine;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import com.sensys.sse_engine.model.DatabaseConfig;

@Service
public class DatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    public DataSource createDataSource(DatabaseConfig config) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(getDriverClassName(config.getDatabaseType()));
        dataSource.setUrl(buildJdbcUrl(config));
        dataSource.setUsername(config.getUsername());
        dataSource.setPassword(config.getPassword());
        return dataSource;
    }

    private Flyway createFlyway(DataSource dataSource, DatabaseConfig config) {
        logger.info("Creating Flyway instance for URL: {}", buildJdbcUrl(config)); // Log without password
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("db/migration/" + config.getDatabaseType())
                .load();
    }

    public String buildJdbcUrl(DatabaseConfig config) {
        String baseUrl = "";
        if (config.getDatabaseType().equalsIgnoreCase("mysql")) {
            baseUrl = "jdbc:mysql://";
        } else if (config.getDatabaseType().equalsIgnoreCase("postgresql")) {
            baseUrl = "jdbc:postgresql://";
        } else {
            throw new IllegalArgumentException("Unsupported database type: " + config.getDatabaseType());
        }
        return baseUrl + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();
    }

    private String getDriverClassName(String databaseType) {
        switch (databaseType.toLowerCase()) {
            case "mysql":
                return "com.mysql.cj.jdbc.Driver";
            case "postgresql":
                return "org.postgresql.Driver";
            default:
                throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
    }

    public void migrateDatabase(DatabaseConfig config) {
        try (Connection connection = createDataSource(config).getConnection()) {
            
            Flyway flyway = createFlyway(createDataSource(config), config);
            flyway.migrate();
        } catch (SQLException e) {
            logger.error("Error during database migration: ", e);
            throw new RuntimeException("Database migration failed!", e);
        }
    }

    public void transferSchemaUsingFlyway(DatabaseConfig sourceConfig,
            DatabaseConfig destConfig,
            boolean dropTablesIfExists) {
        try {
            Flyway destFlyway = createFlyway(createDataSource(destConfig), destConfig);

            if (dropTablesIfExists) {
                logger.warn("Dropping all objects in target schema: {}", destConfig.getDatabase());
                destFlyway.clean();
            }

            destFlyway.migrate();

            Flyway sourceFlyway = createFlyway(createDataSource(sourceConfig), sourceConfig);
            int sourceVersion = sourceFlyway.info().current().getVersion().hashCode();
            logger.info("Source database version: {}", sourceVersion);

        } catch (Exception e) {
            logger.error("Error during schema transfer: ", e);
            throw new RuntimeException("Schema transfer failed!", e);
        }
    }
}

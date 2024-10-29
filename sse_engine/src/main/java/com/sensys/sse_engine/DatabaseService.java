package com.sensys.sse_engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLNonTransientConnectionException;
import java.util.ArrayList;
import java.util.List;

import com.sensys.sse_engine.exception.DatabaseConfigException;
import com.sensys.sse_engine.model.DatabaseConfig;
import com.sensys.sse_engine.model.TableComparisonResult;

@Service
@Scope("prototype")
public class DatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    public Connection createRawConnection(DatabaseConfig config) {
        String jdbcUrl = buildJdbcUrl(config);
        logger.info("Creating raw connection with URL: {}", jdbcUrl);
        try {
            return DriverManager.getConnection(jdbcUrl, config.getUsername(), config.getPassword());
        } catch (SQLInvalidAuthorizationSpecException e) {
            throw new DatabaseConfigException(401, "Invalid username or password for the database.");
        } catch (SQLNonTransientConnectionException e) {
            throw new DatabaseConfigException(503, "Unable to connect to the database. Please check the host and port.");
        } catch (SQLException e) {
            throw new DatabaseConfigException(500, "Database connection error: " + e.getMessage());
        }
    }

    public DataSource createDataSource(DatabaseConfig config) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        String jdbcUrl = buildJdbcUrl(config);
        logger.info("Creating DataSource with URL: {}", jdbcUrl);
        dataSource.setDriverClassName(getDriverClassName(config.getDatabaseType()));
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(config.getUsername());
        dataSource.setPassword(config.getPassword());
        return dataSource;
    }

    public String buildJdbcUrl(DatabaseConfig config) {
        String baseUrl;
        switch (config.getDatabaseType().toLowerCase()) {
            case "mysql":
                baseUrl = "jdbc:mysql://";
                break;
            case "postgresql":
                baseUrl = "jdbc:postgresql://";
                break;
            default:
                throw new DatabaseConfigException(400, "Unsupported database type: " + config.getDatabaseType());
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
                throw new DatabaseConfigException(400, "Unsupported database type: " + databaseType);
        }
    }
    

    private List<String> stripDatabaseNames(List<String> tables) {
        List<String> strippedTables = new ArrayList<>();
        for (String table : tables) {
            // Split the schema/catalog from the table name by the period (.)
            String[] parts = table.split("\\.");
            // Use the last part (table name) only
            strippedTables.add(parts[parts.length - 1]);
        }
        return strippedTables;
    }

    // ... (You can remove migrateDatabase() and transferSchemaUsingFlyway() 
    //      since you're not using Flyway for comparison) ...
    public TableComparisonResult compareTables(DatabaseConfig sourceConfig, DatabaseConfig destConfig, boolean compareByTableNamesOnly) {
        try (Connection sourceConn = createRawConnection(sourceConfig);
             Connection destConn = createRawConnection(destConfig)) {

            logger.info("Comparing tables between {} and {}", sourceConfig.getDatabase(), destConfig.getDatabase());

            // Get table names
            List<String> sourceTables = getTableNames(sourceConn.getMetaData(), sourceConfig.getDatabase());
            List<String> destTables = getTableNames(destConn.getMetaData(), destConfig.getDatabase());

            if (compareByTableNamesOnly) {
                sourceTables = stripDatabaseNames(sourceTables);
                destTables = stripDatabaseNames(destTables);
            }

            logger.info("Source tables: {}", sourceTables);
            logger.info("Destination tables: {}", destTables);

            List<String> tablesOnlyInSource = new ArrayList<>(sourceTables);
            tablesOnlyInSource.removeAll(destTables);

            List<String> tablesOnlyInDestination = new ArrayList<>(destTables);
            tablesOnlyInDestination.removeAll(sourceTables);

            boolean schemasMatch = tablesOnlyInSource.isEmpty() && tablesOnlyInDestination.isEmpty();

            return new TableComparisonResult(schemasMatch, tablesOnlyInSource, tablesOnlyInDestination);  

        } catch (SQLException e) {
            logger.error("Error comparing tables: ", e);
            throw new DatabaseConfigException(500, "Error comparing tables: " + e.getMessage());
        }
    }

    private List<String> getTableNames(DatabaseMetaData metaData, String databaseName) throws SQLException {
        List<String> tables = new ArrayList<>();

        // Use null for schemaPattern to get tables from all schemas (or catalogs)
        try (ResultSet rs = metaData.getTables(databaseName, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                // Use catalog name if schema is null (especially relevant for MySQL)
                String schemaOrCatalog = rs.getString("TABLE_SCHEM");
                if (schemaOrCatalog == null) {
                    schemaOrCatalog = rs.getString("TABLE_CAT"); // Fallback to catalog if schema is null
                }
                String tableName = rs.getString("TABLE_NAME");
                tables.add((schemaOrCatalog != null ? schemaOrCatalog : "null") + "." + tableName);
            }
        }
        return tables;
    }

}

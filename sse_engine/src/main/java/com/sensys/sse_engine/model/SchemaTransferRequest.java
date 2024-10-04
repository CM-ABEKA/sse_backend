package com.sensys.sse_engine.model;

public class SchemaTransferRequest {
    private DatabaseConfig sourceConfig;
    private DatabaseConfig destConfig;
    private boolean includeData;
    private boolean dropTablesIfExists;

    // Getters and Setters

    public DatabaseConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(DatabaseConfig sourceConfig) {
        this.sourceConfig = sourceConfig;
    }

    public DatabaseConfig getDestConfig() {
        return destConfig;
    }

    public void setDestConfig(DatabaseConfig destConfig) {
        this.destConfig = destConfig;
    }

    public boolean isIncludeData() {
        return includeData;
    }

    public void setIncludeData(boolean includeData) {
        this.includeData = includeData;
    }

    public boolean isDropTablesIfExists() {
        return dropTablesIfExists;
    }

    public void setDropTablesIfExists(boolean dropTablesIfExists) {
        this.dropTablesIfExists = dropTablesIfExists;
    }
}
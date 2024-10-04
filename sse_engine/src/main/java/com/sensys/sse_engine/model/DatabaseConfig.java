package com.sensys.sse_engine.model;

public class DatabaseConfig {
    private String databaseType;
    private String username;
    private String password;
    private String host;
    private int port;
    private String database;
    private String databaseName;
    private String databaseIdentity;

    // Getters and setters
    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabaseIdentity() {
        return databaseIdentity;
    }

    public void setDatabaseIdentity(String databaseIdentity) {
        this.databaseIdentity = databaseIdentity;
    }
}
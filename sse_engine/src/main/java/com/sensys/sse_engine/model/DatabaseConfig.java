package com.sensys.sse_engine.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseConfig {
    private String databaseType;
    private String username;
    private String password;
    private String host;
    private int port;
    private String database;
    private String databaseName;
    private String databaseIdentity;    
}
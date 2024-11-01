package com.sensys.sse_engine.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchemaTransferRequest {
    private DatabaseConfig sourceConfig;
    private DatabaseConfig destConfig;
    private boolean includeData;
    private boolean dropTablesIfExists;
}
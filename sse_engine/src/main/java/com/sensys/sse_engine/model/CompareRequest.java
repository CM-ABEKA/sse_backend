package com.sensys.sse_engine.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompareRequest {
    private DatabaseConfig sourceConfig;
    private DatabaseConfig destConfig;
    private boolean compareByTableNamesOnly;
}

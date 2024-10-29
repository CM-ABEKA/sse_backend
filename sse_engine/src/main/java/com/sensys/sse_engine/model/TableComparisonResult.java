package com.sensys.sse_engine.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter 
@Setter
public class TableComparisonResult {
    private boolean schemasMatch;
    private List<String> tablesOnlyInSource;
    private List<String> tablesOnlyInDestination;
    private int countTablesOnlyInSource;      // New field for source table count
    private int countTablesOnlyInDestination; // New field for destination table count

    public TableComparisonResult(boolean schemasMatch, List<String> tablesOnlyInSource, List<String> tablesOnlyInDestination) {
        this.schemasMatch = schemasMatch;
        this.tablesOnlyInSource = tablesOnlyInSource;
        this.tablesOnlyInDestination = tablesOnlyInDestination;
        this.countTablesOnlyInSource = tablesOnlyInSource.size();           // Set count for source
        this.countTablesOnlyInDestination = tablesOnlyInDestination.size(); // Set count for destination
    }
}
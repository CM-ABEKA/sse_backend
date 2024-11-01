package com.sensys.sse_engine.dto.enums;

public enum NiFiResourceType {
    PROCESS_GROUP,
    PROCESSOR,
    OTHER;

    public static NiFiResourceType fromIdentifier(String identifier) {
        if (identifier == null) return OTHER;
        
        if (identifier.startsWith("/process-groups/") && 
            !identifier.startsWith("/data/") && 
            !identifier.startsWith("/policies/") && 
            !identifier.startsWith("/operation/") &&
            !identifier.startsWith("/provenance-data/")) {
            return PROCESS_GROUP;
        }
        
        if (identifier.startsWith("/processors/") && 
            !identifier.startsWith("/data/") && 
            !identifier.startsWith("/policies/") && 
            !identifier.startsWith("/operation/") &&
            !identifier.startsWith("/provenance-data/")) {
            return PROCESSOR;
        }
        
        return OTHER;
    }
}
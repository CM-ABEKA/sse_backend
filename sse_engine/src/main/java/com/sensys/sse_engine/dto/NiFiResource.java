package com.sensys.sse_engine.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NiFiResource {
    private String identifier;
    private String name;
    private String uri;
    private String parentGroupId;
    private boolean processor;
    private boolean processGroup;

    /**
     * Check if this resource is a process group
     * @return true if the resource is a process group
     */
    public boolean isProcessGroup() {
        return processGroup || (identifier != null && 
               identifier.startsWith("/process-groups/") && 
               !identifier.startsWith("/process-groups/", 1));
    }

    /**
     * Check if this resource is a processor
     * @return true if the resource is a processor
     */
    public boolean isProcessor() {
        return processor || (identifier != null && 
               identifier.startsWith("/processors/") && 
               !identifier.startsWith("/processors/", 1));
    }

    /**
     * Extract the ID portion from the identifier
     * @return the ID part of the identifier, or empty string if not available
     */
    public String extractId() {
        if (identifier == null) {
            return "";
        }
        
        // Handle process groups
        if (identifier.startsWith("/process-groups/")) {
            return identifier.substring("/process-groups/".length());
        }
        
        // Handle processors
        if (identifier.startsWith("/processors/")) {
            return identifier.substring("/processors/".length());
        }
        
        // For other resources, return the last segment
        String[] parts = identifier.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : "";
    }

    /**
     * Get the parent process group ID from the identifier
     * @return the parent process group ID, or null if not available
     */
    public String getParentProcessGroupId() {
        if (parentGroupId != null && !parentGroupId.isEmpty()) {
            return parentGroupId;
        }
        return null;
    }
}
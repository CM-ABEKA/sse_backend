package com.sensys.sse_engine.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sensys.sse_engine.dto.enums.NiFiResourceType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NiFiResource {
    private String identifier;
    private String name;
    
    /**
     * Gets the resource type based on the identifier pattern
     */
    public NiFiResourceType getType() {
        return NiFiResourceType.fromIdentifier(identifier);
    }

    /**
     * Gets the resource ID from the identifier path
     */
    public String getId() {
        if (identifier == null) return null;
        
        NiFiResourceType type = getType();
        switch (type) {
            case PROCESSOR:
                return extractIdAfterPrefix("/processors/");
            case PROCESS_GROUP:
                return extractIdAfterPrefix("/process-groups/");
            default:
                return null;
        }
    }

    private String extractIdAfterPrefix(String prefix) {
        if (!identifier.startsWith(prefix)) return null;
        String id = identifier.substring(prefix.length());
        int nextSlash = id.indexOf('/');
        return nextSlash == -1 ? id : id.substring(0, nextSlash);
    }

    /**
     * Convenience method to check if this is a process group
     */
    public boolean isProcessGroup() {
        return getType() == NiFiResourceType.PROCESS_GROUP;
    }

    /**
     * Convenience method to check if this is a processor
     */
    public boolean isProcessor() {
        return getType() == NiFiResourceType.PROCESSOR;
    }
}
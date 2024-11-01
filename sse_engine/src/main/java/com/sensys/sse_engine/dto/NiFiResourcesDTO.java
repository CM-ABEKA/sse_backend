package com.sensys.sse_engine.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sensys.sse_engine.dto.enums.NiFiResourceType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NiFiResourcesDTO {
    private List<NiFiResource> resources = new ArrayList<>();

    /**
     * Get all process groups
     */
    public List<NiFiResource> getProcessGroups() {
        if (resources == null) return new ArrayList<>();
        return resources.stream()
            .filter(NiFiResource::isProcessGroup)
            .collect(Collectors.toList());
    }

    /**
     * Get all processors
     */
    public List<NiFiResource> getProcessors() {
        if (resources == null) return new ArrayList<>();
        return resources.stream()
            .filter(NiFiResource::isProcessor)
            .collect(Collectors.toList());
    }

    /**
     * Find a process group by name
     */
    public NiFiResource findProcessGroupByName(String name) {
        if (name == null) return null;
        return getProcessGroups().stream()
            .filter(pg -> name.equals(pg.getName()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Find a processor by name
     */
    public NiFiResource findProcessorByName(String name) {
        if (name == null) return null;
        return getProcessors().stream()
            .filter(p -> name.equals(p.getName()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Find resource by ID
     */
    public NiFiResource findResourceById(String id) {
        if (id == null) return null;
        return resources.stream()
            .filter(r -> id.equals(r.getId()))
            .findFirst()
            .orElse(null);
    }
}
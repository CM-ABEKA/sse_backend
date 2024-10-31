package com.sensys.sse_engine.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private boolean empty;
    private int size;

    /**
     * Get all process groups from the resources list
     * @return List of process group resources
     */
    public List<NiFiResource> getProcessGroups() {
        if (resources == null) {
            return new ArrayList<>();
        }
        return resources.stream()
                .filter(resource -> resource != null && resource.isProcessGroup())
                .collect(Collectors.toList());
    }

    /**
     * Get all processors from the resources list
     * @return List of processor resources
     */
    public List<NiFiResource> getProcessors() {
        if (resources == null) {
            return new ArrayList<>();
        }
        return resources.stream()
                .filter(resource -> resource != null && resource.isProcessor())
                .collect(Collectors.toList());
    }

    /**
     * Find a process group by name
     * @param name The name to search for
     * @return The process group with the matching name, or null if not found
     */
    public NiFiResource findProcessGroupByName(String name) {
        if (resources == null || name == null) {
            return null;
        }
        return getProcessGroups().stream()
                .filter(pg -> name.equals(pg.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Find a processor by name
     * @param name The name to search for
     * @return The processor with the matching name, or null if not found
     */
    public NiFiResource findProcessorByName(String name) {
        if (resources == null || name == null) {
            return null;
        }
        return getProcessors().stream()
                .filter(p -> name.equals(p.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get resources by their parent process group ID
     * @param processGroupId The ID of the parent process group
     * @return List of resources within the specified process group
     */
    public List<NiFiResource> getResourcesByProcessGroup(String processGroupId) {
        if (resources == null || processGroupId == null) {
            return new ArrayList<>();
        }
        return resources.stream()
                .filter(resource -> resource != null && 
                        processGroupId.equals(resource.getParentProcessGroupId()))
                .collect(Collectors.toList());
    }

    /**
     * Find a resource by its identifier
     * @param identifier The identifier to search for
     * @return The resource with the matching identifier, or null if not found
     */
    public NiFiResource findResourceByIdentifier(String identifier) {
        if (resources == null || identifier == null) {
            return null;
        }
        return resources.stream()
                .filter(resource -> resource != null && 
                        identifier.equals(resource.getIdentifier()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Set the resources list with null safety
     * @param resources The list of resources to set
     */
    public void setResources(List<NiFiResource> resources) {
        this.resources = resources != null ? resources : new ArrayList<>();
        this.empty = this.resources.isEmpty();
        this.size = this.resources.size();
    }
}
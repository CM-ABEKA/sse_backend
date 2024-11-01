package com.sensys.sse_engine.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConnectionStatusSnapshot implements NiFiStatusDTO {
    private String id;
    private String groupId;
    private String name;
    private String sourceName;
    private String destinationName;
    private Integer flowFilesIn;
    private Long bytesIn;
    private String input;
    private Integer flowFilesOut;
    private Long bytesOut;
    private String output;
    private Integer flowFilesQueued;
    private Long bytesQueued;
    private String queued;
    private String queuedSize;
    private String queuedCount;
    private Integer percentUseCount;
    private Integer percentUseBytes;
    private String flowFileAvailability;
}
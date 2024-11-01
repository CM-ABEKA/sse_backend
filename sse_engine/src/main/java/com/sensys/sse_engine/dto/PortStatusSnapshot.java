package com.sensys.sse_engine.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortStatusSnapshot implements NiFiStatusDTO {
    private String id;
    private String groupId;
    private String name;
    private Integer runStatus;
    private Long bytesReceived;
    private Long bytesSent;
    private Integer flowFilesReceived;
    private Integer flowFilesSent;
    private String received;
    private String sent;
    private Integer activeThreadCount;
}
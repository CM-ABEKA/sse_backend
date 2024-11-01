package com.sensys.sse_engine.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteProcessGroupStatusSnapshot implements NiFiStatusDTO {
    private String id;
    private String groupId;
    private String name;
    private String targetUri;
    private String transmissionStatus;
    private Integer activeThreadCount;
    private Long flowFilesSent;
    private Long bytesSent;
    private String sent;
    private Long flowFilesReceived;
    private Long bytesReceived;
    private String received;
}
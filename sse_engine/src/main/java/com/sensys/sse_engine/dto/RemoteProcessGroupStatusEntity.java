package com.sensys.sse_engine.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteProcessGroupStatusEntity implements NiFiStatusDTO {
    private String id;
    private RemoteProcessGroupStatusSnapshot remoteProcessGroupStatusSnapshot;
    private Boolean canRead;
}
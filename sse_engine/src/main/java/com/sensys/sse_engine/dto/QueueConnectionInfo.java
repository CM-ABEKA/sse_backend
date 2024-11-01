package com.sensys.sse_engine.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueueConnectionInfo {
    private String connectionId;
    private String sourceProcessor;
    private String destinationProcessor;
    private Integer queuedCount;
    private String queuedSize;
}
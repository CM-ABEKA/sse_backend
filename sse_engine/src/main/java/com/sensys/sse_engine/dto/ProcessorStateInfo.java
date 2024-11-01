package com.sensys.sse_engine.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessorStateInfo {
    private String processorId;
    private String processorName;
    private String processorType;
    private String runStatus;
}
package com.sensys.sse_engine.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessorTimingInfo {
    private long tasksDurationNanos;
    private String formattedProcessingTime;
    private double processingTimeSeconds;
    private long taskCount;
    private double averageTaskDurationMs;
}
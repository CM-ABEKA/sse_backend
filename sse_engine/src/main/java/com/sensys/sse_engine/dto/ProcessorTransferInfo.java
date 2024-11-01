package com.sensys.sse_engine.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessorTransferInfo {
    private String processorId;
    private String processorName;
    private String processorType;
    private TransferMetrics input;
    private TransferMetrics output;
    private ProcessorTimingInfo timing;  // Added timing information
}
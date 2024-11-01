package com.sensys.sse_engine.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ProcessGroupAnalysis {
    private String processGroupId;
    private String processGroupName;
    private List<QueueConnectionInfo> queueConnections;
    private List<ProcessorStateInfo> stoppedProcessors;
    private List<ProcessorStateInfo> notRunningProcessors;
    private List<ProcessorTransferInfo> processorTransfers;
    private TransferMetrics groupTotalTransfer;
}
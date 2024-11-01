package com.sensys.sse_engine.services;

import com.sensys.sse_engine.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NiFiStatusAnalysisService {
    
    private final NiFiStatusService nifiStatusService;
    private static final double BYTES_TO_KB = 1024.0;
    private static final double BYTES_TO_MB = 1024.0 * 1024.0;
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");

    /**
     * Analyzes a process group to extract queue information, processor states and transfer metrics
     */
    public Mono<ProcessGroupAnalysis> analyzeProcessGroupStatus(String processGroupId) {
        return nifiStatusService.getProcessGroupStatus(processGroupId)
            .map(this::extractProcessGroupAnalysis)
            .doOnSuccess(analysis -> logAnalysisResults(analysis))
            .doOnError(error -> log.error("Error analyzing process group {}: {}", 
                processGroupId, error.getMessage()));
    }

    /**
     * Extracts comprehensive analysis from process group status
     */
    private ProcessGroupAnalysis extractProcessGroupAnalysis(ProcessGroupStatus status) {
        ProcessGroupSnapshot snapshot = status.getAggregateSnapshot();
        
        return ProcessGroupAnalysis.builder()
            .processGroupId(status.getId())
            .processGroupName(status.getName())
            .queueConnections(extractQueueConnections(snapshot))
            .stoppedProcessors(extractStoppedProcessors(snapshot))
            .notRunningProcessors(extractNonRunningProcessors(snapshot))
            .processorTransfers(extractProcessorTransfers(snapshot))
            .groupTotalTransfer(calculateGroupTotalTransfers(snapshot))
            .build();
    }

    /**
     * Extracts information about queued connections
     */
    private List<QueueConnectionInfo> extractQueueConnections(ProcessGroupSnapshot snapshot) {
        return snapshot.getConnectionStatusSnapshots().stream()
            .filter(conn -> hasQueuedFlowFiles(conn.getConnectionStatusSnapshot()))
            .map(this::createQueueConnectionInfo)
            .collect(Collectors.toList());
    }

    private boolean hasQueuedFlowFiles(ConnectionStatusSnapshot connectionSnapshot) {
        return connectionSnapshot.getFlowFilesQueued() != null && 
               connectionSnapshot.getFlowFilesQueued() > 0;
    }

    private QueueConnectionInfo createQueueConnectionInfo(ConnectionStatusEntity connection) {
        ConnectionStatusSnapshot snapshot = connection.getConnectionStatusSnapshot();
        return QueueConnectionInfo.builder()
            .connectionId(snapshot.getId())
            .sourceProcessor(snapshot.getSourceName())
            .destinationProcessor(snapshot.getDestinationName())
            .queuedCount(snapshot.getFlowFilesQueued())
            .queuedSize(snapshot.getQueuedSize())
            .build();
    }

    /**
     * Extracts information about stopped processors
     */
    private List<ProcessorStateInfo> extractStoppedProcessors(ProcessGroupSnapshot snapshot) {
        return snapshot.getProcessorStatusSnapshots().stream()
            .map(proc -> proc.getProcessorStatusSnapshot())
            .filter(proc -> "STOPPED".equalsIgnoreCase(proc.getRunStatus()))
            .map(this::createProcessorStateInfo)
            .collect(Collectors.toList());
    }

    /**
     * Extracts information about processors in non-running states (excluding STOPPED)
     */
    private List<ProcessorStateInfo> extractNonRunningProcessors(ProcessGroupSnapshot snapshot) {
        return snapshot.getProcessorStatusSnapshots().stream()
            .map(proc -> proc.getProcessorStatusSnapshot())
            .filter(proc -> !isProcessorRunning(proc))
            .filter(proc -> !"STOPPED".equalsIgnoreCase(proc.getRunStatus()))
            .map(this::createProcessorStateInfo)
            .collect(Collectors.toList());
    }

    private boolean isProcessorRunning(ProcessorStatusSnapshot processor) {
        return "RUNNING".equalsIgnoreCase(processor.getRunStatus());
    }

    private ProcessorStateInfo createProcessorStateInfo(ProcessorStatusSnapshot processor) {
        return ProcessorStateInfo.builder()
            .processorId(processor.getId())
            .processorName(processor.getName())
            .processorType(processor.getType())
            .runStatus(processor.getRunStatus())
            .build();
    }

    /**
     * Extracts transfer metrics for all processors
     */
    private List<ProcessorTransferInfo> extractProcessorTransfers(ProcessGroupSnapshot snapshot) {
        return snapshot.getProcessorStatusSnapshots().stream()
            .map(proc -> createProcessorTransferInfo(proc.getProcessorStatusSnapshot()))
            .collect(Collectors.toList());
    }

     /**
     * Creates processor timing information with human-readable format
     */
    private ProcessorTimingInfo createProcessorTimingInfo(ProcessorStatusSnapshot processor) {
        long nanos = processor.getTasksDurationNanos() != null ? processor.getTasksDurationNanos() : 0L;
        long tasks = processor.getTaskCount() != null ? processor.getTaskCount() : 0L;
        
        // Convert to readable formats
        double seconds = nanos / 1_000_000_000.0;
        double avgTaskMs = tasks > 0 ? (nanos / 1_000_000.0) / tasks : 0.0;
        
        return ProcessorTimingInfo.builder()
                .tasksDurationNanos(nanos)
                .processingTimeSeconds(seconds)
                .taskCount(tasks)
                .averageTaskDurationMs(avgTaskMs)
                .formattedProcessingTime(formatProcessingDuration(nanos))
                .build();
    }
    
    private String formatProcessingDuration(long nanos) {
        if (nanos == 0) return "0 ms";
        
        long ms = nanos / 1_000_000;
        if (ms < 1000) return ms + " ms";
        
        long seconds = ms / 1000;
        if (seconds < 60) return df.format(seconds) + " seconds";
        
        long minutes = seconds / 60;
        seconds = seconds % 60;
        if (minutes < 60) {
            return String.format("%d minutes %d seconds", minutes, seconds);
        }
        
        long hours = minutes / 60;
        minutes = minutes % 60;
        return String.format("%d hours %d minutes", hours, minutes);
    }
    


    private ProcessorTransferInfo createProcessorTransferInfo(ProcessorStatusSnapshot processor) {
        return ProcessorTransferInfo.builder()
                .processorId(processor.getId())
                .processorName(processor.getName())
                .processorType(processor.getType())
                .input(createTransferMetrics(
                        processor.getFlowFilesIn(),
                        processor.getBytesIn()))
                .output(createTransferMetrics(
                        processor.getFlowFilesOut(),
                        processor.getBytesOut()))
                .timing(createProcessorTimingInfo(processor))
                .build();
    }

    /**
     * Creates transfer metrics with appropriate size unit based on the size
     */
    private TransferMetrics createTransferMetrics(long flowFiles, Long bytes) {
        long byteSize = (bytes != null ? bytes : 0);
        
        // Choose appropriate unit based on size
        if (byteSize < BYTES_TO_KB) { // Less than 1KB
            return TransferMetrics.builder()
                .flowFileCount(flowFiles)
                .size(byteSize)
                .sizeUnit("bytes")
                .formattedSize(byteSize + " bytes")
                .build();
        } 
        else if (byteSize < BYTES_TO_MB) { // Less than 1MB
            double kbSize = byteSize / BYTES_TO_KB;
            return TransferMetrics.builder()
                .flowFileCount(flowFiles)
                .size(kbSize)
                .sizeUnit("KB")
                .formattedSize(df.format(kbSize) + " KB")
                .build();
        }
        else { // 1MB or greater
            double mbSize = byteSize / BYTES_TO_MB;
            return TransferMetrics.builder()
                .flowFileCount(flowFiles)
                .size(mbSize)
                .sizeUnit("MB")
                .formattedSize(df.format(mbSize) + " MB")
                .build();
        }
    }

    /**
     * Calculates total transfer metrics for the process group
     */
    private TransferMetrics calculateGroupTotalTransfers(ProcessGroupSnapshot snapshot) {
        long totalFlowFiles = snapshot.getFlowFilesIn() + snapshot.getFlowFilesOut() +
                            snapshot.getFlowFilesTransferred() + snapshot.getFlowFilesReceived() +
                            snapshot.getFlowFilesSent();
        
        long totalBytes = (snapshot.getBytesIn() != null ? snapshot.getBytesIn() : 0L) +
                         (snapshot.getBytesOut() != null ? snapshot.getBytesOut() : 0L) +
                         (snapshot.getBytesTransferred() != null ? snapshot.getBytesTransferred() : 0L) +
                         (snapshot.getBytesReceived() != null ? snapshot.getBytesReceived() : 0L) +
                         (snapshot.getBytesSent() != null ? snapshot.getBytesSent() : 0L);

        return createTransferMetrics(totalFlowFiles, totalBytes);
    }

    /**
     * Logs analysis results
     */
    private void logAnalysisResults(ProcessGroupAnalysis analysis) {
        log.info("Analysis completed for process group: {} ({})", 
            analysis.getProcessGroupName(), 
            analysis.getProcessGroupId());
        
        log.info("Queue Status: {} connections with queued data", 
            analysis.getQueueConnections().size());
        
        log.info("Processor Status: {} stopped, {} other non-running", 
            analysis.getStoppedProcessors().size(),
            analysis.getNotRunningProcessors().size());
        
        log.info("Total Transfer: {} flow files, {}", 
            analysis.getGroupTotalTransfer().getFlowFileCount(),
            analysis.getGroupTotalTransfer().getFormattedSize());

        // Log details of queued connections
        analysis.getQueueConnections().forEach(queue -> 
            log.debug("Queued connection: {} -> {}: {} files, {}", 
                queue.getSourceProcessor(),
                queue.getDestinationProcessor(),
                queue.getQueuedCount(),
                queue.getQueuedSize())
        );

        // Log non-running processors
        analysis.getStoppedProcessors().forEach(proc ->
            log.debug("Stopped processor: {} ({})", 
                proc.getProcessorName(),
                proc.getProcessorType())
        );
    }
    
    
}
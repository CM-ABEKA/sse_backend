package com.sensys.sse_engine.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessGroupSnapshot implements NiFiStatusDTO {
    private String id;
    private String name;
    private List<ConnectionStatusEntity> connectionStatusSnapshots = new ArrayList<>();
    private List<ProcessorStatusEntity> processorStatusSnapshots = new ArrayList<>();
    private List<ProcessGroupStatusEntity> processGroupStatusSnapshots = new ArrayList<>();
    private List<RemoteProcessGroupStatusEntity> remoteProcessGroupStatusSnapshots = new ArrayList<>();
    private List<PortStatusEntity> inputPortStatusSnapshots = new ArrayList<>();
    private List<PortStatusEntity> outputPortStatusSnapshots = new ArrayList<>();
    
    // Aggregate metrics
    private Integer flowFilesIn;
    private Long bytesIn;
    private String input;
    private Integer flowFilesQueued;
    private Long bytesQueued;
    private String queued;
    private String queuedCount;
    private String queuedSize;
    private Long bytesRead;
    private String read;
    private Long bytesWritten;
    private String written;
    private Integer flowFilesOut;
    private Long bytesOut;
    private String output;
    private Integer flowFilesTransferred;
    private Long bytesTransferred;
    private String transferred;
    private Long bytesReceived;
    private Integer flowFilesReceived;
    private String received;
    private Long bytesSent;
    private Integer flowFilesSent;
    private String sent;
    private Integer activeThreadCount;
    private Integer terminatedThreadCount;
    private Integer statelessActiveThreadCount;
    private Long processingNanos;
}
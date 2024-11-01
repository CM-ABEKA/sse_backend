package com.sensys.sse_engine.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessorStatusSnapshot implements NiFiStatusDTO {
    private String id;
    private String groupId;
    private String name;
    private String type;
    private String runStatus;
    private String executionNode;
    private Long bytesRead;
    private Long bytesWritten;
    private String read;
    private String written;
    private Integer flowFilesIn;
    private Long bytesIn;
    private String input;
    private Integer flowFilesOut;
    private Long bytesOut;
    private String output;
    private Integer taskCount;
    private Long tasksDurationNanos;
    private String tasks;
    private String tasksDuration;
    private Integer activeThreadCount;
    private Integer terminatedThreadCount;
}
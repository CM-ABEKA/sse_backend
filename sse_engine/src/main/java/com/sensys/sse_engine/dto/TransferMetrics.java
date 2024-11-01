package com.sensys.sse_engine.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransferMetrics {
    private long flowFileCount;
    private double size;
    private String sizeUnit;  // "bytes", "KB", or "MB"
    private String formattedSize;
}
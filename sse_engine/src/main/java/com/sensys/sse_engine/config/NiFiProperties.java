package com.sensys.sse_engine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "nifi")
public class NiFiProperties {
    private String url;
    private String username;
    private String password;
    private int timeout = 30;
    private int retryAttempts = 3;
    private long retryDelay = 1000;
}
package com.sensys.sse_engine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NiFiClientException extends RuntimeException {
    private final HttpStatus statusCode;
    
    public NiFiClientException(String message) {
        super(message);
        this.statusCode = HttpStatus.BAD_REQUEST;
    }
    
    public HttpStatus getStatusCode() {
        return statusCode;
    }
}

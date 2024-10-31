package com.sensys.sse_engine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class NiFiServerException extends RuntimeException {
    public NiFiServerException(String message) {
        super(message);
    }
}
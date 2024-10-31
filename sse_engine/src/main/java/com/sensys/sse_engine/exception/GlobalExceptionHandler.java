package com.sensys.sse_engine.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NiFiClientException.class)
    public ResponseEntity<String> handleNiFiClientException(NiFiClientException ex) {
        log.error("NiFi client error: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode())
            .body(ex.getMessage());
    }

    @ExceptionHandler(NiFiServerException.class)
    public ResponseEntity<String> handleNiFiServerException(NiFiServerException ex) {
        log.error("NiFi server error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ex.getMessage());
    }

    @ExceptionHandler(DatabaseConfigException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseConfigException(DatabaseConfigException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getStatus(), e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(e.getStatus()));
    }

    public static class ErrorResponse {
        private final int status;
        private final String message;

        public ErrorResponse(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}

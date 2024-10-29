package com.sensys.sse_engine.exception;

public class DatabaseConfigException extends RuntimeException {
    private final int status;
    private final String message;

    public DatabaseConfigException(int status, String message) {
        super(message);
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

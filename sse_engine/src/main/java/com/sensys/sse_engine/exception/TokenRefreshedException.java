// src/main/java/com/sensys/sse_engine/exception/TokenRefreshedException.java
package com.sensys.sse_engine.exception;

public class TokenRefreshedException extends RuntimeException {
    public TokenRefreshedException() {
        super("Token refreshed, please retry the request");
    }
}
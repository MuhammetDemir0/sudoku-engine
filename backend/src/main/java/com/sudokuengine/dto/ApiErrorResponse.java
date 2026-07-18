package com.sudokuengine.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error response payload for API failures.
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors) {

    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status, error, message, path, Map.of());
    }

    public static ApiErrorResponse validation(
            int status,
            String error,
            String message,
            String path,
            Map<String, String> validationErrors) {
        return new ApiErrorResponse(
                Instant.now(),
                status,
                error,
                message,
                path,
                Map.copyOf(validationErrors));
    }
}

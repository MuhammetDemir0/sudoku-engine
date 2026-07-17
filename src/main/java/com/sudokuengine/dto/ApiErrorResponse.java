package com.sudokuengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error response payload for API failures.
 */
@Schema(description = "Common API error response.")
public record ApiErrorResponse(
        @Schema(description = "UTC timestamp when the error response was created.", example = "2026-07-17T10:15:30Z")
        Instant timestamp,
        @Schema(description = "HTTP status code.", example = "400")
        int status,
        @Schema(description = "HTTP reason phrase.", example = "Bad Request")
        String error,
        @Schema(description = "Safe client-facing error message.", example = "Request validation failed.")
        String message,
        @Schema(description = "Request path that produced the error.", example = "/api/v1/puzzles/generate")
        String path,
        @Schema(description = "Field-level validation errors keyed by request property path.")
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

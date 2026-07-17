package com.sudokuengine.dto;

import com.sudokuengine.model.SolveResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * API-safe solver metrics payload.
 */
@Schema(description = "Performance metrics collected during a solver run.")
public record SolverMetricsResponse(
        @Schema(description = "Number of solver nodes visited.", example = "51")
        int visitedNodes,
        @Schema(description = "Number of backtracking events.", example = "0")
        int backtracks,
        @Schema(description = "Deepest recursion depth reached.", example = "51")
        int maxRecursionDepth,
        @Schema(description = "Solver runtime in nanoseconds.", example = "1200000")
        long elapsedNanos) {

    public static SolverMetricsResponse fromDomain(SolveResult.Metrics metrics) {
        return new SolverMetricsResponse(
                metrics.getVisitedNodes(),
                metrics.getBacktracks(),
                metrics.getMaxRecursionDepth(),
                metrics.getElapsedNanos());
    }
}

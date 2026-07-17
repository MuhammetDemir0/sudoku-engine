package com.sudokuengine.dto;

import com.sudokuengine.model.SolveResult;

/**
 * API-safe solver metrics payload.
 */
public record SolverMetricsResponse(
        int visitedNodes,
        int backtracks,
        int maxRecursionDepth,
        long elapsedNanos) {

    public static SolverMetricsResponse fromDomain(SolveResult.Metrics metrics) {
        return new SolverMetricsResponse(
                metrics.getVisitedNodes(),
                metrics.getBacktracks(),
                metrics.getMaxRecursionDepth(),
                metrics.getElapsedNanos());
    }
}

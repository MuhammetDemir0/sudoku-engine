package com.sudokuengine.dto;

import com.sudokuengine.model.SolveResult;

import java.util.List;

/**
 * Response payload for a solver run.
 */
public record SolvePuzzleResponse(
        boolean solved,
        List<List<Integer>> board,
        SolverMetricsResponse metrics,
        List<SolveStepResponse> steps) {

    public static SolvePuzzleResponse fromDomain(SolveResult result) {
        List<List<Integer>> solvedBoard = result.getBoard()
                .map(BoardDtoMapper::fromDomain)
                .orElse(null);
        List<SolveStepResponse> solveSteps = result.getSteps()
                .map(steps -> steps.stream()
                        .map(SolveStepResponse::fromDomain)
                        .toList())
                .orElse(List.of());

        return new SolvePuzzleResponse(
                result.isSolved(),
                solvedBoard,
                SolverMetricsResponse.fromDomain(result.getMetrics()),
                solveSteps);
    }
}

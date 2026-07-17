package com.sudokuengine.dto;

import com.sudokuengine.model.SolveResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response payload for a solver run.
 */
@Schema(description = "Solver result and performance metrics.")
public record SolvePuzzleResponse(
        @Schema(description = "Whether the solver found a valid solution.", example = "true")
        boolean solved,
        @Schema(
                description = "Solved board when a solution exists; null when unsolved.",
                nullable = true,
                example = "[[5,3,4,6,7,8,9,1,2],[6,7,2,1,9,5,3,4,8],[1,9,8,3,4,2,5,6,7],[8,5,9,7,6,1,4,2,3],[4,2,6,8,5,3,7,9,1],[7,1,3,9,2,4,8,5,6],[9,6,1,5,3,7,2,8,4],[2,8,7,4,1,9,6,3,5],[3,4,5,2,8,6,1,7,9]]")
        List<List<Integer>> board,
        @Schema(description = "Solver performance metrics.")
        SolverMetricsResponse metrics,
        @Schema(description = "Solver visualization steps. Empty unless includeSteps is true.")
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

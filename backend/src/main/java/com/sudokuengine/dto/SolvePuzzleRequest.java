package com.sudokuengine.dto;

import com.sudokuengine.model.SudokuBoard;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request payload for solving a puzzle.
 */
public record SolvePuzzleRequest(
        @NotNull
        List<List<Integer>> board,
        Boolean includeSteps,
        SolverType solver) {

    @AssertTrue(message = "board must be a 9x9 matrix with values between 0 and 9")
    public boolean isBoardValid() {
        return board == null || BoardDtoMapper.isValidPayload(board);
    }

    public SudokuBoard toBoard() {
        return BoardDtoMapper.toDomain(board);
    }

    public boolean shouldIncludeSteps() {
        return Boolean.TRUE.equals(includeSteps);
    }

    public SolverType requestedSolver() {
        return solver == null ? SolverType.MRV : solver;
    }
}

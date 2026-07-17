package com.sudokuengine.dto;

import com.sudokuengine.model.SudokuBoard;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request payload for solving a puzzle.
 */
public record SolvePuzzleRequest(
        @NotNull
        @Size(min = SudokuBoard.SIZE, max = SudokuBoard.SIZE)
        List<@NotNull @Size(min = SudokuBoard.SIZE, max = SudokuBoard.SIZE) List<@NotNull @Min(0) @Max(9) Integer>> board,
        Boolean includeSteps,
        SolverType solver) {

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

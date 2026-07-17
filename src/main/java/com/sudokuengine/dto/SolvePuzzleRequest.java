package com.sudokuengine.dto;

import com.sudokuengine.model.SudokuBoard;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request payload for solving a puzzle.
 */
@Schema(description = "Request payload for solving a Sudoku board.")
public record SolvePuzzleRequest(
        @Schema(
                description = "9x9 Sudoku board. Use 0 for empty cells.",
                example = "[[5,3,0,0,7,0,0,0,0],[6,0,0,1,9,5,0,0,0],[0,9,8,0,0,0,0,6,0],[8,0,0,0,6,0,0,0,3],[4,0,0,8,0,3,0,0,1],[7,0,0,0,2,0,0,0,6],[0,6,0,0,0,0,2,8,0],[0,0,0,4,1,9,0,0,5],[0,0,0,0,8,0,0,7,9]]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Size(min = SudokuBoard.SIZE, max = SudokuBoard.SIZE)
        List<@NotNull @Size(min = SudokuBoard.SIZE, max = SudokuBoard.SIZE) List<@NotNull @Min(0) @Max(9) Integer>> board,
        @Schema(description = "When true, includes solver visualization steps.", example = "true")
        Boolean includeSteps,
        @Schema(description = "Solver implementation to use. Defaults to MRV.", example = "MRV")
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

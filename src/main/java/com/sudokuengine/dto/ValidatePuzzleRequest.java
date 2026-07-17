package com.sudokuengine.dto;

import com.sudokuengine.model.SudokuBoard;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request payload for board validation.
 */
@Schema(description = "Request payload for validating Sudoku rule violations.")
public record ValidatePuzzleRequest(
        @Schema(
                description = "9x9 Sudoku board. Use 0 for empty cells.",
                example = "[[5,5,0,0,7,0,0,0,0],[6,0,0,1,9,5,0,0,0],[0,9,8,0,0,0,0,6,0],[8,0,0,0,6,0,0,0,3],[4,0,0,8,0,3,0,0,1],[7,0,0,0,2,0,0,0,6],[0,6,0,0,0,0,2,8,0],[0,0,0,4,1,9,0,0,5],[0,0,0,0,8,0,0,7,9]]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Size(min = SudokuBoard.SIZE, max = SudokuBoard.SIZE)
        List<@NotNull @Size(min = SudokuBoard.SIZE, max = SudokuBoard.SIZE) List<@NotNull @Min(0) @Max(9) Integer>> board) {

    public SudokuBoard toBoard() {
        return BoardDtoMapper.toDomain(board);
    }
}

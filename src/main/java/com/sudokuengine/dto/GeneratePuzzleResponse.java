package com.sudokuengine.dto;

import com.sudokuengine.model.Difficulty;
import com.sudokuengine.model.SudokuPuzzle;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response payload for a generated puzzle.
 */
@Schema(description = "Generated puzzle response. The solution is not exposed.")
public record GeneratePuzzleResponse(
        @Schema(
                description = "Generated 9x9 puzzle. Empty cells are represented by 0.",
                example = "[[5,3,0,0,7,0,0,0,0],[6,0,0,1,9,5,0,0,0],[0,9,8,0,0,0,0,6,0],[8,0,0,0,6,0,0,0,3],[4,0,0,8,0,3,0,0,1],[7,0,0,0,2,0,0,0,6],[0,6,0,0,0,0,2,8,0],[0,0,0,4,1,9,0,0,5],[0,0,0,0,8,0,0,7,9]]")
        List<List<Integer>> puzzle,
        @Schema(description = "Difficulty used for generation.", example = "MEDIUM")
        Difficulty difficulty) {

    public static GeneratePuzzleResponse fromDomain(SudokuPuzzle puzzle, Difficulty difficulty) {
        return new GeneratePuzzleResponse(
                BoardDtoMapper.fromDomain(puzzle.getPuzzle()),
                difficulty);
    }
}

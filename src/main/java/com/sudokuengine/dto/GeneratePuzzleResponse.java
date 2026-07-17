package com.sudokuengine.dto;

import com.sudokuengine.model.Difficulty;
import com.sudokuengine.model.SudokuPuzzle;

import java.util.List;

/**
 * Response payload for a generated puzzle.
 */
public record GeneratePuzzleResponse(
        List<List<Integer>> puzzle,
        List<List<Integer>> solution,
        Difficulty difficulty) {

    public static GeneratePuzzleResponse fromDomain(SudokuPuzzle puzzle, Difficulty difficulty) {
        return new GeneratePuzzleResponse(
                BoardDtoMapper.fromDomain(puzzle.getPuzzle()),
                BoardDtoMapper.fromDomain(puzzle.getSolution()),
                difficulty);
    }
}

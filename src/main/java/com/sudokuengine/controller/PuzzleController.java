package com.sudokuengine.controller;

import com.sudokuengine.dto.GeneratePuzzleRequest;
import com.sudokuengine.dto.GeneratePuzzleResponse;
import com.sudokuengine.model.SudokuPuzzle;
import com.sudokuengine.service.UniqueSudokuPuzzleGenerator;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for Sudoku puzzle generation.
 */
@RestController
@RequestMapping("/api/v1/puzzles")
public class PuzzleController {

    private final UniqueSudokuPuzzleGenerator puzzleGenerator;

    public PuzzleController(UniqueSudokuPuzzleGenerator puzzleGenerator) {
        this.puzzleGenerator = puzzleGenerator;
    }

    @PostMapping("/generate")
    public ResponseEntity<GeneratePuzzleResponse> generate(@Valid @RequestBody GeneratePuzzleRequest request) {
        SudokuPuzzle puzzle = puzzleGenerator.generate(request.difficulty());
        return ResponseEntity.ok(GeneratePuzzleResponse.fromDomain(puzzle, request.difficulty()));
    }
}

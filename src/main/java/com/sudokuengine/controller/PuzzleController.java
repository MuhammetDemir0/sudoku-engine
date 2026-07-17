package com.sudokuengine.controller;

import com.sudokuengine.dto.GeneratePuzzleRequest;
import com.sudokuengine.dto.GeneratePuzzleResponse;
import com.sudokuengine.dto.SolvePuzzleRequest;
import com.sudokuengine.dto.SolvePuzzleResponse;
import com.sudokuengine.dto.SolverType;
import com.sudokuengine.dto.ValidatePuzzleRequest;
import com.sudokuengine.dto.ValidatePuzzleResponse;
import com.sudokuengine.model.SolveResult;
import com.sudokuengine.model.SudokuPuzzle;
import com.sudokuengine.service.BacktrackingSudokuSolver;
import com.sudokuengine.service.MrvSudokuSolver;
import com.sudokuengine.service.SudokuValidator;
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
    private final BacktrackingSudokuSolver backtrackingSolver;
    private final MrvSudokuSolver mrvSolver;
    private final SudokuValidator validator;

    public PuzzleController(
            UniqueSudokuPuzzleGenerator puzzleGenerator,
            BacktrackingSudokuSolver backtrackingSolver,
            MrvSudokuSolver mrvSolver,
            SudokuValidator validator) {
        this.puzzleGenerator = puzzleGenerator;
        this.backtrackingSolver = backtrackingSolver;
        this.mrvSolver = mrvSolver;
        this.validator = validator;
    }

    @PostMapping("/generate")
    public ResponseEntity<GeneratePuzzleResponse> generate(@Valid @RequestBody GeneratePuzzleRequest request) {
        SudokuPuzzle puzzle = puzzleGenerator.generate(request.difficulty());
        return ResponseEntity.ok(GeneratePuzzleResponse.fromDomain(puzzle, request.difficulty()));
    }

    @PostMapping("/solve")
    public ResponseEntity<SolvePuzzleResponse> solve(@Valid @RequestBody SolvePuzzleRequest request) {
        SolveResult result = switch (request.requestedSolver()) {
            case BACKTRACKING -> backtrackingSolver.solve(request.toBoard(), request.shouldIncludeSteps());
            case MRV -> mrvSolver.solve(request.toBoard(), request.shouldIncludeSteps());
        };
        return ResponseEntity.ok(SolvePuzzleResponse.fromDomain(result));
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidatePuzzleResponse> validate(@Valid @RequestBody ValidatePuzzleRequest request) {
        return ResponseEntity.ok(ValidatePuzzleResponse.fromDomain(validator.validate(request.toBoard())));
    }
}

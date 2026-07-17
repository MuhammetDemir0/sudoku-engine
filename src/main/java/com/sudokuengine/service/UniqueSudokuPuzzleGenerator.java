package com.sudokuengine.service;

import com.sudokuengine.exception.PuzzleGenerationException;
import com.sudokuengine.model.SudokuBoard;
import com.sudokuengine.model.SudokuPuzzle;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Generates uniquely solvable Sudoku puzzles by removing values from a full
 * solution board.
 */
@Component
public class UniqueSudokuPuzzleGenerator {

    public static final int DEFAULT_MAX_ATTEMPTS = 200;

    private final SudokuSolutionGenerator solutionGenerator;
    private final BacktrackingSudokuSolver solver;
    private final Random random;
    private final int maxAttempts;

    public UniqueSudokuPuzzleGenerator() {
        this(new SudokuSolutionGenerator(), new BacktrackingSudokuSolver(), new Random(), DEFAULT_MAX_ATTEMPTS);
    }

    public UniqueSudokuPuzzleGenerator(
            SudokuSolutionGenerator solutionGenerator,
            BacktrackingSudokuSolver solver,
            Random random,
            int maxAttempts) {
        this.solutionGenerator = Objects.requireNonNull(solutionGenerator, "Solution generator cannot be null.");
        this.solver = Objects.requireNonNull(solver, "Solver cannot be null.");
        this.random = Objects.requireNonNull(random, "Random dependency cannot be null.");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("Max attempts must be greater than zero.");
        }
        this.maxAttempts = maxAttempts;
    }

    public SudokuPuzzle generate() {
        SudokuBoard solution = solutionGenerator.generate();
        SudokuBoard puzzle = solution.copy();

        List<Integer> positions = shuffledPositions();

        int attempts = 0;
        for (int position : positions) {
            if (attempts >= maxAttempts) {
                break;
            }

            int row = position / SudokuBoard.SIZE;
            int col = position % SudokuBoard.SIZE;
            int originalValue = puzzle.read(row, col);
            if (originalValue == SudokuBoard.EMPTY) {
                continue;
            }

            attempts++;
            puzzle.write(row, col, SudokuBoard.EMPTY);

            int solutionCount = solver.countSolutions(puzzle, 2);
            if (solutionCount != 1) {
                puzzle.write(row, col, originalValue);
            }
        }

        int emptyCells = countEmptyCells(puzzle);
        if (emptyCells == 0) {
            throw new PuzzleGenerationException(
                    "Failed to generate uniquely solvable puzzle: no removable cells found within max attempts "
                            + maxAttempts + ".");
        }

        int finalSolutionCount = solver.countSolutions(puzzle, 2);
        if (finalSolutionCount != 1) {
            throw new PuzzleGenerationException(
                    "Failed to generate uniquely solvable puzzle: expected exactly one solution but found "
                            + finalSolutionCount + ".");
        }

        return new SudokuPuzzle(puzzle, solution);
    }

    private List<Integer> shuffledPositions() {
        List<Integer> positions = new ArrayList<>(SudokuBoard.SIZE * SudokuBoard.SIZE);
        for (int i = 0; i < SudokuBoard.SIZE * SudokuBoard.SIZE; i++) {
            positions.add(i);
        }
        Collections.shuffle(positions, random);
        return positions;
    }

    private static int countEmptyCells(SudokuBoard board) {
        int emptyCount = 0;
        for (int row = 0; row < SudokuBoard.SIZE; row++) {
            for (int col = 0; col < SudokuBoard.SIZE; col++) {
                if (board.read(row, col) == SudokuBoard.EMPTY) {
                    emptyCount++;
                }
            }
        }
        return emptyCount;
    }
}

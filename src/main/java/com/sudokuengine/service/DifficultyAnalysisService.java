package com.sudokuengine.service;

import com.sudokuengine.config.DifficultyThresholdConfig;
import com.sudokuengine.exception.InvalidBoardException;
import com.sudokuengine.exception.UnsolvablePuzzleException;
import com.sudokuengine.model.Difficulty;
import com.sudokuengine.model.SolveResult;
import com.sudokuengine.model.SudokuBoard;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Calculates puzzle difficulty from clue count and solver effort metrics.
 */
@Component
public class DifficultyAnalysisService {

    private final SudokuSolver solver;
    private final DifficultyThresholdConfig thresholdConfig;

    public DifficultyAnalysisService() {
        this(new MrvSudokuSolver(), DifficultyThresholdConfig.defaults());
    }

    public DifficultyAnalysisService(SudokuSolver solver) {
        this(solver, DifficultyThresholdConfig.defaults());
    }

    public DifficultyAnalysisService(SudokuSolver solver, DifficultyThresholdConfig thresholdConfig) {
        this.solver = Objects.requireNonNull(solver, "Solver cannot be null.");
        this.thresholdConfig = Objects.requireNonNull(thresholdConfig, "Threshold config cannot be null.");
    }

    /**
     * Classifies the puzzle using the harder result between clue-count and
     * solver-metric classifications.
     *
     * @param puzzle puzzle board to classify
     * @return calculated puzzle difficulty
     */
    public Difficulty calculate(SudokuBoard puzzle) {
        if (puzzle == null) {
            throw new InvalidBoardException("Puzzle board cannot be null.");
        }

        Difficulty clueDifficulty = thresholdConfig.classifyByClues(countClues(puzzle));
        SolveResult result = solver.solve(puzzle);
        if (!result.isSolved()) {
            throw new UnsolvablePuzzleException("Puzzle must be solvable to calculate difficulty.");
        }

        SolveResult.Metrics metrics = result.getMetrics();
        Difficulty solverDifficulty = thresholdConfig.classifyBySolverMetrics(
                metrics.getVisitedNodes(),
                metrics.getBacktracks(),
                metrics.getMaxRecursionDepth());

        return harderOf(clueDifficulty, solverDifficulty);
    }

    public int countClues(SudokuBoard board) {
        if (board == null) {
            throw new InvalidBoardException("Puzzle board cannot be null.");
        }

        int clues = 0;
        for (int row = 0; row < SudokuBoard.SIZE; row++) {
            for (int col = 0; col < SudokuBoard.SIZE; col++) {
                if (board.read(row, col) != SudokuBoard.EMPTY) {
                    clues++;
                }
            }
        }
        return clues;
    }

    private static Difficulty harderOf(Difficulty first, Difficulty second) {
        return first.ordinal() >= second.ordinal() ? first : second;
    }
}

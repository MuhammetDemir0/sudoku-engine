package com.sudokuengine.service;

import com.sudokuengine.config.DifficultyThresholdConfig;
import com.sudokuengine.exception.InvalidBoardException;
import com.sudokuengine.exception.UnsolvablePuzzleException;
import com.sudokuengine.model.Difficulty;
import com.sudokuengine.model.SolveResult;
import com.sudokuengine.model.SudokuBoard;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DifficultyAnalysisServiceTest {

    @Test
    void calculatesDifficultyFromClueCountAndSolverMetrics() {
        DifficultyAnalysisService service = new DifficultyAnalysisService(
                new StubSolver(new SolveResult.Metrics(90, 0, 45, 0)));

        Difficulty difficulty = service.calculate(puzzleWithClues(40));

        assertEquals(Difficulty.MEDIUM, difficulty);
    }

    @Test
    void returnsHardestSignalBetweenCluesAndSolverMetrics() {
        DifficultyAnalysisService service = new DifficultyAnalysisService(
                new StubSolver(new SolveResult.Metrics(10, 0, 20, 0)));

        Difficulty difficulty = service.calculate(puzzleWithClues(27));

        assertEquals(Difficulty.EXPERT, difficulty);
    }

    @Test
    void countsNonEmptyCellsAsClues() {
        DifficultyAnalysisService service = new DifficultyAnalysisService(
                new StubSolver(new SolveResult.Metrics(0, 0, 0, 0)));

        assertEquals(32, service.countClues(puzzleWithClues(32)));
    }

    @Test
    void rejectsNullPuzzle() {
        DifficultyAnalysisService service = new DifficultyAnalysisService(
                new StubSolver(new SolveResult.Metrics(0, 0, 0, 0)));

        assertThrows(InvalidBoardException.class, () -> service.calculate(null));
    }

    @Test
    void rejectsUnsolvablePuzzle() {
        DifficultyAnalysisService service = new DifficultyAnalysisService(new UnsolvedStubSolver());

        assertThrows(UnsolvablePuzzleException.class, () -> service.calculate(puzzleWithClues(20)));
    }

    @Test
    void rejectsNullConstructorDependencies() {
        assertThrows(NullPointerException.class, () -> new DifficultyAnalysisService(null));
        assertThrows(NullPointerException.class,
                () -> new DifficultyAnalysisService(
                        new StubSolver(new SolveResult.Metrics(0, 0, 0, 0)),
                        null));
    }

    @Test
    void rejectsInvalidThresholdConfiguration() {
        assertThrows(NullPointerException.class, () -> new DifficultyThresholdConfig(null));
        assertThrows(IllegalArgumentException.class,
                () -> new DifficultyThresholdConfig(Map.of(
                        Difficulty.EASY, new DifficultyThresholdConfig.Thresholds(36, 45, 80, 0, 45))));
        assertThrows(IllegalArgumentException.class,
                () -> new DifficultyThresholdConfig.Thresholds(40, 39, 80, 0, 45));
    }

    private static SudokuBoard puzzleWithClues(int clues) {
        int[][] cells = new int[SudokuBoard.SIZE][SudokuBoard.SIZE];
        for (int index = 0; index < clues; index++) {
            cells[index / SudokuBoard.SIZE][index % SudokuBoard.SIZE] = 1;
        }
        return new SudokuBoard(cells);
    }

    private static final class StubSolver implements SudokuSolver {
        private final SolveResult.Metrics metrics;

        private StubSolver(SolveResult.Metrics metrics) {
            this.metrics = metrics;
        }

        @Override
        public SolveResult solveInternal(SudokuBoard workingBoard) {
            return SolveResult.solved(workingBoard, metrics);
        }
    }

    private static final class UnsolvedStubSolver implements SudokuSolver {
        @Override
        public SolveResult solveInternal(SudokuBoard workingBoard) {
            return SolveResult.unsolved(new SolveResult.Metrics(3, 1, 2, 0));
        }
    }
}

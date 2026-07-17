package com.sudokuengine.service;

import com.sudokuengine.exception.PuzzleGenerationException;
import com.sudokuengine.config.DifficultyThresholdConfig;
import com.sudokuengine.model.Difficulty;
import com.sudokuengine.model.SudokuBoard;
import com.sudokuengine.model.SudokuPuzzle;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UniqueSudokuPuzzleGeneratorTest {

    private final BacktrackingSudokuSolver solver = new BacktrackingSudokuSolver();

    @Test
    void generatedPuzzleContainsEmptyCellsAndHasExactlyOneSolution() {
        UniqueSudokuPuzzleGenerator generator = new UniqueSudokuPuzzleGenerator(
                new SudokuSolutionGenerator(new Random(11L)),
                new BacktrackingSudokuSolver(),
                new Random(11L),
                200);

        SudokuPuzzle generated = generator.generate();
        SudokuBoard puzzle = generated.getPuzzle();

        assertTrue(hasEmptyCells(puzzle));
        assertTrue(solver.countSolutions(puzzle, 2) == 1);
    }

    @Test
    void generatedPuzzleCanTargetRequestedDifficulty() {
        DifficultyThresholdConfig thresholdConfig = DifficultyThresholdConfig.defaults();
        ClueOnlyDifficultyAnalysisService difficultyAnalysisService =
                new ClueOnlyDifficultyAnalysisService(thresholdConfig);
        UniqueSudokuPuzzleGenerator generator = new UniqueSudokuPuzzleGenerator(
                new SudokuSolutionGenerator(new Random(19L)),
                new BacktrackingSudokuSolver(),
                difficultyAnalysisService,
                thresholdConfig,
                new Random(19L),
                200);

        SudokuPuzzle generated = generator.generate(Difficulty.EASY);
        SudokuBoard puzzle = generated.getPuzzle();
        int clues = difficultyAnalysisService.countClues(puzzle);

        assertTrue(hasEmptyCells(puzzle));
        assertTrue(solver.countSolutions(puzzle, 2) == 1);
        assertTrue(clues >= thresholdConfig.get(Difficulty.EASY).minimumClues());
        assertTrue(clues <= thresholdConfig.get(Difficulty.EASY).generationTargetMaximumClues());
    }

    @Test
    void retainedSolutionMatchesSolvedPuzzle() {
        UniqueSudokuPuzzleGenerator generator = new UniqueSudokuPuzzleGenerator(
                new SudokuSolutionGenerator(new Random(42L)),
                new BacktrackingSudokuSolver(),
                new Random(42L),
                200);

        SudokuPuzzle generated = generator.generate();
        SudokuBoard puzzle = generated.getPuzzle();
        SudokuBoard retainedSolution = generated.getSolution();
        SudokuBoard solved = solver.solve(puzzle).getBoard().orElseThrow();

        assertArrayEquals(retainedSolution.toMatrix(), solved.toMatrix());
    }

    @Test
    void maxAttemptFailureIsMeaningfulAndFinite() {
        BacktrackingSudokuSolver alwaysNonUniqueSolver = new BacktrackingSudokuSolver() {
            @Override
            public int countSolutions(SudokuBoard board, int limit) {
                return 2;
            }
        };

        UniqueSudokuPuzzleGenerator generator = new UniqueSudokuPuzzleGenerator(
                new SudokuSolutionGenerator(new Random(7L)),
                alwaysNonUniqueSolver,
                new Random(7L),
                1);

        PuzzleGenerationException ex = org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(
                Duration.ofSeconds(1),
                () -> assertThrows(PuzzleGenerationException.class, generator::generate));
        assertTrue(ex.getMessage().contains("max attempts") || ex.getMessage().contains("exactly one solution"));
    }

    private static boolean hasEmptyCells(SudokuBoard board) {
        for (int row = 0; row < SudokuBoard.SIZE; row++) {
            for (int col = 0; col < SudokuBoard.SIZE; col++) {
                if (board.read(row, col) == SudokuBoard.EMPTY) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final class ClueOnlyDifficultyAnalysisService extends DifficultyAnalysisService {
        private final DifficultyThresholdConfig thresholdConfig;

        private ClueOnlyDifficultyAnalysisService(DifficultyThresholdConfig thresholdConfig) {
            this.thresholdConfig = thresholdConfig;
        }

        @Override
        public Difficulty calculate(SudokuBoard puzzle) {
            return thresholdConfig.classifyByClues(countClues(puzzle));
        }
    }
}

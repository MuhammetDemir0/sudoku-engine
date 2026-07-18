package com.sudokuengine.service;

import com.sudokuengine.exception.InvalidBoardException;
import com.sudokuengine.model.SolveResult;
import com.sudokuengine.model.SolveStep;
import com.sudokuengine.model.SolveStepType;
import com.sudokuengine.model.SudokuBoard;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BacktrackingSudokuSolverTest {

    private final BacktrackingSudokuSolver solver = new BacktrackingSudokuSolver();
    private final SudokuValidator validator = new SudokuValidator();

    @Test
    void solvableBoardIsSolvedCorrectly() {
        SudokuBoard puzzle = new SudokuBoard(new int[][] {
                { 5, 3, 0, 0, 7, 0, 0, 0, 0 },
                { 6, 0, 0, 1, 9, 5, 0, 0, 0 },
                { 0, 9, 8, 0, 0, 0, 0, 6, 0 },
                { 8, 0, 0, 0, 6, 0, 0, 0, 3 },
                { 4, 0, 0, 8, 0, 3, 0, 0, 1 },
                { 7, 0, 0, 0, 2, 0, 0, 0, 6 },
                { 0, 6, 0, 0, 0, 0, 2, 8, 0 },
                { 0, 0, 0, 4, 1, 9, 0, 0, 5 },
                { 0, 0, 0, 0, 8, 0, 0, 7, 9 }
        });

        SolveResult result = solver.solve(puzzle);

        assertTrue(result.isSolved());
        SudokuBoard solvedBoard = result.getBoard().orElseThrow();
        assertTrue(validator.isValid(solvedBoard));
        assertFalse(hasEmptyCell(solvedBoard));

        assertTrue(result.getMetrics().getVisitedNodes() > 0);
        assertTrue(result.getMetrics().getMaxRecursionDepth() > 0);
        assertTrue(result.getMetrics().getElapsedNanos() >= 0);
    }

    @Test
    void unsolvableBoardReturnsUnsuccessfulResult() {
        SudokuBoard unsolvable = new SudokuBoard(new int[][] {
                { 0, 3, 4, 6, 7, 8, 9, 1, 2 },
                { 5, 7, 2, 1, 9, 0, 3, 4, 8 },
                { 1, 9, 8, 3, 4, 2, 5, 6, 7 },
                { 8, 5, 9, 7, 6, 1, 4, 2, 3 },
                { 4, 2, 6, 8, 5, 3, 7, 9, 1 },
                { 7, 1, 3, 9, 2, 4, 8, 5, 6 },
                { 9, 6, 1, 5, 3, 7, 2, 8, 4 },
                { 2, 8, 7, 4, 1, 9, 6, 3, 5 },
                { 3, 4, 5, 2, 8, 6, 1, 7, 9 }
        });

        SolveResult result = solver.solve(unsolvable);

        assertFalse(result.isSolved());
        assertTrue(result.getBoard().isEmpty());
        assertTrue(result.getMetrics().getVisitedNodes() > 0);
    }

    @Test
    void invalidInitialBoardIsRejected() {
        SudokuBoard invalid = new SudokuBoard(new int[][] {
                { 5, 5, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 }
        });

        assertThrows(InvalidBoardException.class, () -> solver.solve(invalid));
    }

    @Test
    void inputBoardIsNotMutated() {
        SudokuBoard puzzle = new SudokuBoard(new int[][] {
                { 5, 3, 0, 0, 7, 0, 0, 0, 0 },
                { 6, 0, 0, 1, 9, 5, 0, 0, 0 },
                { 0, 9, 8, 0, 0, 0, 0, 6, 0 },
                { 8, 0, 0, 0, 6, 0, 0, 0, 3 },
                { 4, 0, 0, 8, 0, 3, 0, 0, 1 },
                { 7, 0, 0, 0, 2, 0, 0, 0, 6 },
                { 0, 6, 0, 0, 0, 0, 2, 8, 0 },
                { 0, 0, 0, 4, 1, 9, 0, 0, 5 },
                { 0, 0, 0, 0, 8, 0, 0, 7, 9 }
        });

        int[][] snapshot = puzzle.toMatrix();

        solver.solve(puzzle);

        assertEquals(snapshot[0][2], puzzle.read(0, 2));
        assertEquals(snapshot[8][0], puzzle.read(8, 0));
        assertEquals(0, puzzle.read(0, 2));
    }

    @Test
    void countSolutionsReturnsZeroForUnsolvableBoard() {
        SudokuBoard unsolvable = new SudokuBoard(new int[][] {
                { 0, 3, 4, 6, 7, 8, 9, 1, 2 },
                { 5, 7, 2, 1, 9, 0, 3, 4, 8 },
                { 1, 9, 8, 3, 4, 2, 5, 6, 7 },
                { 8, 5, 9, 7, 6, 1, 4, 2, 3 },
                { 4, 2, 6, 8, 5, 3, 7, 9, 1 },
                { 7, 1, 3, 9, 2, 4, 8, 5, 6 },
                { 9, 6, 1, 5, 3, 7, 2, 8, 4 },
                { 2, 8, 7, 4, 1, 9, 6, 3, 5 },
                { 3, 4, 5, 2, 8, 6, 1, 7, 9 }
        });

        int count = solver.countSolutions(unsolvable, 2);

        assertEquals(0, count);
    }

    @Test
    void countSolutionsReturnsOneForUniquePuzzle() {
        SudokuBoard puzzle = new SudokuBoard(new int[][] {
                { 5, 3, 0, 0, 7, 0, 0, 0, 0 },
                { 6, 0, 0, 1, 9, 5, 0, 0, 0 },
                { 0, 9, 8, 0, 0, 0, 0, 6, 0 },
                { 8, 0, 0, 0, 6, 0, 0, 0, 3 },
                { 4, 0, 0, 8, 0, 3, 0, 0, 1 },
                { 7, 0, 0, 0, 2, 0, 0, 0, 6 },
                { 0, 6, 0, 0, 0, 0, 2, 8, 0 },
                { 0, 0, 0, 4, 1, 9, 0, 0, 5 },
                { 0, 0, 0, 0, 8, 0, 0, 7, 9 }
        });

        int count = solver.countSolutions(puzzle, 2);

        assertEquals(1, count);
    }

    @Test
    void countSolutionsStopsAfterSecondSolutionWhenLimitIsTwo() {
        SudokuBoard highlyAmbiguous = new SudokuBoard(new int[][] {
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 }
        });

        int count = org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(
                Duration.ofSeconds(1),
                () -> solver.countSolutions(highlyAmbiguous, 2));

        assertEquals(2, count);
    }

    @Test
    void stepTrackingRecordsChronologicalSequenceAndSolutionFound() {
        SudokuBoard puzzle = new SudokuBoard(new int[][] {
                { 5, 3, 0, 0, 7, 0, 0, 0, 0 },
                { 6, 0, 0, 1, 9, 5, 0, 0, 0 },
                { 0, 9, 8, 0, 0, 0, 0, 6, 0 },
                { 8, 0, 0, 0, 6, 0, 0, 0, 3 },
                { 4, 0, 0, 8, 0, 3, 0, 0, 1 },
                { 7, 0, 0, 0, 2, 0, 0, 0, 6 },
                { 0, 6, 0, 0, 0, 0, 2, 8, 0 },
                { 0, 0, 0, 4, 1, 9, 0, 0, 5 },
                { 0, 0, 0, 0, 8, 0, 0, 7, 9 }
        });

        SolveResult result = solver.solve(puzzle, true);
        var steps = result.getSteps().orElseThrow();

        assertFalse(steps.isEmpty());
        for (int i = 0; i < steps.size(); i++) {
            assertEquals(i + 1, steps.get(i).getSequence());
        }
        assertEquals(SolveStepType.SOLUTION_FOUND, steps.get(steps.size() - 1).getType());
    }

    @Test
    void stepTrackingRecordsPlacementAndRemovalActions() {
        SudokuBoard puzzle = new SudokuBoard(new int[][] {
                { 5, 3, 0, 0, 7, 0, 0, 0, 0 },
                { 6, 0, 0, 1, 9, 5, 0, 0, 0 },
                { 0, 9, 8, 0, 0, 0, 0, 6, 0 },
                { 8, 0, 0, 0, 6, 0, 0, 0, 3 },
                { 4, 0, 0, 8, 0, 3, 0, 0, 1 },
                { 7, 0, 0, 0, 2, 0, 0, 0, 6 },
                { 0, 6, 0, 0, 0, 0, 2, 8, 0 },
                { 0, 0, 0, 4, 1, 9, 0, 0, 5 },
                { 0, 0, 0, 0, 8, 0, 0, 7, 9 }
        });

        SolveResult result = solver.solve(puzzle, true);
        var steps = result.getSteps().orElseThrow();

        assertTrue(steps.stream().anyMatch(s -> s.getType() == SolveStepType.PLACE_VALUE));
        assertTrue(steps.stream().anyMatch(s -> s.getType() == SolveStepType.REMOVE_VALUE));
    }

    @Test
    void stepTrackingCanBeDisabled() {
        SudokuBoard puzzle = new SudokuBoard(new int[][] {
                { 5, 3, 0, 0, 7, 0, 0, 0, 0 },
                { 6, 0, 0, 1, 9, 5, 0, 0, 0 },
                { 0, 9, 8, 0, 0, 0, 0, 6, 0 },
                { 8, 0, 0, 0, 6, 0, 0, 0, 3 },
                { 4, 0, 0, 8, 0, 3, 0, 0, 1 },
                { 7, 0, 0, 0, 2, 0, 0, 0, 6 },
                { 0, 6, 0, 0, 0, 0, 2, 8, 0 },
                { 0, 0, 0, 4, 1, 9, 0, 0, 5 },
                { 0, 0, 0, 0, 8, 0, 0, 7, 9 }
        });

        SolveResult withTracking = solver.solve(puzzle, true);
        SolveResult withoutTracking = solver.solve(puzzle, false);

        assertTrue(withTracking.getSteps().isPresent());
        assertTrue(withoutTracking.getSteps().isEmpty());
    }

    @Test
    void trackedStepsContainDepthInformation() {
        SudokuBoard puzzle = new SudokuBoard(new int[][] {
                { 5, 3, 0, 0, 7, 0, 0, 0, 0 },
                { 6, 0, 0, 1, 9, 5, 0, 0, 0 },
                { 0, 9, 8, 0, 0, 0, 0, 6, 0 },
                { 8, 0, 0, 0, 6, 0, 0, 0, 3 },
                { 4, 0, 0, 8, 0, 3, 0, 0, 1 },
                { 7, 0, 0, 0, 2, 0, 0, 0, 6 },
                { 0, 6, 0, 0, 0, 0, 2, 8, 0 },
                { 0, 0, 0, 4, 1, 9, 0, 0, 5 },
                { 0, 0, 0, 0, 8, 0, 0, 7, 9 }
        });

        SolveResult result = solver.solve(puzzle, true);
        for (SolveStep step : result.getSteps().orElseThrow()) {
            assertTrue(step.getDepth() >= 0);
        }
    }

    private static boolean hasEmptyCell(SudokuBoard board) {
        for (int row = 0; row < SudokuBoard.SIZE; row++) {
            for (int col = 0; col < SudokuBoard.SIZE; col++) {
                if (board.read(row, col) == SudokuBoard.EMPTY) {
                    return true;
                }
            }
        }
        return false;
    }
}

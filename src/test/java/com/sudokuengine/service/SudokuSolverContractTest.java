package com.sudokuengine.service;

import com.sudokuengine.model.SolveResult;
import com.sudokuengine.model.SudokuBoard;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SudokuSolverContractTest {

    @Test
    void solveDoesNotMutateInputBoard() {
        SudokuBoard input = new SudokuBoard(new int[][] {
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

        SudokuSolver solver = workingBoard -> {
            workingBoard.write(0, 0, 9);
            return SolveResult.solved(workingBoard, new SolveResult.Metrics(1, 0, 1_000L));
        };

        SolveResult result = solver.solve(input);

        assertEquals(0, input.read(0, 0));
        assertTrue(result.isSolved());
        assertEquals(9, result.getBoard().orElseThrow().read(0, 0));
    }

    @Test
    void bothSolvedAndUnsolvedResultsAreRepresented() {
        SudokuBoard solvedBoard = new SudokuBoard(new int[][] {
                { 1, 2, 3, 4, 5, 6, 7, 8, 9 },
                { 4, 5, 6, 7, 8, 9, 1, 2, 3 },
                { 7, 8, 9, 1, 2, 3, 4, 5, 6 },
                { 2, 3, 4, 5, 6, 7, 8, 9, 1 },
                { 5, 6, 7, 8, 9, 1, 2, 3, 4 },
                { 8, 9, 1, 2, 3, 4, 5, 6, 7 },
                { 3, 4, 5, 6, 7, 8, 9, 1, 2 },
                { 6, 7, 8, 9, 1, 2, 3, 4, 5 },
                { 9, 1, 2, 3, 4, 5, 6, 7, 8 }
        });

        SolveResult solved = SolveResult.solved(solvedBoard, new SolveResult.Metrics(40, 3, 500_000L));
        SolveResult unsolved = SolveResult.unsolved(new SolveResult.Metrics(100, 20, 2_000_000L));

        assertTrue(solved.isSolved());
        assertTrue(solved.getBoard().isPresent());

        assertFalse(unsolved.isSolved());
        assertTrue(unsolved.getBoard().isEmpty());
    }

    @Test
    void metricsAreIncludedInResult() {
        SolveResult.Metrics metrics = new SolveResult.Metrics(123, 12, 1_250_000L);
        SolveResult result = SolveResult.unsolved(metrics);

        assertEquals(123, result.getMetrics().getAssignmentsTried());
        assertEquals(12, result.getMetrics().getBacktracks());
        assertEquals(1_250_000L, result.getMetrics().getElapsedNanos());
    }

    @Test
    void stepTrackingIsOptional() {
        SolveResult withoutSteps = SolveResult.unsolved(new SolveResult.Metrics(5, 1, 100L));
        SolveResult withSteps = SolveResult.unsolved(
                new SolveResult.Metrics(5, 1, 100L),
                List.of("Fill r0c0 with 7", "Backtrack r0c0"));

        assertTrue(withoutSteps.getSteps().isEmpty());
        assertTrue(withSteps.getSteps().isPresent());
        assertEquals(2, withSteps.getSteps().orElseThrow().size());
    }
}

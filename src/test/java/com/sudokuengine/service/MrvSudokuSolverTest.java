package com.sudokuengine.service;

import com.sudokuengine.model.SolveResult;
import com.sudokuengine.model.SudokuBoard;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MrvSudokuSolverTest {

    private final SudokuValidator validator = new SudokuValidator();
    private final BacktrackingSudokuSolver backtrackingSolver = new BacktrackingSudokuSolver();
    private final MrvSudokuSolver mrvSolver = new MrvSudokuSolver();

    @Test
    void mrvProducesSameCorrectSolutionAsBacktracking() {
        SudokuBoard puzzle = createDifficultPuzzle();

        SolveResult backtrackingResult = backtrackingSolver.solve(puzzle);
        SolveResult mrvResult = mrvSolver.solve(puzzle);

        SudokuBoard backtrackingBoard = backtrackingResult.getBoard().orElseThrow();
        SudokuBoard mrvBoard = mrvResult.getBoard().orElseThrow();

        assertTrue(backtrackingResult.isSolved());
        assertTrue(mrvResult.isSolved());
        assertTrue(validator.isValid(backtrackingBoard));
        assertTrue(validator.isValid(mrvBoard));
        assertArrayEquals(backtrackingBoard.toMatrix(), mrvBoard.toMatrix());
    }

    @Test
    void mrvVisitsFewerNodesOnDifficultPuzzle() {
        SudokuBoard puzzle = createDifficultPuzzle();

        SolveResult backtrackingResult = backtrackingSolver.solve(puzzle);
        SolveResult mrvResult = mrvSolver.solve(puzzle);

        int backtrackingVisitedNodes = backtrackingResult.getMetrics().getVisitedNodes();
        int mrvVisitedNodes = mrvResult.getMetrics().getVisitedNodes();

        assertTrue(mrvVisitedNodes < backtrackingVisitedNodes,
                "Expected MRV to visit fewer nodes than baseline backtracking, but got MRV="
                        + mrvVisitedNodes + " and backtracking=" + backtrackingVisitedNodes);
    }

    @Test
    void performanceMetricsCanBeComparedAcrossSolvers() {
        SudokuBoard puzzle = createDifficultPuzzle();

        SolveResult backtrackingResult = backtrackingSolver.solve(puzzle);
        SolveResult mrvResult = mrvSolver.solve(puzzle);

        assertTrue(backtrackingResult.getMetrics().getElapsedNanos() >= 0);
        assertTrue(mrvResult.getMetrics().getElapsedNanos() >= 0);
        assertTrue(backtrackingResult.getMetrics().getBacktracks() >= 0);
        assertTrue(mrvResult.getMetrics().getBacktracks() >= 0);
    }

    private static SudokuBoard createDifficultPuzzle() {
        int[][] puzzle = {
                { 8, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 3, 6, 0, 0, 0, 0, 0 },
                { 0, 7, 0, 0, 9, 0, 2, 0, 0 },
                { 0, 5, 0, 0, 0, 7, 0, 0, 0 },
                { 0, 0, 0, 0, 4, 5, 7, 0, 0 },
                { 0, 0, 0, 1, 0, 0, 0, 3, 0 },
                { 0, 0, 1, 0, 0, 0, 0, 6, 8 },
                { 0, 0, 8, 5, 0, 0, 0, 1, 0 },
                { 0, 9, 0, 0, 0, 0, 4, 0, 0 }
        };

        return new SudokuBoard(Arrays.stream(puzzle)
                .map(int[]::clone)
                .toArray(int[][]::new));
    }
}

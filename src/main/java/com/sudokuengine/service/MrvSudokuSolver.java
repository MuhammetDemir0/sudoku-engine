package com.sudokuengine.service;

import com.sudokuengine.exception.InvalidBoardException;
import com.sudokuengine.model.SolveResult;
import com.sudokuengine.model.SolveStep;
import com.sudokuengine.model.SolveStepType;
import com.sudokuengine.model.SudokuBoard;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sudoku solver that uses MRV (Minimum Remaining Values) cell selection.
 */
@Component
public class MrvSudokuSolver implements SudokuSolver {

    private final SudokuValidator validator;

    public MrvSudokuSolver() {
        this(new SudokuValidator());
    }

    public MrvSudokuSolver(SudokuValidator validator) {
        this.validator = validator;
    }

    @Override
    public SolveResult solve(SudokuBoard inputBoard) {
        return solve(inputBoard, false);
    }

    /**
     * Solves a board using MRV with optional step tracking.
     */
    public SolveResult solve(SudokuBoard inputBoard, boolean trackSteps) {
        if (inputBoard == null) {
            throw new InvalidBoardException("Input board cannot be null.");
        }
        return solveInternal(inputBoard.copy(), trackSteps);
    }

    @Override
    public SolveResult solveInternal(SudokuBoard workingBoard) {
        return solveInternal(workingBoard, false);
    }

    private SolveResult solveInternal(SudokuBoard workingBoard, boolean trackSteps) {
        if (!validator.isValid(workingBoard)) {
            throw new InvalidBoardException("Initial board is invalid and cannot be solved.");
        }

        SearchMetrics metrics = new SearchMetrics();
        StepSequence sequence = new StepSequence();
        List<SolveStep> steps = trackSteps ? new ArrayList<>() : null;

        long start = System.nanoTime();
        boolean solved = solveRecursively(workingBoard, metrics, 0, steps, sequence);
        long elapsed = System.nanoTime() - start;

        SolveResult.Metrics resultMetrics = new SolveResult.Metrics(
                metrics.visitedNodes,
                metrics.backtracks,
                metrics.maxRecursionDepth,
                elapsed);

        if (!solved) {
            return trackSteps ? SolveResult.unsolved(resultMetrics, steps) : SolveResult.unsolved(resultMetrics);
        }
        return trackSteps ? SolveResult.solved(workingBoard, resultMetrics, steps)
                : SolveResult.solved(workingBoard, resultMetrics);
    }

    private boolean solveRecursively(SudokuBoard board, SearchMetrics metrics, int depth,
            List<SolveStep> steps, StepSequence sequence) {
        if (depth > metrics.maxRecursionDepth) {
            metrics.maxRecursionDepth = depth;
        }

        CellSelection selection = selectCellWithFewestCandidates(board);
        if (selection == null) {
            addStep(steps, sequence, SolveStepType.SOLUTION_FOUND, -1, -1, SudokuBoard.EMPTY, depth);
            return true;
        }

        if (selection.candidates.isEmpty()) {
            return false;
        }

        for (int candidate : selection.candidates) {
            metrics.visitedNodes++;
            board.write(selection.row, selection.col, candidate);
            addStep(steps, sequence, SolveStepType.PLACE_VALUE,
                    selection.row, selection.col, candidate, depth);

            if (solveRecursively(board, metrics, depth + 1, steps, sequence)) {
                return true;
            }

            board.write(selection.row, selection.col, SudokuBoard.EMPTY);
            addStep(steps, sequence, SolveStepType.REMOVE_VALUE,
                    selection.row, selection.col, SudokuBoard.EMPTY, depth);
            metrics.backtracks++;
        }

        return false;
    }

    private static CellSelection selectCellWithFewestCandidates(SudokuBoard board) {
        CellSelection best = null;

        for (int row = 0; row < SudokuBoard.SIZE; row++) {
            for (int col = 0; col < SudokuBoard.SIZE; col++) {
                if (board.read(row, col) != SudokuBoard.EMPTY) {
                    continue;
                }

                List<Integer> candidates = getCandidates(board, row, col);
                if (best == null || candidates.size() < best.candidates.size()) {
                    best = new CellSelection(row, col, candidates);
                    if (candidates.size() <= 1) {
                        return best;
                    }
                }
            }
        }

        return best;
    }

    private static List<Integer> getCandidates(SudokuBoard board, int row, int col) {
        List<Integer> candidates = new ArrayList<>(SudokuBoard.MAX_VALUE);
        for (int value = 1; value <= SudokuBoard.MAX_VALUE; value++) {
            if (isValidMove(board, row, col, value)) {
                candidates.add(value);
            }
        }
        return candidates;
    }

    private static boolean isValidMove(SudokuBoard board, int row, int col, int value) {
        return isRowValid(board, row, value)
                && isColumnValid(board, col, value)
                && isBoxValid(board, row, col, value);
    }

    private static boolean isRowValid(SudokuBoard board, int row, int value) {
        for (int col = 0; col < SudokuBoard.SIZE; col++) {
            if (board.read(row, col) == value) {
                return false;
            }
        }
        return true;
    }

    private static boolean isColumnValid(SudokuBoard board, int col, int value) {
        for (int row = 0; row < SudokuBoard.SIZE; row++) {
            if (board.read(row, col) == value) {
                return false;
            }
        }
        return true;
    }

    private static boolean isBoxValid(SudokuBoard board, int row, int col, int value) {
        int boxStartRow = (row / 3) * 3;
        int boxStartCol = (col / 3) * 3;

        for (int r = boxStartRow; r < boxStartRow + 3; r++) {
            for (int c = boxStartCol; c < boxStartCol + 3; c++) {
                if (board.read(r, c) == value) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void addStep(List<SolveStep> steps, StepSequence sequence, SolveStepType type,
            int row, int col, int value, int depth) {
        if (steps == null) {
            return;
        }
        steps.add(new SolveStep(type, row, col, value, sequence.next(), depth));
    }

    private static final class CellSelection {
        private final int row;
        private final int col;
        private final List<Integer> candidates;

        private CellSelection(int row, int col, List<Integer> candidates) {
            this.row = row;
            this.col = col;
            this.candidates = candidates;
        }
    }

    private static final class StepSequence {
        private int current = 1;

        private int next() {
            return current++;
        }
    }

    private static final class SearchMetrics {
        private int visitedNodes;
        private int backtracks;
        private int maxRecursionDepth;
    }
}

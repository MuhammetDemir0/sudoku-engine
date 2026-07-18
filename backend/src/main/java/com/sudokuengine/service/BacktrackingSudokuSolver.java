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
 * Recursive backtracking Sudoku solver implementation.
 */
@Component
public class BacktrackingSudokuSolver implements SudokuSolver {

    private final SudokuValidator validator;

    public BacktrackingSudokuSolver() {
        this(new SudokuValidator());
    }

    public BacktrackingSudokuSolver(SudokuValidator validator) {
        this.validator = validator;
    }

    @Override
    public SolveResult solve(SudokuBoard inputBoard) {
        return solve(inputBoard, false);
    }

    /**
     * Solves a board with optional step tracking for visualization.
     */
    public SolveResult solve(SudokuBoard inputBoard, boolean trackSteps) {
        if (inputBoard == null) {
            throw new InvalidBoardException("Input board cannot be null.");
        }
        return solveInternal(inputBoard.copy(), trackSteps);
    }

    /**
     * Counts solutions up to a caller-provided limit.
     */
    public int countSolutions(SudokuBoard board, int limit) {
        if (board == null) {
            throw new InvalidBoardException("Input board cannot be null.");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be greater than zero.");
        }

        SudokuBoard workingCopy = board.copy();
        if (!validator.isValid(workingCopy)) {
            throw new InvalidBoardException("Initial board is invalid and cannot be counted.");
        }
        return countSolutionsRecursively(workingCopy, limit);
    }

    @Override
    public SolveResult solveInternal(SudokuBoard workingBoard) {
        return solveInternal(workingBoard, false);
    }

    private SolveResult solveInternal(SudokuBoard workingBoard, boolean trackSteps) {
        if (!validator.isValid(workingBoard)) {
            throw new InvalidBoardException("Initial board is invalid and cannot be solved.");
        }

        SearchMetrics searchMetrics = new SearchMetrics();
        StepSequence stepSequence = new StepSequence();
        List<SolveStep> steps = trackSteps ? new ArrayList<>() : null;
        long start = System.nanoTime();
        boolean solved = solveRecursively(workingBoard, searchMetrics, 0, steps, stepSequence);
        long elapsed = System.nanoTime() - start;

        SolveResult.Metrics metrics = new SolveResult.Metrics(
                searchMetrics.visitedNodes,
                searchMetrics.backtracks,
                searchMetrics.maxRecursionDepth,
                elapsed);

        if (!solved) {
            return trackSteps ? SolveResult.unsolved(metrics, steps) : SolveResult.unsolved(metrics);
        }
        return trackSteps ? SolveResult.solved(workingBoard, metrics, steps)
                : SolveResult.solved(workingBoard, metrics);
    }

    private boolean solveRecursively(SudokuBoard board, SearchMetrics metrics, int depth,
            List<SolveStep> steps, StepSequence sequence) {
        if (depth > metrics.maxRecursionDepth) {
            metrics.maxRecursionDepth = depth;
        }

        Cell emptyCell = findFirstEmptyCell(board);
        if (emptyCell == null) {
            addStep(steps, sequence, SolveStepType.SOLUTION_FOUND, -1, -1, SudokuBoard.EMPTY, depth);
            return true;
        }

        for (int candidate = 1; candidate <= SudokuBoard.MAX_VALUE; candidate++) {
            metrics.visitedNodes++;
            if (!isValidMove(board, emptyCell.row(), emptyCell.col(), candidate)) {
                continue;
            }

            board.write(emptyCell.row(), emptyCell.col(), candidate);
            addStep(steps, sequence, SolveStepType.PLACE_VALUE,
                    emptyCell.row(), emptyCell.col(), candidate, depth);
            if (solveRecursively(board, metrics, depth + 1, steps, sequence)) {
                return true;
            }

            board.write(emptyCell.row(), emptyCell.col(), SudokuBoard.EMPTY);
            addStep(steps, sequence, SolveStepType.REMOVE_VALUE,
                    emptyCell.row(), emptyCell.col(), SudokuBoard.EMPTY, depth);
            metrics.backtracks++;
        }
        return false;
    }

    private int countSolutionsRecursively(SudokuBoard board, int limit) {
        if (limit <= 0) {
            return 0;
        }

        Cell emptyCell = findFirstEmptyCell(board);
        if (emptyCell == null) {
            return 1;
        }

        int found = 0;
        for (int candidate = 1; candidate <= SudokuBoard.MAX_VALUE; candidate++) {
            if (!isValidMove(board, emptyCell.row(), emptyCell.col(), candidate)) {
                continue;
            }

            board.write(emptyCell.row(), emptyCell.col(), candidate);
            found += countSolutionsRecursively(board, limit - found);
            board.write(emptyCell.row(), emptyCell.col(), SudokuBoard.EMPTY);

            if (found >= limit) {
                return found;
            }
        }
        return found;
    }

    private static Cell findFirstEmptyCell(SudokuBoard board) {
        for (int row = 0; row < SudokuBoard.SIZE; row++) {
            for (int col = 0; col < SudokuBoard.SIZE; col++) {
                if (board.read(row, col) == SudokuBoard.EMPTY) {
                    return new Cell(row, col);
                }
            }
        }
        return null;
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

    private record Cell(int row, int col) {
    }

    private static void addStep(List<SolveStep> steps, StepSequence sequence, SolveStepType type,
            int row, int col, int value, int depth) {
        if (steps == null) {
            return;
        }
        steps.add(new SolveStep(type, row, col, value, sequence.next(), depth));
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

package com.sudokuengine.service;

import com.sudokuengine.exception.InvalidBoardException;
import com.sudokuengine.model.SolveResult;
import com.sudokuengine.model.SudokuBoard;
import org.springframework.stereotype.Component;

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
    public SolveResult solveInternal(SudokuBoard workingBoard) {
        if (!validator.isValid(workingBoard)) {
            throw new InvalidBoardException("Initial board is invalid and cannot be solved.");
        }

        SearchMetrics searchMetrics = new SearchMetrics();
        long start = System.nanoTime();
        boolean solved = solveRecursively(workingBoard, searchMetrics, 0);
        long elapsed = System.nanoTime() - start;

        SolveResult.Metrics metrics = new SolveResult.Metrics(
                searchMetrics.visitedNodes,
                searchMetrics.backtracks,
                searchMetrics.maxRecursionDepth,
                elapsed);

        if (!solved) {
            return SolveResult.unsolved(metrics);
        }
        return SolveResult.solved(workingBoard, metrics);
    }

    private boolean solveRecursively(SudokuBoard board, SearchMetrics metrics, int depth) {
        if (depth > metrics.maxRecursionDepth) {
            metrics.maxRecursionDepth = depth;
        }

        Cell emptyCell = findFirstEmptyCell(board);
        if (emptyCell == null) {
            return true;
        }

        for (int candidate = 1; candidate <= SudokuBoard.MAX_VALUE; candidate++) {
            metrics.visitedNodes++;
            if (!isValidMove(board, emptyCell.row(), emptyCell.col(), candidate)) {
                continue;
            }

            board.write(emptyCell.row(), emptyCell.col(), candidate);
            if (solveRecursively(board, metrics, depth + 1)) {
                return true;
            }

            board.write(emptyCell.row(), emptyCell.col(), SudokuBoard.EMPTY);
            metrics.backtracks++;
        }
        return false;
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

    private static final class SearchMetrics {
        private int visitedNodes;
        private int backtracks;
        private int maxRecursionDepth;
    }
}

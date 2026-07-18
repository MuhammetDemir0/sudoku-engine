package com.sudokuengine.service;

import com.sudokuengine.exception.PuzzleGenerationException;
import com.sudokuengine.model.SudokuBoard;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Generates complete valid Sudoku solution boards.
 */
@Component
public class SudokuSolutionGenerator {

    private final Random random;

    public SudokuSolutionGenerator() {
        this(new Random());
    }

    public SudokuSolutionGenerator(Random random) {
        this.random = Objects.requireNonNull(random, "Random dependency cannot be null.");
    }

    public SudokuBoard generate() {
        SudokuBoard board = new SudokuBoard(new int[SudokuBoard.SIZE][SudokuBoard.SIZE]);
        boolean generated = fillBoard(board);
        if (!generated) {
            throw new PuzzleGenerationException("Failed to generate a complete Sudoku board.");
        }
        return board;
    }

    private boolean fillBoard(SudokuBoard board) {
        Cell emptyCell = findFirstEmptyCell(board);
        if (emptyCell == null) {
            return true;
        }

        for (int candidate : shuffledCandidates()) {
            if (!isValidMove(board, emptyCell.row, emptyCell.col, candidate)) {
                continue;
            }

            board.write(emptyCell.row, emptyCell.col, candidate);
            if (fillBoard(board)) {
                return true;
            }
            board.write(emptyCell.row, emptyCell.col, SudokuBoard.EMPTY);
        }

        return false;
    }

    private List<Integer> shuffledCandidates() {
        List<Integer> values = new ArrayList<>(SudokuBoard.MAX_VALUE);
        for (int value = 1; value <= SudokuBoard.MAX_VALUE; value++) {
            values.add(value);
        }
        Collections.shuffle(values, random);
        return values;
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

    private static final class Cell {
        private final int row;
        private final int col;

        private Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
}

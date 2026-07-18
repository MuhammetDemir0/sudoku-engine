package com.sudokuengine.model;

import com.sudokuengine.exception.InvalidBoardException;
import com.sudokuengine.exception.InvalidCellValueException;
import com.sudokuengine.exception.InvalidCoordinateException;

import java.util.Arrays;

/**
 * Immutable-size Sudoku board domain object.
 *
 * <p>
 * The board is always 9x9 and cell values must be in range 0-9 where 0
 * represents empty.
 * </p>
 */
public final class SudokuBoard {

    public static final int SIZE = 9;
    public static final int EMPTY = 0;
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 9;

    private final int[][] cells;

    public SudokuBoard(int[][] cells) {
        validateBoardShape(cells);
        this.cells = deepCopy(cells);
        validateAllValues(this.cells);
    }

    public int getValue(int row, int col) {
        validateCoordinate(row, col);
        return cells[row][col];
    }

    /**
     * Reads the value at the given coordinate.
     */
    public int read(int row, int col) {
        return getValue(row, col);
    }

    public void setValue(int row, int col, int value) {
        validateCoordinate(row, col);
        validateValue(value);
        cells[row][col] = value;
    }

    /**
     * Writes a value at the given coordinate.
     */
    public void write(int row, int col, int value) {
        setValue(row, col, value);
    }

    /**
     * Returns a defensive copy of the board matrix.
     */
    public int[][] toMatrix() {
        return deepCopy(cells);
    }

    /**
     * Returns a deep-copied domain object.
     */
    public SudokuBoard copy() {
        return new SudokuBoard(this.cells);
    }

    private static void validateBoardShape(int[][] board) {
        if (board == null) {
            throw new InvalidBoardException("Board matrix cannot be null.");
        }
        if (board.length != SIZE) {
            throw new InvalidBoardException("Board must have exactly 9 rows.");
        }
        for (int i = 0; i < SIZE; i++) {
            if (board[i] == null || board[i].length != SIZE) {
                throw new InvalidBoardException("Board must be a 9x9 matrix.");
            }
        }
    }

    private static void validateAllValues(int[][] board) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                validateValue(board[row][col]);
            }
        }
    }

    private static void validateCoordinate(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            throw new InvalidCoordinateException("Coordinates must be between 0 and 8.");
        }
    }

    private static void validateValue(int value) {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new InvalidCellValueException("Cell value must be between 0 and 9.");
        }
    }

    private static int[][] deepCopy(int[][] source) {
        int[][] copy = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = Arrays.copyOf(source[i], source[i].length);
        }
        return copy;
    }
}

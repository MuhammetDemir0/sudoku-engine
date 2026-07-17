package com.sudokuengine.model;

import java.util.Objects;

/**
 * Immutable recommendation for a safe next Sudoku move.
 */
public final class Hint {

    private final int row;
    private final int col;
    private final int value;
    private final String reason;

    public Hint(int row, int col, int value, String reason) {
        this.row = validateCoordinate(row, "row");
        this.col = validateCoordinate(col, "col");
        this.value = validateValue(value);
        this.reason = Objects.requireNonNull(reason, "Reason cannot be null.");
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getValue() {
        return value;
    }

    public String getReason() {
        return reason;
    }

    private static int validateCoordinate(int coordinate, String name) {
        if (coordinate < 0 || coordinate >= SudokuBoard.SIZE) {
            throw new IllegalArgumentException(name + " must be between 0 and 8.");
        }
        return coordinate;
    }

    private static int validateValue(int value) {
        if (value < SudokuBoard.MIN_VALUE + 1 || value > SudokuBoard.MAX_VALUE) {
            throw new IllegalArgumentException("Hint value must be between 1 and 9.");
        }
        return value;
    }
}

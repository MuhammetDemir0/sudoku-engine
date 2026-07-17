package com.sudokuengine.model;

import java.util.Objects;

/**
 * Represents a single rule violation found during Sudoku validation.
 */
public final class ValidationViolation {

    private final ViolationType type;
    private final int row;
    private final int col;
    private final int value;
    private final String message;

    public ValidationViolation(ViolationType type, int row, int col, int value, String message) {
        this.type = Objects.requireNonNull(type, "Violation type cannot be null.");
        this.message = Objects.requireNonNull(message, "Violation message cannot be null.");
        this.row = row;
        this.col = col;
        this.value = value;
    }

    public ViolationType getType() {
        return type;
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

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ValidationViolation that)) {
            return false;
        }
        return row == that.row
                && col == that.col
                && value == that.value
                && type == that.type
                && message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, row, col, value, message);
    }
}

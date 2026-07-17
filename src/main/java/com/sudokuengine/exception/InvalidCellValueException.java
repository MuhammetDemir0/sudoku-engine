package com.sudokuengine.exception;

/**
 * Thrown when a Sudoku cell value is outside accepted limits.
 */
public class InvalidCellValueException extends RuntimeException {

    public InvalidCellValueException(String message) {
        super(message);
    }

    public InvalidCellValueException(String message, Throwable cause) {
        super(message, cause);
    }
}

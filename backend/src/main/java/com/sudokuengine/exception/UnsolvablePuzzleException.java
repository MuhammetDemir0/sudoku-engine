package com.sudokuengine.exception;

/**
 * Thrown when a Sudoku puzzle cannot be solved with current constraints.
 */
public class UnsolvablePuzzleException extends RuntimeException {

    public UnsolvablePuzzleException(String message) {
        super(message);
    }

    public UnsolvablePuzzleException(String message, Throwable cause) {
        super(message, cause);
    }
}

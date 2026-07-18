package com.sudokuengine.exception;

/**
 * Thrown when a Sudoku board structure or state is invalid.
 */
public class InvalidBoardException extends RuntimeException {

    public InvalidBoardException(String message) {
        super(message);
    }

    public InvalidBoardException(String message, Throwable cause) {
        super(message, cause);
    }
}

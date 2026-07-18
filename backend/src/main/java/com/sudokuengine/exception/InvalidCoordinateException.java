package com.sudokuengine.exception;

/**
 * Thrown when a Sudoku board coordinate is out of bounds.
 */
public class InvalidCoordinateException extends RuntimeException {

    public InvalidCoordinateException(String message) {
        super(message);
    }

    public InvalidCoordinateException(String message, Throwable cause) {
        super(message, cause);
    }
}

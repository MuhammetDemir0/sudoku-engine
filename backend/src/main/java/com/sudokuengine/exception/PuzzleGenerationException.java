package com.sudokuengine.exception;

/**
 * Thrown when puzzle generation fails due to an unrecoverable error.
 */
public class PuzzleGenerationException extends RuntimeException {

    public PuzzleGenerationException(String message) {
        super(message);
    }

    public PuzzleGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.sudokuengine.service;

import com.sudokuengine.exception.InvalidBoardException;
import com.sudokuengine.model.SolveResult;
import com.sudokuengine.model.SudokuBoard;

import java.util.Objects;

/**
 * Contract for Sudoku solving algorithms.
 *
 * <p>
 * Implementations receive a defensive copy through
 * {@link #solveInternal(SudokuBoard)} so
 * input boards are never mutated.
 * </p>
 */
public interface SudokuSolver {

    /**
     * Solves the given Sudoku board without mutating the caller's input instance.
     */
    default SolveResult solve(SudokuBoard inputBoard) {
        if (inputBoard == null) {
            throw new InvalidBoardException("Input board cannot be null.");
        }
        SudokuBoard workingCopy = inputBoard.copy();
        SolveResult result = Objects.requireNonNull(
                solveInternal(workingCopy),
                "Solver must return a non-null SolveResult.");
        return result;
    }

    /**
     * Runs the concrete solving algorithm against a copy of the original board.
     */
    SolveResult solveInternal(SudokuBoard workingBoard);
}

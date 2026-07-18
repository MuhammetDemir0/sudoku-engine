package com.sudokuengine.model;

import com.sudokuengine.exception.InvalidBoardException;

import java.util.Objects;

/**
 * Immutable pair of a generated puzzle and its retained solution board.
 */
public final class SudokuPuzzle {

    private final SudokuBoard puzzle;
    private final SudokuBoard solution;

    public SudokuPuzzle(SudokuBoard puzzle, SudokuBoard solution) {
        this.puzzle = Objects.requireNonNull(puzzle, "Puzzle board cannot be null.").copy();
        this.solution = Objects.requireNonNull(solution, "Solution board cannot be null.").copy();

        if (countEmptyCells(this.solution) > 0) {
            throw new InvalidBoardException("Solution board must be completely filled.");
        }
    }

    public SudokuBoard getPuzzle() {
        return puzzle.copy();
    }

    public SudokuBoard getSolution() {
        return solution.copy();
    }

    private static int countEmptyCells(SudokuBoard board) {
        int emptyCount = 0;
        for (int row = 0; row < SudokuBoard.SIZE; row++) {
            for (int col = 0; col < SudokuBoard.SIZE; col++) {
                if (board.read(row, col) == SudokuBoard.EMPTY) {
                    emptyCount++;
                }
            }
        }
        return emptyCount;
    }
}

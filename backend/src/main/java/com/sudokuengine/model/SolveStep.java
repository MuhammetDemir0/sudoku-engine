package com.sudokuengine.model;

import java.util.Objects;

/**
 * Immutable step entry for solver visualization.
 */
public final class SolveStep {

    private final SolveStepType type;
    private final int row;
    private final int col;
    private final int value;
    private final int sequence;
    private final int depth;

    public SolveStep(SolveStepType type, int row, int col, int value, int sequence, int depth) {
        this.type = Objects.requireNonNull(type, "Step type cannot be null.");
        this.sequence = validateSequence(sequence);
        this.depth = validateDepth(depth);

        if (type == SolveStepType.SOLUTION_FOUND) {
            this.row = -1;
            this.col = -1;
            this.value = SudokuBoard.EMPTY;
            return;
        }

        this.row = validateCoordinate(row, "row");
        this.col = validateCoordinate(col, "col");
        this.value = validateValue(value);
    }

    public SolveStepType getType() {
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

    public int getSequence() {
        return sequence;
    }

    public int getDepth() {
        return depth;
    }

    private static int validateCoordinate(int coordinate, String name) {
        if (coordinate < 0 || coordinate >= SudokuBoard.SIZE) {
            throw new IllegalArgumentException(name + " must be between 0 and 8.");
        }
        return coordinate;
    }

    private static int validateValue(int value) {
        if (value < SudokuBoard.MIN_VALUE || value > SudokuBoard.MAX_VALUE) {
            throw new IllegalArgumentException("Step value must be between 0 and 9.");
        }
        return value;
    }

    private static int validateSequence(int sequence) {
        if (sequence < 1) {
            throw new IllegalArgumentException("Sequence must be at least 1.");
        }
        return sequence;
    }

    private static int validateDepth(int depth) {
        if (depth < 0) {
            throw new IllegalArgumentException("Depth cannot be negative.");
        }
        return depth;
    }
}

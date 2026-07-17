package com.sudokuengine.model;

import com.sudokuengine.exception.InvalidBoardException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable result object returned by Sudoku solving algorithms.
 */
public final class SolveResult {

    private final boolean solved;
    private final SudokuBoard board;
    private final Metrics metrics;
    private final List<String> steps;

    private SolveResult(boolean solved, SudokuBoard board, Metrics metrics, List<String> steps) {
        this.solved = solved;
        this.board = board == null ? null : board.copy();
        this.metrics = Objects.requireNonNull(metrics, "Metrics cannot be null.");
        this.steps = steps == null ? null : List.copyOf(steps);
    }

    public static SolveResult solved(SudokuBoard solvedBoard, Metrics metrics) {
        if (solvedBoard == null) {
            throw new InvalidBoardException("Solved board cannot be null.");
        }
        return new SolveResult(true, solvedBoard, metrics, null);
    }

    public static SolveResult solved(SudokuBoard solvedBoard, Metrics metrics, List<String> steps) {
        if (solvedBoard == null) {
            throw new InvalidBoardException("Solved board cannot be null.");
        }
        return new SolveResult(true, solvedBoard, metrics, steps);
    }

    public static SolveResult unsolved(Metrics metrics) {
        return new SolveResult(false, null, metrics, null);
    }

    public static SolveResult unsolved(Metrics metrics, List<String> steps) {
        return new SolveResult(false, null, metrics, steps);
    }

    public boolean isSolved() {
        return solved;
    }

    public Optional<SudokuBoard> getBoard() {
        return board == null ? Optional.empty() : Optional.of(board.copy());
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public Optional<List<String>> getSteps() {
        return steps == null ? Optional.empty() : Optional.of(List.copyOf(steps));
    }

    /**
     * Solver metrics for performance and effort reporting.
     */
    public static final class Metrics {

        private final int assignmentsTried;
        private final int backtracks;
        private final long elapsedNanos;

        public Metrics(int assignmentsTried, int backtracks, long elapsedNanos) {
            if (assignmentsTried < 0) {
                throw new IllegalArgumentException("Assignments tried cannot be negative.");
            }
            if (backtracks < 0) {
                throw new IllegalArgumentException("Backtracks cannot be negative.");
            }
            if (elapsedNanos < 0) {
                throw new IllegalArgumentException("Elapsed nanos cannot be negative.");
            }
            this.assignmentsTried = assignmentsTried;
            this.backtracks = backtracks;
            this.elapsedNanos = elapsedNanos;
        }

        public int getAssignmentsTried() {
            return assignmentsTried;
        }

        public int getBacktracks() {
            return backtracks;
        }

        public long getElapsedNanos() {
            return elapsedNanos;
        }
    }
}

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
    private final List<SolveStep> steps;

    private SolveResult(boolean solved, SudokuBoard board, Metrics metrics, List<SolveStep> steps) {
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

    public static SolveResult solved(SudokuBoard solvedBoard, Metrics metrics, List<SolveStep> steps) {
        if (solvedBoard == null) {
            throw new InvalidBoardException("Solved board cannot be null.");
        }
        return new SolveResult(true, solvedBoard, metrics, steps);
    }

    public static SolveResult unsolved(Metrics metrics) {
        return new SolveResult(false, null, metrics, null);
    }

    public static SolveResult unsolved(Metrics metrics, List<SolveStep> steps) {
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

    public Optional<List<SolveStep>> getSteps() {
        return steps == null ? Optional.empty() : Optional.of(List.copyOf(steps));
    }

    /**
     * Solver metrics for performance and effort reporting.
     */
    public static final class Metrics {

        private final int visitedNodes;
        private final int backtracks;
        private final int maxRecursionDepth;
        private final long elapsedNanos;

        public Metrics(int assignmentsTried, int backtracks, long elapsedNanos) {
            this(assignmentsTried, backtracks, 0, elapsedNanos);
        }

        public Metrics(int visitedNodes, int backtracks, int maxRecursionDepth, long elapsedNanos) {
            if (visitedNodes < 0) {
                throw new IllegalArgumentException("Visited nodes cannot be negative.");
            }
            if (backtracks < 0) {
                throw new IllegalArgumentException("Backtracks cannot be negative.");
            }
            if (maxRecursionDepth < 0) {
                throw new IllegalArgumentException("Max recursion depth cannot be negative.");
            }
            if (elapsedNanos < 0) {
                throw new IllegalArgumentException("Elapsed nanos cannot be negative.");
            }
            this.visitedNodes = visitedNodes;
            this.backtracks = backtracks;
            this.maxRecursionDepth = maxRecursionDepth;
            this.elapsedNanos = elapsedNanos;
        }

        public int getAssignmentsTried() {
            return visitedNodes;
        }

        public int getVisitedNodes() {
            return visitedNodes;
        }

        public int getBacktracks() {
            return backtracks;
        }

        public int getMaxRecursionDepth() {
            return maxRecursionDepth;
        }

        public long getElapsedNanos() {
            return elapsedNanos;
        }
    }
}

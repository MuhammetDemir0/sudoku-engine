package com.sudokuengine.config;

import com.sudokuengine.model.Difficulty;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Central configuration for puzzle difficulty classification and generation
 * targets.
 */
public final class DifficultyThresholdConfig {

    private static final DifficultyThresholdConfig DEFAULT = new DifficultyThresholdConfig(Map.of(
            Difficulty.EASY, new Thresholds(36, 45, 80, 0, 45),
            Difficulty.MEDIUM, new Thresholds(32, 35, 200, 30, 49),
            Difficulty.HARD, new Thresholds(28, 31, 1_000, 300, 53),
            Difficulty.EXPERT, new Thresholds(0, 27, Integer.MAX_VALUE, Integer.MAX_VALUE, 81)));

    private final EnumMap<Difficulty, Thresholds> thresholds;

    public DifficultyThresholdConfig(Map<Difficulty, Thresholds> thresholds) {
        Objects.requireNonNull(thresholds, "Thresholds cannot be null.");
        this.thresholds = new EnumMap<>(Difficulty.class);
        for (Difficulty difficulty : Difficulty.values()) {
            Thresholds value = thresholds.get(difficulty);
            if (value == null) {
                throw new IllegalArgumentException("Missing thresholds for " + difficulty + ".");
            }
            this.thresholds.put(difficulty, value);
        }
    }

    public static DifficultyThresholdConfig defaults() {
        return DEFAULT;
    }

    public Thresholds get(Difficulty difficulty) {
        return thresholds.get(Objects.requireNonNull(difficulty, "Difficulty cannot be null."));
    }

    /**
     * Finds the easiest difficulty whose clue-count threshold accepts the
     * puzzle.
     */
    public Difficulty classifyByClues(int clueCount) {
        validateNonNegative(clueCount, "Clue count");
        for (Difficulty difficulty : Difficulty.values()) {
            if (clueCount >= get(difficulty).minimumClues()) {
                return difficulty;
            }
        }
        return Difficulty.EXPERT;
    }

    /**
     * Finds the easiest difficulty whose solver-metric thresholds accept the
     * puzzle.
     */
    public Difficulty classifyBySolverMetrics(int visitedNodes, int backtracks, int maxRecursionDepth) {
        validateNonNegative(visitedNodes, "Visited nodes");
        validateNonNegative(backtracks, "Backtracks");
        validateNonNegative(maxRecursionDepth, "Max recursion depth");

        for (Difficulty difficulty : Difficulty.values()) {
            Thresholds threshold = get(difficulty);
            if (visitedNodes <= threshold.maximumVisitedNodes()
                    && backtracks <= threshold.maximumBacktracks()
                    && maxRecursionDepth <= threshold.maximumRecursionDepth()) {
                return difficulty;
            }
        }
        return Difficulty.EXPERT;
    }

    private static void validateNonNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " cannot be negative.");
        }
    }

    /**
     * Thresholds for a single difficulty band.
     *
     * @param minimumClues minimum givens needed to classify into this band by
     *                     clue count
     * @param generationTargetMaximumClues upper clue-count bound the generator
     *                                     aims for when this band is requested
     * @param maximumVisitedNodes maximum solver search nodes for this band
     * @param maximumBacktracks maximum solver backtracks for this band
     * @param maximumRecursionDepth maximum solver recursion depth for this band
     */
    public record Thresholds(
            int minimumClues,
            int generationTargetMaximumClues,
            int maximumVisitedNodes,
            int maximumBacktracks,
            int maximumRecursionDepth) {

        public Thresholds {
            validateNonNegative(minimumClues, "Minimum clues");
            validateNonNegative(generationTargetMaximumClues, "Generation target maximum clues");
            validateNonNegative(maximumVisitedNodes, "Maximum visited nodes");
            validateNonNegative(maximumBacktracks, "Maximum backtracks");
            validateNonNegative(maximumRecursionDepth, "Maximum recursion depth");
            if (generationTargetMaximumClues < minimumClues) {
                throw new IllegalArgumentException("Generation target maximum clues cannot be below minimum clues.");
            }
        }
    }
}

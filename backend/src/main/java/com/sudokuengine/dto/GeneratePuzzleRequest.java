package com.sudokuengine.dto;

import com.sudokuengine.model.Difficulty;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for puzzle generation.
 */
public record GeneratePuzzleRequest(
        @NotNull Difficulty difficulty) {
}

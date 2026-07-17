package com.sudokuengine.dto;

import com.sudokuengine.model.Difficulty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for puzzle generation.
 */
@Schema(description = "Request payload for generating a Sudoku puzzle.")
public record GeneratePuzzleRequest(
        @Schema(
                description = "Difficulty target for the generated puzzle.",
                example = "MEDIUM",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Difficulty difficulty) {
}

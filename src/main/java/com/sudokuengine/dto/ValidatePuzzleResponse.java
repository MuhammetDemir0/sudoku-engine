package com.sudokuengine.dto;

import com.sudokuengine.model.ValidationViolation;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response payload for board validation.
 */
@Schema(description = "Validation result for a Sudoku board.")
public record ValidatePuzzleResponse(
        @Schema(description = "Whether the board has no Sudoku rule violations.", example = "false")
        boolean valid,
        @Schema(description = "All row, column, and box violations found on the board.")
        List<ValidationViolationResponse> violations) {

    public static ValidatePuzzleResponse fromDomain(List<ValidationViolation> violations) {
        return new ValidatePuzzleResponse(
                violations.isEmpty(),
                violations.stream()
                        .map(ValidationViolationResponse::fromDomain)
                        .toList());
    }
}

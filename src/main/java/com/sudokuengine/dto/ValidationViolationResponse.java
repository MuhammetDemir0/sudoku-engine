package com.sudokuengine.dto;

import com.sudokuengine.model.ValidationViolation;
import com.sudokuengine.model.ViolationType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * API-safe representation of a validation violation.
 */
@Schema(description = "One Sudoku rule violation.")
public record ValidationViolationResponse(
        @Schema(description = "Rule category that was violated.", example = "ROW_DUPLICATE")
        ViolationType type,
        @Schema(description = "Zero-based row index for the duplicate value.", example = "0")
        int row,
        @Schema(description = "Zero-based column index for the duplicate value.", example = "1")
        int col,
        @Schema(description = "Duplicated value.", example = "5")
        int value,
        @Schema(description = "Human-readable violation explanation.", example = "Value 5 appears more than once in row 0.")
        String message) {

    public static ValidationViolationResponse fromDomain(ValidationViolation violation) {
        return new ValidationViolationResponse(
                violation.getType(),
                violation.getRow(),
                violation.getCol(),
                violation.getValue(),
                violation.getMessage());
    }
}

package com.sudokuengine.dto;

import com.sudokuengine.model.ValidationViolation;
import com.sudokuengine.model.ViolationType;

/**
 * API-safe representation of a validation violation.
 */
public record ValidationViolationResponse(
        ViolationType type,
        int row,
        int col,
        int value,
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

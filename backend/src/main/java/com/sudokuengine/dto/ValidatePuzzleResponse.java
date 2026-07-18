package com.sudokuengine.dto;

import com.sudokuengine.model.ValidationViolation;

import java.util.List;

/**
 * Response payload for board validation.
 */
public record ValidatePuzzleResponse(
        boolean valid,
        List<ValidationViolationResponse> violations) {

    public static ValidatePuzzleResponse fromDomain(List<ValidationViolation> violations) {
        return new ValidatePuzzleResponse(
                violations.isEmpty(),
                violations.stream()
                        .map(ValidationViolationResponse::fromDomain)
                        .toList());
    }
}

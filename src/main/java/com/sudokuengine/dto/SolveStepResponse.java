package com.sudokuengine.dto;

import com.sudokuengine.model.SolveStep;
import com.sudokuengine.model.SolveStepType;

/**
 * API-safe representation of a solver visualization step.
 */
public record SolveStepResponse(
        SolveStepType type,
        int row,
        int col,
        int value,
        int sequence,
        int depth) {

    public static SolveStepResponse fromDomain(SolveStep step) {
        return new SolveStepResponse(
                step.getType(),
                step.getRow(),
                step.getCol(),
                step.getValue(),
                step.getSequence(),
                step.getDepth());
    }
}

package com.sudokuengine.dto;

import com.sudokuengine.model.SolveStep;
import com.sudokuengine.model.SolveStepType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * API-safe representation of a solver visualization step.
 */
@Schema(description = "One solver action for client-side visualization.")
public record SolveStepResponse(
        @Schema(description = "Type of solver action.", example = "PLACE_VALUE")
        SolveStepType type,
        @Schema(description = "Zero-based row index.", example = "0")
        int row,
        @Schema(description = "Zero-based column index.", example = "2")
        int col,
        @Schema(description = "Cell value affected by the step.", example = "4")
        int value,
        @Schema(description = "One-based ordering of the step within the solve trace.", example = "1")
        int sequence,
        @Schema(description = "Recursion depth at the time of this step.", example = "1")
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

package com.sudokuengine.dto;

import com.sudokuengine.model.Hint;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload for a hint recommendation.
 */
@Schema(description = "Safe next move recommendation.")
public record HintResponse(
        @Schema(description = "Zero-based row index for the recommended cell.", example = "0")
        int row,
        @Schema(description = "Zero-based column index for the recommended cell.", example = "2")
        int col,
        @Schema(description = "Recommended value for the cell.", example = "4")
        int value,
        @Schema(description = "Explanation for why this move is safe.", example = "Cell (0,2) has only one valid candidate.")
        String reason) {

    public static HintResponse fromDomain(Hint hint) {
        return new HintResponse(
                hint.getRow(),
                hint.getCol(),
                hint.getValue(),
                hint.getReason());
    }
}

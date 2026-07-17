package com.sudokuengine.dto;

import com.sudokuengine.model.Hint;

/**
 * Response payload for a hint recommendation.
 */
public record HintResponse(
        int row,
        int col,
        int value,
        String reason) {

    public static HintResponse fromDomain(Hint hint) {
        return new HintResponse(
                hint.getRow(),
                hint.getCol(),
                hint.getValue(),
                hint.getReason());
    }
}

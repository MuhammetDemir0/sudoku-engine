package com.sudokuengine.dto;

import com.sudokuengine.model.SudokuBoard;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request payload for hint lookup.
 */
public record HintRequest(
        @NotNull
        List<List<Integer>> board) {

    @AssertTrue(message = "board must be a 9x9 matrix with values between 0 and 9")
    public boolean isBoardValid() {
        return board == null || BoardDtoMapper.isValidPayload(board);
    }

    public SudokuBoard toBoard() {
        return BoardDtoMapper.toDomain(board);
    }
}

package com.sudokuengine.dto;

import com.sudokuengine.model.SudokuBoard;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts between API board payloads and the domain board model.
 */
public final class BoardDtoMapper {

    private BoardDtoMapper() {
    }

    public static SudokuBoard toDomain(List<List<Integer>> board) {
        int[][] cells = new int[SudokuBoard.SIZE][SudokuBoard.SIZE];
        for (int row = 0; row < SudokuBoard.SIZE; row++) {
            for (int col = 0; col < SudokuBoard.SIZE; col++) {
                cells[row][col] = board.get(row).get(col);
            }
        }
        return new SudokuBoard(cells);
    }

    public static boolean isValidPayload(List<List<Integer>> board) {
        if (board == null || board.size() != SudokuBoard.SIZE) {
            return false;
        }
        for (List<Integer> row : board) {
            if (row == null || row.size() != SudokuBoard.SIZE) {
                return false;
            }
            for (Integer value : row) {
                if (value == null || value < SudokuBoard.EMPTY || value > SudokuBoard.MAX_VALUE) {
                    return false;
                }
            }
        }
        return true;
    }

    public static List<List<Integer>> fromDomain(SudokuBoard board) {
        int[][] cells = board.toMatrix();
        List<List<Integer>> rows = new ArrayList<>(SudokuBoard.SIZE);
        for (int row = 0; row < SudokuBoard.SIZE; row++) {
            List<Integer> values = new ArrayList<>(SudokuBoard.SIZE);
            for (int col = 0; col < SudokuBoard.SIZE; col++) {
                values.add(cells[row][col]);
            }
            rows.add(List.copyOf(values));
        }
        return List.copyOf(rows);
    }
}

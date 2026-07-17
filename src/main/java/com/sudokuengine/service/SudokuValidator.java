package com.sudokuengine.service;

import com.sudokuengine.model.SudokuBoard;
import com.sudokuengine.model.ValidationViolation;
import com.sudokuengine.model.ViolationType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates board state against Sudoku uniqueness rules.
 */
@Component
public class SudokuValidator {

    public List<ValidationViolation> validate(SudokuBoard board) {
        if (board == null) {
            throw new IllegalArgumentException("Sudoku board cannot be null.");
        }

        List<ValidationViolation> violations = new ArrayList<>();
        int[][] matrix = board.toMatrix();

        validateRows(matrix, violations);
        validateColumns(matrix, violations);
        validateBoxes(matrix, violations);

        return List.copyOf(violations);
    }

    public boolean isValid(SudokuBoard board) {
        return validate(board).isEmpty();
    }

    private static void validateRows(int[][] matrix, List<ValidationViolation> violations) {
        for (int row = 0; row < SudokuBoard.SIZE; row++) {
            int[] firstSeenColByValue = initializeIndexMap();
            for (int col = 0; col < SudokuBoard.SIZE; col++) {
                int value = matrix[row][col];
                if (value == SudokuBoard.EMPTY) {
                    continue;
                }
                if (firstSeenColByValue[value] == -1) {
                    firstSeenColByValue[value] = col;
                    continue;
                }
                violations.add(new ValidationViolation(
                        ViolationType.ROW_DUPLICATE,
                        row,
                        col,
                        value,
                        "Duplicate value " + value + " found in row " + row + "."
                ));
            }
        }
    }

    private static void validateColumns(int[][] matrix, List<ValidationViolation> violations) {
        for (int col = 0; col < SudokuBoard.SIZE; col++) {
            int[] firstSeenRowByValue = initializeIndexMap();
            for (int row = 0; row < SudokuBoard.SIZE; row++) {
                int value = matrix[row][col];
                if (value == SudokuBoard.EMPTY) {
                    continue;
                }
                if (firstSeenRowByValue[value] == -1) {
                    firstSeenRowByValue[value] = row;
                    continue;
                }
                violations.add(new ValidationViolation(
                        ViolationType.COLUMN_DUPLICATE,
                        row,
                        col,
                        value,
                        "Duplicate value " + value + " found in column " + col + "."
                ));
            }
        }
    }

    private static void validateBoxes(int[][] matrix, List<ValidationViolation> violations) {
        for (int boxRowStart = 0; boxRowStart < SudokuBoard.SIZE; boxRowStart += 3) {
            for (int boxColStart = 0; boxColStart < SudokuBoard.SIZE; boxColStart += 3) {
                int[] firstSeenByValue = initializeIndexMap();
                for (int row = boxRowStart; row < boxRowStart + 3; row++) {
                    for (int col = boxColStart; col < boxColStart + 3; col++) {
                        int value = matrix[row][col];
                        if (value == SudokuBoard.EMPTY) {
                            continue;
                        }
                        if (firstSeenByValue[value] == -1) {
                            firstSeenByValue[value] = 1;
                            continue;
                        }
                        violations.add(new ValidationViolation(
                                ViolationType.BOX_DUPLICATE,
                                row,
                                col,
                                value,
                                "Duplicate value " + value + " found in 3x3 box starting at ("
                                        + boxRowStart + "," + boxColStart + ")."
                        ));
                    }
                }
            }
        }
    }

    private static int[] initializeIndexMap() {
        int[] firstSeen = new int[SudokuBoard.MAX_VALUE + 1];
        for (int i = 0; i < firstSeen.length; i++) {
            firstSeen[i] = -1;
        }
        return firstSeen;
    }
}

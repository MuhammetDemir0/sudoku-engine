package com.sudokuengine.service;

import com.sudokuengine.exception.InvalidBoardException;
import com.sudokuengine.model.SudokuBoard;
import com.sudokuengine.model.ValidationViolation;
import com.sudokuengine.model.ViolationType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SudokuValidatorTest {

    private final SudokuValidator validator = new SudokuValidator();

    @Test
    void validBoardIsAccepted() {
        SudokuBoard board = new SudokuBoard(new int[][] {
                { 5, 3, 4, 6, 7, 8, 9, 1, 2 },
                { 6, 7, 2, 1, 9, 5, 3, 4, 8 },
                { 1, 9, 8, 3, 4, 2, 5, 6, 7 },
                { 8, 5, 9, 7, 6, 1, 4, 2, 3 },
                { 4, 2, 6, 8, 5, 3, 7, 9, 1 },
                { 7, 1, 3, 9, 2, 4, 8, 5, 6 },
                { 9, 6, 1, 5, 3, 7, 2, 8, 4 },
                { 2, 8, 7, 4, 1, 9, 6, 3, 5 },
                { 3, 4, 5, 2, 8, 6, 1, 7, 9 }
        });

        List<ValidationViolation> violations = validator.validate(board);

        assertTrue(violations.isEmpty());
        assertTrue(validator.isValid(board));
    }

    @Test
    void rowDuplicateIsDetected() {
        SudokuBoard board = new SudokuBoard(new int[][] {
                { 5, 0, 0, 5, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 }
        });

        List<ValidationViolation> violations = validator.validate(board);

        assertEquals(1, violations.size());
        assertEquals(ViolationType.ROW_DUPLICATE, violations.get(0).getType());
        assertFalse(validator.isValid(board));
    }

    @Test
    void columnDuplicateIsDetected() {
        SudokuBoard board = new SudokuBoard(new int[][] {
                { 7, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 7, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 }
        });

        List<ValidationViolation> violations = validator.validate(board);

        assertEquals(1, violations.size());
        assertEquals(ViolationType.COLUMN_DUPLICATE, violations.get(0).getType());
    }

    @Test
    void boxDuplicateIsDetected() {
        SudokuBoard board = new SudokuBoard(new int[][] {
                { 8, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 8, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 }
        });

        List<ValidationViolation> violations = validator.validate(board);

        assertEquals(1, violations.size());
        assertEquals(ViolationType.BOX_DUPLICATE, violations.get(0).getType());
    }

    @Test
    void multipleViolationsAreReportedTogether() {
        SudokuBoard board = new SudokuBoard(new int[][] {
                { 1, 0, 0, 1, 0, 0, 0, 0, 0 },
                { 0, 3, 0, 0, 0, 0, 0, 0, 2 },
                { 0, 0, 3, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 2 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0 }
        });

        List<ValidationViolation> violations = validator.validate(board);

        assertEquals(3, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getType() == ViolationType.ROW_DUPLICATE));
        assertTrue(violations.stream().anyMatch(v -> v.getType() == ViolationType.COLUMN_DUPLICATE));
        assertTrue(violations.stream().anyMatch(v -> v.getType() == ViolationType.BOX_DUPLICATE));
    }

    @Test
    void nullBoardIsRejected() {
        assertThrows(InvalidBoardException.class, () -> validator.validate(null));
    }
}

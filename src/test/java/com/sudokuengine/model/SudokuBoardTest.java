package com.sudokuengine.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SudokuBoardTest {

    @Test
    void constructorAcceptsOnlyNineByNineMatrix() {
        int[][] valid = createEmptyBoard();
        assertDoesNotThrow(() -> new SudokuBoard(valid));

        int[][] invalidRows = new int[8][9];
        assertThrows(IllegalArgumentException.class, () -> new SudokuBoard(invalidRows));

        int[][] invalidCols = new int[9][8];
        assertThrows(IllegalArgumentException.class, () -> new SudokuBoard(invalidCols));
    }

    @Test
    void constructorRejectsValuesOutsideZeroToNine() {
        int[][] boardWithNegative = createEmptyBoard();
        boardWithNegative[0][0] = -1;
        assertThrows(IllegalArgumentException.class, () -> new SudokuBoard(boardWithNegative));

        int[][] boardWithTooLarge = createEmptyBoard();
        boardWithTooLarge[1][1] = 10;
        assertThrows(IllegalArgumentException.class, () -> new SudokuBoard(boardWithTooLarge));
    }

    @Test
    void getAndSetRejectInvalidCoordinates() {
        SudokuBoard board = new SudokuBoard(createEmptyBoard());

        assertThrows(IllegalArgumentException.class, () -> board.getValue(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> board.getValue(9, 0));
        assertThrows(IllegalArgumentException.class, () -> board.getValue(0, 9));

        assertThrows(IllegalArgumentException.class, () -> board.setValue(-1, 0, 5));
        assertThrows(IllegalArgumentException.class, () -> board.setValue(0, 9, 5));
    }

    @Test
    void setRejectsValueOutsideZeroToNine() {
        SudokuBoard board = new SudokuBoard(createEmptyBoard());

        assertThrows(IllegalArgumentException.class, () -> board.setValue(0, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> board.setValue(0, 0, 10));
    }

    @Test
    void constructorUsesDefensiveCopyAgainstInputArrayChanges() {
        int[][] input = createEmptyBoard();
        input[0][0] = 5;

        SudokuBoard board = new SudokuBoard(input);
        input[0][0] = 9;

        assertEquals(5, board.getValue(0, 0));
    }

    @Test
    void toMatrixReturnsDefensiveCopy() {
        SudokuBoard board = new SudokuBoard(createEmptyBoard());
        int[][] exported = board.toMatrix();
        exported[0][0] = 7;

        assertEquals(0, board.getValue(0, 0));
    }

    @Test
    void copyReturnsIndependentBoardState() {
        int[][] input = createEmptyBoard();
        input[2][3] = 4;

        SudokuBoard original = new SudokuBoard(input);
        SudokuBoard copied = original.copy();

        assertNotSame(original, copied);
        assertEquals(4, copied.getValue(2, 3));

        copied.setValue(2, 3, 9);

        assertEquals(4, original.getValue(2, 3));
        assertEquals(9, copied.getValue(2, 3));
    }

    @Test
    void zeroRepresentsEmptyCell() {
        SudokuBoard board = new SudokuBoard(createEmptyBoard());
        assertEquals(SudokuBoard.EMPTY, board.getValue(4, 4));

        board.setValue(4, 4, 0);
        assertEquals(SudokuBoard.EMPTY, board.getValue(4, 4));
    }

    private static int[][] createEmptyBoard() {
        return new int[9][9];
    }
}

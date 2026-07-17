package com.sudokuengine.service;

import com.sudokuengine.model.Hint;
import com.sudokuengine.model.SudokuBoard;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HintServiceTest {

    private final HintService hintService = new HintService();
    private final BacktrackingSudokuSolver solver = new BacktrackingSudokuSolver();

    @Test
    void noHintIsReturnedForInvalidBoard() {
        SudokuBoard invalid = new SudokuBoard(new int[][] {
                { 5, 5, 0, 0, 7, 0, 0, 0, 0 },
                { 6, 0, 0, 1, 9, 5, 0, 0, 0 },
                { 0, 9, 8, 0, 0, 0, 0, 6, 0 },
                { 8, 0, 0, 0, 6, 0, 0, 0, 3 },
                { 4, 0, 0, 8, 0, 3, 0, 0, 1 },
                { 7, 0, 0, 0, 2, 0, 0, 0, 6 },
                { 0, 6, 0, 0, 0, 0, 2, 8, 0 },
                { 0, 0, 0, 4, 1, 9, 0, 0, 5 },
                { 0, 0, 0, 0, 8, 0, 0, 7, 9 }
        });

        assertTrue(hintService.findHint(invalid).isEmpty());
    }

    @Test
    void noHintIsReturnedForCompletedBoard() {
        SudokuBoard completed = solvedBoard();

        assertTrue(hintService.findHint(completed).isEmpty());
    }

    @Test
    void suggestionMatchesValidSolution() {
        SudokuBoard puzzle = puzzle();
        SudokuBoard solution = solver.solve(puzzle).getBoard().orElseThrow();

        Optional<Hint> hint = hintService.findHint(puzzle);

        assertTrue(hint.isPresent());
        Hint value = hint.orElseThrow();
        assertTrue(puzzle.read(value.getRow(), value.getCol()) == SudokuBoard.EMPTY);
        assertTrue(value.getValue() == solution.read(value.getRow(), value.getCol()));
        assertFalse(value.getReason().isBlank());
    }

    @Test
    void inputBoardIsNotMutated() {
        SudokuBoard puzzle = puzzle();
        int[][] snapshot = puzzle.toMatrix();

        hintService.findHint(puzzle);

        assertArrayEquals(snapshot, puzzle.toMatrix());
    }

    private static SudokuBoard puzzle() {
        return new SudokuBoard(new int[][] {
                { 5, 3, 0, 0, 7, 0, 0, 0, 0 },
                { 6, 0, 0, 1, 9, 5, 0, 0, 0 },
                { 0, 9, 8, 0, 0, 0, 0, 6, 0 },
                { 8, 0, 0, 0, 6, 0, 0, 0, 3 },
                { 4, 0, 0, 8, 0, 3, 0, 0, 1 },
                { 7, 0, 0, 0, 2, 0, 0, 0, 6 },
                { 0, 6, 0, 0, 0, 0, 2, 8, 0 },
                { 0, 0, 0, 4, 1, 9, 0, 0, 5 },
                { 0, 0, 0, 0, 8, 0, 0, 7, 9 }
        });
    }

    private static SudokuBoard solvedBoard() {
        return new SudokuBoard(new int[][] {
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
    }
}

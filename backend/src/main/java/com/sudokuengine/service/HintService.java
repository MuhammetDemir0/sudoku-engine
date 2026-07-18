package com.sudokuengine.service;

import com.sudokuengine.model.Hint;
import com.sudokuengine.model.SolveResult;
import com.sudokuengine.model.SudokuBoard;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Recommends a safe next move for the current board state.
 */
@Component
public class HintService {

    private final SudokuValidator validator;
    private final SudokuSolver solver;

    public HintService() {
        this(new SudokuValidator(), new MrvSudokuSolver());
    }

    public HintService(SudokuValidator validator, SudokuSolver solver) {
        this.validator = Objects.requireNonNull(validator, "Validator cannot be null.");
        this.solver = Objects.requireNonNull(solver, "Solver cannot be null.");
    }

    /**
     * Returns a safe next move when the board is valid, incomplete, and
     * solvable.
     */
    public Optional<Hint> findHint(SudokuBoard board) {
        if (board == null || !validator.isValid(board) || isCompleted(board)) {
            return Optional.empty();
        }

        SolveResult result = solver.solve(board);
        if (!result.isSolved()) {
            return Optional.empty();
        }

        SudokuBoard solution = result.getBoard().orElseThrow();
        Cell cell = selectEmptyCellWithFewestCandidates(board);
        if (cell == null) {
            return Optional.empty();
        }

        int value = solution.read(cell.row(), cell.col());
        return Optional.of(new Hint(
                cell.row(),
                cell.col(),
                value,
                "Cell (" + cell.row() + "," + cell.col() + ") can safely be " + value
                        + " based on the solved board."));
    }

    private static boolean isCompleted(SudokuBoard board) {
        for (int row = 0; row < SudokuBoard.SIZE; row++) {
            for (int col = 0; col < SudokuBoard.SIZE; col++) {
                if (board.read(row, col) == SudokuBoard.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Cell selectEmptyCellWithFewestCandidates(SudokuBoard board) {
        Cell best = null;
        List<Integer> bestCandidates = null;

        for (int row = 0; row < SudokuBoard.SIZE; row++) {
            for (int col = 0; col < SudokuBoard.SIZE; col++) {
                if (board.read(row, col) != SudokuBoard.EMPTY) {
                    continue;
                }

                List<Integer> candidates = candidatesFor(board, row, col);
                if (bestCandidates == null || candidates.size() < bestCandidates.size()) {
                    best = new Cell(row, col);
                    bestCandidates = candidates;
                    if (candidates.size() <= 1) {
                        return best;
                    }
                }
            }
        }

        return best;
    }

    private static List<Integer> candidatesFor(SudokuBoard board, int row, int col) {
        List<Integer> candidates = new ArrayList<>(SudokuBoard.MAX_VALUE);
        for (int value = 1; value <= SudokuBoard.MAX_VALUE; value++) {
            if (isValidMove(board, row, col, value)) {
                candidates.add(value);
            }
        }
        return candidates;
    }

    private static boolean isValidMove(SudokuBoard board, int row, int col, int value) {
        return isRowValid(board, row, value)
                && isColumnValid(board, col, value)
                && isBoxValid(board, row, col, value);
    }

    private static boolean isRowValid(SudokuBoard board, int row, int value) {
        for (int col = 0; col < SudokuBoard.SIZE; col++) {
            if (board.read(row, col) == value) {
                return false;
            }
        }
        return true;
    }

    private static boolean isColumnValid(SudokuBoard board, int col, int value) {
        for (int row = 0; row < SudokuBoard.SIZE; row++) {
            if (board.read(row, col) == value) {
                return false;
            }
        }
        return true;
    }

    private static boolean isBoxValid(SudokuBoard board, int row, int col, int value) {
        int boxStartRow = (row / 3) * 3;
        int boxStartCol = (col / 3) * 3;

        for (int r = boxStartRow; r < boxStartRow + 3; r++) {
            for (int c = boxStartCol; c < boxStartCol + 3; c++) {
                if (board.read(r, c) == value) {
                    return false;
                }
            }
        }
        return true;
    }

    private record Cell(int row, int col) {
    }
}

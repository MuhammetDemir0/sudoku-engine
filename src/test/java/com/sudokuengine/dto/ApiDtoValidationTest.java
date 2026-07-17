package com.sudokuengine.dto;

import com.sudokuengine.model.Difficulty;
import com.sudokuengine.model.SudokuBoard;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiDtoValidationTest {

    private final Validator validator = createValidator();

    @Test
    void generateRequestRequiresDifficulty() {
        GeneratePuzzleRequest request = new GeneratePuzzleRequest(null);

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void solveRequestAcceptsValidBoard() {
        SolvePuzzleRequest request = new SolvePuzzleRequest(validBoardPayload(), true, SolverType.MRV);

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void solveRequestRejectsInvalidBoardShape() {
        List<List<Integer>> board = validBoardPayload();
        board.removeLast();
        SolvePuzzleRequest request = new SolvePuzzleRequest(board, false, SolverType.BACKTRACKING);

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void validateRequestRejectsOutOfRangeCellValue() {
        List<List<Integer>> board = validBoardPayload();
        board.getFirst().set(0, 10);
        ValidatePuzzleRequest request = new ValidatePuzzleRequest(board);

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void hintRequestRejectsNullCells() {
        List<List<Integer>> board = validBoardPayload();
        board.get(3).set(4, null);
        HintRequest request = new HintRequest(board);

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void requestCanConvertToDomainBoard() {
        int[][] expected = {
                { 5, 3, 0, 0, 7, 0, 0, 0, 0 },
                { 6, 0, 0, 1, 9, 5, 0, 0, 0 },
                { 0, 9, 8, 0, 0, 0, 0, 6, 0 },
                { 8, 0, 0, 0, 6, 0, 0, 0, 3 },
                { 4, 0, 0, 8, 0, 3, 0, 0, 1 },
                { 7, 0, 0, 0, 2, 0, 0, 0, 6 },
                { 0, 6, 0, 0, 0, 0, 2, 8, 0 },
                { 0, 0, 0, 4, 1, 9, 0, 0, 5 },
                { 0, 0, 0, 0, 8, 0, 0, 7, 9 }
        };
        SolvePuzzleRequest request = new SolvePuzzleRequest(validBoardPayload(), null, null);

        SudokuBoard board = request.toBoard();

        assertArrayEquals(expected, board.toMatrix());
        assertFalse(request.shouldIncludeSteps());
        assertTrue(request.requestedSolver() == SolverType.MRV);
    }

    @Test
    void boardMapperReturnsApiMatrixCopy() {
        SudokuBoard board = new SudokuBoard(new int[SudokuBoard.SIZE][SudokuBoard.SIZE]);

        List<List<Integer>> payload = BoardDtoMapper.fromDomain(board);

        assertTrue(payload.size() == SudokuBoard.SIZE);
        assertTrue(payload.getFirst().size() == SudokuBoard.SIZE);
    }

    @Test
    void generateRequestWithDifficultyIsValid() {
        GeneratePuzzleRequest request = new GeneratePuzzleRequest(Difficulty.EASY);

        assertTrue(validator.validate(request).isEmpty());
    }

    private static Validator createValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }

    private static List<List<Integer>> validBoardPayload() {
        int[][] cells = {
                { 5, 3, 0, 0, 7, 0, 0, 0, 0 },
                { 6, 0, 0, 1, 9, 5, 0, 0, 0 },
                { 0, 9, 8, 0, 0, 0, 0, 6, 0 },
                { 8, 0, 0, 0, 6, 0, 0, 0, 3 },
                { 4, 0, 0, 8, 0, 3, 0, 0, 1 },
                { 7, 0, 0, 0, 2, 0, 0, 0, 6 },
                { 0, 6, 0, 0, 0, 0, 2, 8, 0 },
                { 0, 0, 0, 4, 1, 9, 0, 0, 5 },
                { 0, 0, 0, 0, 8, 0, 0, 7, 9 }
        };
        List<List<Integer>> board = new ArrayList<>(SudokuBoard.SIZE);
        for (int[] row : cells) {
            List<Integer> values = new ArrayList<>(SudokuBoard.SIZE);
            for (int value : row) {
                values.add(value);
            }
            board.add(values);
        }
        return board;
    }
}

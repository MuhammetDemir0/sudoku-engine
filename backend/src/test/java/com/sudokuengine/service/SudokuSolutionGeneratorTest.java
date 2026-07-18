package com.sudokuengine.service;

import com.sudokuengine.model.SudokuBoard;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SudokuSolutionGeneratorTest {

    private final SudokuValidator validator = new SudokuValidator();

    @Test
    void generatedBoardIsCompletelyFilledAndValid() {
        SudokuSolutionGenerator generator = new SudokuSolutionGenerator(new Random(42L));

        SudokuBoard board = generator.generate();

        assertTrue(validator.isValid(board));
        assertTrue(isCompletelyFilled(board));
    }

    @Test
    void fixedSeedProducesDeterministicResults() {
        SudokuSolutionGenerator generatorA = new SudokuSolutionGenerator(new Random(1234L));
        SudokuSolutionGenerator generatorB = new SudokuSolutionGenerator(new Random(1234L));

        SudokuBoard boardA = generatorA.generate();
        SudokuBoard boardB = generatorB.generate();

        assertArrayEquals(boardA.toMatrix(), boardB.toMatrix());
    }

    @Test
    void oneHundredGeneratedBoardsAreValidAndFilled() {
        SudokuSolutionGenerator generator = new SudokuSolutionGenerator(new Random(2026L));

        for (int i = 0; i < 100; i++) {
            SudokuBoard board = generator.generate();
            assertTrue(validator.isValid(board));
            assertTrue(isCompletelyFilled(board));
        }
    }

    @Test
    void rejectsNullRandomDependency() {
        assertThrows(NullPointerException.class, () -> new SudokuSolutionGenerator(null));
    }

    private static boolean isCompletelyFilled(SudokuBoard board) {
        for (int row = 0; row < SudokuBoard.SIZE; row++) {
            for (int col = 0; col < SudokuBoard.SIZE; col++) {
                if (board.read(row, col) == SudokuBoard.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }
}

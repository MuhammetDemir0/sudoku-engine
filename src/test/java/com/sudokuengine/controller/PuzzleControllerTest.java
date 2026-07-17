package com.sudokuengine.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sudokuengine.model.Difficulty;
import com.sudokuengine.model.SudokuBoard;
import com.sudokuengine.model.SudokuPuzzle;
import com.sudokuengine.service.BacktrackingSudokuSolver;
import com.sudokuengine.service.UniqueSudokuPuzzleGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PuzzleController.class)
class PuzzleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UniqueSudokuPuzzleGenerator puzzleGenerator;

    private final BacktrackingSudokuSolver solver = new BacktrackingSudokuSolver();

    @Test
    void validRequestReturnsPuzzleForDifficulty() throws Exception {
        when(puzzleGenerator.generate(Difficulty.EASY)).thenReturn(samplePuzzle());

        mockMvc.perform(post("/api/v1/puzzles/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"difficulty\":\"EASY\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.difficulty").value("EASY"))
                .andExpect(jsonPath("$.puzzle").isArray())
                .andExpect(jsonPath("$.puzzle.length()").value(SudokuBoard.SIZE))
                .andExpect(jsonPath("$.puzzle[0].length()").value(SudokuBoard.SIZE));

        verify(puzzleGenerator).generate(Difficulty.EASY);
    }

    @Test
    void invalidDifficultyReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/puzzles/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"difficulty\":\"IMPOSSIBLE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generatedPuzzleHasUniqueSolution() throws Exception {
        when(puzzleGenerator.generate(Difficulty.MEDIUM)).thenReturn(samplePuzzle());

        MvcResult result = mockMvc.perform(post("/api/v1/puzzles/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"difficulty\":\"MEDIUM\"}"))
                .andExpect(status().isOk())
                .andReturn();

        SudokuBoard responsePuzzle = responsePuzzle(result);

        assertEquals(1, solver.countSolutions(responsePuzzle, 2));
    }

    @Test
    void fullSolutionIsNotExposed() throws Exception {
        when(puzzleGenerator.generate(Difficulty.HARD)).thenReturn(samplePuzzle());

        MvcResult result = mockMvc.perform(post("/api/v1/puzzles/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"difficulty\":\"HARD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solution").doesNotExist())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());

        assertFalse(body.has("solution"));
    }

    @Test
    void missingDifficultyReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/puzzles/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private SudokuBoard responsePuzzle(MvcResult result) throws Exception {
        JsonNode puzzle = objectMapper.readTree(result.getResponse().getContentAsString()).get("puzzle");
        int[][] cells = new int[SudokuBoard.SIZE][SudokuBoard.SIZE];
        for (int row = 0; row < SudokuBoard.SIZE; row++) {
            for (int col = 0; col < SudokuBoard.SIZE; col++) {
                cells[row][col] = puzzle.get(row).get(col).asInt();
            }
        }
        return new SudokuBoard(cells);
    }

    private static SudokuPuzzle samplePuzzle() {
        return new SudokuPuzzle(
                new SudokuBoard(new int[][] {
                        { 5, 3, 0, 0, 7, 0, 0, 0, 0 },
                        { 6, 0, 0, 1, 9, 5, 0, 0, 0 },
                        { 0, 9, 8, 0, 0, 0, 0, 6, 0 },
                        { 8, 0, 0, 0, 6, 0, 0, 0, 3 },
                        { 4, 0, 0, 8, 0, 3, 0, 0, 1 },
                        { 7, 0, 0, 0, 2, 0, 0, 0, 6 },
                        { 0, 6, 0, 0, 0, 0, 2, 8, 0 },
                        { 0, 0, 0, 4, 1, 9, 0, 0, 5 },
                        { 0, 0, 0, 0, 8, 0, 0, 7, 9 }
                }),
                new SudokuBoard(new int[][] {
                        { 5, 3, 4, 6, 7, 8, 9, 1, 2 },
                        { 6, 7, 2, 1, 9, 5, 3, 4, 8 },
                        { 1, 9, 8, 3, 4, 2, 5, 6, 7 },
                        { 8, 5, 9, 7, 6, 1, 4, 2, 3 },
                        { 4, 2, 6, 8, 5, 3, 7, 9, 1 },
                        { 7, 1, 3, 9, 2, 4, 8, 5, 6 },
                        { 9, 6, 1, 5, 3, 7, 2, 8, 4 },
                        { 2, 8, 7, 4, 1, 9, 6, 3, 5 },
                        { 3, 4, 5, 2, 8, 6, 1, 7, 9 }
                }));
    }
}

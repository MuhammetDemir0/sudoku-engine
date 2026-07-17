package com.sudokuengine.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sudokuengine.model.Difficulty;
import com.sudokuengine.model.SudokuBoard;
import com.sudokuengine.model.SudokuPuzzle;
import com.sudokuengine.service.BacktrackingSudokuSolver;
import com.sudokuengine.service.MrvSudokuSolver;
import com.sudokuengine.service.SudokuValidator;
import com.sudokuengine.service.UniqueSudokuPuzzleGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PuzzleController.class)
@Import({ ApiExceptionHandler.class, BacktrackingSudokuSolver.class, MrvSudokuSolver.class, SudokuValidator.class })
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

    @Test
    void solvableBoardIsSolved() throws Exception {
        mockMvc.perform(post("/api/v1/puzzles/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(solvePayload(samplePuzzle().getPuzzle(), false, "MRV")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solved").value(true))
                .andExpect(jsonPath("$.board").isArray())
                .andExpect(jsonPath("$.board[0][2]").value(4))
                .andExpect(jsonPath("$.metrics.visitedNodes").isNumber())
                .andExpect(jsonPath("$.metrics.backtracks").isNumber())
                .andExpect(jsonPath("$.metrics.maxRecursionDepth").isNumber())
                .andExpect(jsonPath("$.metrics.elapsedNanos").isNumber());
    }

    @Test
    void invalidBoardReturnsBadRequest() throws Exception {
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

        mockMvc.perform(post("/api/v1/puzzles/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(solvePayload(invalid, false, "BACKTRACKING")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void includeStepsTrueReturnsSolvingSteps() throws Exception {
        mockMvc.perform(post("/api/v1/puzzles/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(solvePayload(samplePuzzle().getPuzzle(), true, "BACKTRACKING")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solved").value(true))
                .andExpect(jsonPath("$.steps").isArray())
                .andExpect(jsonPath("$.steps.length()").isNumber())
                .andExpect(jsonPath("$.steps[0].type").exists())
                .andExpect(jsonPath("$.steps[0].sequence").value(1));
    }

    @Test
    void unsolvableBoardReturnsUnsolvedResponseWithMetrics() throws Exception {
        SudokuBoard unsolvable = new SudokuBoard(new int[][] {
                { 0, 3, 4, 6, 7, 8, 9, 1, 2 },
                { 5, 7, 2, 1, 9, 0, 3, 4, 8 },
                { 1, 9, 8, 3, 4, 2, 5, 6, 7 },
                { 8, 5, 9, 7, 6, 1, 4, 2, 3 },
                { 4, 2, 6, 8, 5, 3, 7, 9, 1 },
                { 7, 1, 3, 9, 2, 4, 8, 5, 6 },
                { 9, 6, 1, 5, 3, 7, 2, 8, 4 },
                { 2, 8, 7, 4, 1, 9, 6, 3, 5 },
                { 3, 4, 5, 2, 8, 6, 1, 7, 9 }
        });

        mockMvc.perform(post("/api/v1/puzzles/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(solvePayload(unsolvable, false, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solved").value(false))
                .andExpect(jsonPath("$.board").doesNotExist())
                .andExpect(jsonPath("$.metrics.visitedNodes").isNumber());
    }

    @Test
    void invalidSolverParameterReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/puzzles/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(solvePayload(samplePuzzle().getPuzzle(), false, "NOPE")))
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

    private String solvePayload(SudokuBoard board, boolean includeSteps, String solver) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("board", board.toMatrix());
        payload.put("includeSteps", includeSteps);
        if (solver != null) {
            payload.put("solver", solver);
        }
        return objectMapper.writeValueAsString(payload);
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

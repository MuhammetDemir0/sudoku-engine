package com.sudokuengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sudokuengine.model.Difficulty;
import com.sudokuengine.model.SudokuBoard;
import com.sudokuengine.model.SudokuPuzzle;
import com.sudokuengine.service.UniqueSudokuPuzzleGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
class PuzzleApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UniqueSudokuPuzzleGenerator puzzleGenerator;

    @Test
    void generateEndpointReturnsPuzzleAndDoesNotExposeSolution() throws Exception {
        when(puzzleGenerator.generate(Difficulty.MEDIUM)).thenReturn(samplePuzzle());

        mockMvc.perform(post("/api/v1/puzzles/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"difficulty\":\"MEDIUM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.difficulty").value("MEDIUM"))
                .andExpect(jsonPath("$.puzzle").isArray())
                .andExpect(jsonPath("$.puzzle.length()").value(SudokuBoard.SIZE))
                .andExpect(jsonPath("$.puzzle[0].length()").value(SudokuBoard.SIZE))
                .andExpect(jsonPath("$.solution").doesNotExist());
    }

    @Test
    void generateEndpointRejectsInvalidDifficultyWithCommonErrorBody() throws Exception {
        mockMvc.perform(post("/api/v1/puzzles/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"difficulty\":\"IMPOSSIBLE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request body is malformed or contains unsupported values."))
                .andExpect(jsonPath("$.path").value("/api/v1/puzzles/generate"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist());
    }

    @Test
    void solveEndpointSolvesBoardAndReturnsStepsAndMetrics() throws Exception {
        mockMvc.perform(post("/api/v1/puzzles/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(solvePayload(samplePuzzle().getPuzzle(), true, "MRV")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solved").value(true))
                .andExpect(jsonPath("$.board[0][2]").value(4))
                .andExpect(jsonPath("$.metrics.visitedNodes").isNumber())
                .andExpect(jsonPath("$.metrics.backtracks").isNumber())
                .andExpect(jsonPath("$.metrics.maxRecursionDepth").isNumber())
                .andExpect(jsonPath("$.metrics.elapsedNanos").isNumber())
                .andExpect(jsonPath("$.steps").isArray())
                .andExpect(jsonPath("$.steps[0].type").exists())
                .andExpect(jsonPath("$.steps[0].row").isNumber())
                .andExpect(jsonPath("$.steps[0].col").isNumber())
                .andExpect(jsonPath("$.steps[0].value").isNumber());
    }

    @Test
    void solveEndpointRejectsInvalidBoardWithCommonErrorBody() throws Exception {
        mockMvc.perform(post("/api/v1/puzzles/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(solvePayload(rowDuplicateBoard(), false, "BACKTRACKING")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/puzzles/solve"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void validateEndpointReturnsAllRuleViolationTypes() throws Exception {
        mockMvc.perform(post("/api/v1/puzzles/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(boardPayload(boardWithRowColumnAndBoxViolations())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.violations.length()").value(3))
                .andExpect(jsonPath("$.violations[?(@.type == 'ROW_DUPLICATE')]").exists())
                .andExpect(jsonPath("$.violations[?(@.type == 'COLUMN_DUPLICATE')]").exists())
                .andExpect(jsonPath("$.violations[?(@.type == 'BOX_DUPLICATE')]").exists());
    }

    @Test
    void validateEndpointRejectsMalformedRequestWithCommonErrorBody() throws Exception {
        mockMvc.perform(post("/api/v1/puzzles/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request body is malformed or contains unsupported values."))
                .andExpect(jsonPath("$.path").value("/api/v1/puzzles/validate"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void hintEndpointReturnsSafeMoveForValidBoard() throws Exception {
        mockMvc.perform(post("/api/v1/puzzles/hint")
                .contentType(MediaType.APPLICATION_JSON)
                .content(boardPayload(samplePuzzle().getPuzzle())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.row").value(4))
                .andExpect(jsonPath("$.col").value(4))
                .andExpect(jsonPath("$.value").value(5))
                .andExpect(jsonPath("$.reason").isNotEmpty());
    }

    @Test
    void hintEndpointRejectsInvalidBoardWithClearErrorBody() throws Exception {
        mockMvc.perform(post("/api/v1/puzzles/hint")
                .contentType(MediaType.APPLICATION_JSON)
                .content(boardPayload(rowDuplicateBoard())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Board is invalid; fix rule violations before requesting a hint."))
                .andExpect(jsonPath("$.path").value("/api/v1/puzzles/hint"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void hintEndpointReturnsNoContentForCompletedBoard() throws Exception {
        mockMvc.perform(post("/api/v1/puzzles/hint")
                .contentType(MediaType.APPLICATION_JSON)
                .content(boardPayload(samplePuzzle().getSolution())))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    private String solvePayload(SudokuBoard board, boolean includeSteps, String solver) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("board", board.toMatrix());
        payload.put("includeSteps", includeSteps);
        payload.put("solver", solver);
        return objectMapper.writeValueAsString(payload);
    }

    private String boardPayload(SudokuBoard board) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("board", board.toMatrix());
        return objectMapper.writeValueAsString(payload);
    }

    private static SudokuBoard rowDuplicateBoard() {
        return new SudokuBoard(new int[][] {
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
    }

    private static SudokuBoard boardWithRowColumnAndBoxViolations() {
        return new SudokuBoard(new int[][] {
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

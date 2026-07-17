package com.sudokuengine.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void swaggerUiIsAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void openApiSpecDocumentsAllPuzzleEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/puzzles/generate")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/puzzles/solve")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/puzzles/validate")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/puzzles/hint")))
                .andExpect(jsonPath("$.paths['/api/v1/puzzles/generate'].post.summary")
                        .value("Generate a Sudoku puzzle"))
                .andExpect(jsonPath("$.paths['/api/v1/puzzles/solve'].post.summary")
                        .value("Solve a Sudoku board"))
                .andExpect(jsonPath("$.paths['/api/v1/puzzles/validate'].post.summary")
                        .value("Validate a Sudoku board"))
                .andExpect(jsonPath("$.paths['/api/v1/puzzles/hint'].post.summary")
                        .value("Get a safe next move"));
    }

    @Test
    void openApiSpecIncludesExamplesAndErrorResponses() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/puzzles/generate'].post.requestBody.content"
                        + "['application/json'].examples['Medium puzzle'].value.difficulty").value("MEDIUM"))
                .andExpect(jsonPath("$.paths['/api/v1/puzzles/generate'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/puzzles/generate'].post.responses['503']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/puzzles/solve'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/puzzles/validate'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/puzzles/hint'].post.responses['204']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/puzzles/hint'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/puzzles/hint'].post.responses['422'].content"
                        + "['application/json'].examples['No hint'].value.status").value(422))
                .andExpect(jsonPath("$.components.schemas.ApiErrorResponse").exists());
    }
}

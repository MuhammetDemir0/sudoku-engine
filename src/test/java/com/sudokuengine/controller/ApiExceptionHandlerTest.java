package com.sudokuengine.controller;

import com.sudokuengine.dto.GeneratePuzzleRequest;
import com.sudokuengine.exception.InvalidBoardException;
import com.sudokuengine.exception.InvalidCellValueException;
import com.sudokuengine.exception.InvalidCoordinateException;
import com.sudokuengine.exception.PuzzleGenerationException;
import com.sudokuengine.exception.UnsolvablePuzzleException;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void validationErrorsUseCommonFormat() throws Exception {
        mockMvc.perform(post("/test/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request validation failed."))
                .andExpect(jsonPath("$.path").value("/test/validate"))
                .andExpect(jsonPath("$.validationErrors.difficulty").exists())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist());
    }

    @Test
    void jsonParsingErrorsUseCommonFormat() throws Exception {
        mockMvc.perform(post("/test/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request body is malformed or contains unsupported values."))
                .andExpect(jsonPath("$.validationErrors").isMap())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void invalidBoardMapsToBadRequest() throws Exception {
        mockMvc.perform(get("/test/invalid-board"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("bad board"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void invalidCellValueMapsToBadRequest() throws Exception {
        mockMvc.perform(get("/test/invalid-cell"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("bad cell"));
    }

    @Test
    void invalidCoordinateMapsToBadRequest() throws Exception {
        mockMvc.perform(get("/test/invalid-coordinate"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("bad coordinate"));
    }

    @Test
    void unsolvablePuzzleMapsToUnprocessableEntity() throws Exception {
        mockMvc.perform(get("/test/unsolvable"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("unsolvable"));
    }

    @Test
    void puzzleGenerationFailureMapsToServiceUnavailable() throws Exception {
        mockMvc.perform(get("/test/generation"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("generation failed"));
    }

    @Test
    void unexpectedErrorsUseGenericMessageWithoutStackTrace() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred."))
                .andExpect(jsonPath("$.path").value("/test/unexpected"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist());
    }

    @RestController
    @RequestMapping("/test")
    public static class TestController {

        @PostMapping("/validate")
        void validate(@Valid @RequestBody GeneratePuzzleRequest request) {
        }

        @GetMapping("/invalid-board")
        void invalidBoard() {
            throw new InvalidBoardException("bad board");
        }

        @GetMapping("/invalid-cell")
        void invalidCell() {
            throw new InvalidCellValueException("bad cell");
        }

        @GetMapping("/invalid-coordinate")
        void invalidCoordinate() {
            throw new InvalidCoordinateException("bad coordinate");
        }

        @GetMapping("/unsolvable")
        void unsolvable() {
            throw new UnsolvablePuzzleException("unsolvable");
        }

        @GetMapping("/generation")
        void generation() {
            throw new PuzzleGenerationException("generation failed");
        }

        @GetMapping("/unexpected")
        void unexpected() {
            throw new IllegalStateException("technical details");
        }
    }
}

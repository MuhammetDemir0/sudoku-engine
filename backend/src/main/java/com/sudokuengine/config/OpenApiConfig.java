package com.sudokuengine.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sudokuengine.controller.OpenApiExamples;
import com.sudokuengine.dto.ApiErrorResponse;
import com.sudokuengine.dto.GeneratePuzzleRequest;
import com.sudokuengine.dto.GeneratePuzzleResponse;
import com.sudokuengine.dto.HintRequest;
import com.sudokuengine.dto.HintResponse;
import com.sudokuengine.dto.SolvePuzzleRequest;
import com.sudokuengine.dto.SolvePuzzleResponse;
import com.sudokuengine.dto.ValidatePuzzleRequest;
import com.sudokuengine.dto.ValidatePuzzleResponse;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String APPLICATION_JSON = "application/json";

    @Bean
    public OpenAPI sudokuEngineOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sudoku Engine API")
                        .description("A full-stack Sudoku generation, solving and algorithm visualization platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sudoku Engine")
                                .url("https://github.com/MuhammetDemir0/sudoku-engine"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }

    @Bean
    public OpenApiCustomizer puzzleEndpointDocumentation(ObjectMapper objectMapper) {
        return openApi -> {
            ensureComponents(openApi);
            openApi.addTagsItem(new Tag()
                    .name("Puzzles")
                    .description("Generate, solve, validate, and request hints for Sudoku boards."));

            documentGenerate(openApi, objectMapper);
            documentSolve(openApi, objectMapper);
            documentValidate(openApi, objectMapper);
            documentHint(openApi, objectMapper);
        };
    }

    private static void documentGenerate(OpenAPI openApi, ObjectMapper objectMapper) {
        Operation operation = postOperation(openApi, "/api/v1/puzzles/generate");
        if (operation == null) {
            return;
        }
        operation.summary("Generate a Sudoku puzzle")
                .description("Generates a puzzle for the requested difficulty. The full solution is intentionally omitted.")
                .addTagsItem("Puzzles")
                .requestBody(requestBody(
                        "Difficulty to use while generating the puzzle.",
                        GeneratePuzzleRequest.class,
                        "Medium puzzle",
                        OpenApiExamples.GENERATE_REQUEST,
                        objectMapper))
                .responses(new ApiResponses()
                        .addApiResponse("200", response(
                                "Puzzle generated successfully.",
                                GeneratePuzzleResponse.class,
                                "Generated puzzle",
                                OpenApiExamples.GENERATE_RESPONSE,
                                objectMapper))
                        .addApiResponse("400", errorResponse(
                                "The request is malformed or the difficulty value is not supported.",
                                "Validation error",
                                OpenApiExamples.ERROR_400,
                                objectMapper))
                        .addApiResponse("503", errorResponse(
                                "A puzzle could not be generated with the configured constraints.",
                                null,
                                null,
                                objectMapper))
                        .addApiResponse("500", errorResponse(
                                "Unexpected server error.",
                                "Unexpected error",
                                OpenApiExamples.ERROR_500,
                                objectMapper)));
    }

    private static void documentSolve(OpenAPI openApi, ObjectMapper objectMapper) {
        Operation operation = postOperation(openApi, "/api/v1/puzzles/solve");
        if (operation == null) {
            return;
        }
        operation.summary("Solve a Sudoku board")
                .description("Solves the supplied board with the requested solver and optionally returns solving steps.")
                .addTagsItem("Puzzles")
                .requestBody(requestBody(
                        "Board to solve. Use 0 for empty cells.",
                        SolvePuzzleRequest.class,
                        "MRV with steps",
                        OpenApiExamples.SOLVE_REQUEST,
                        objectMapper))
                .responses(new ApiResponses()
                        .addApiResponse("200", response(
                                "Solver run completed. If the puzzle is unsolvable, solved is false.",
                                SolvePuzzleResponse.class,
                                "Solved puzzle",
                                OpenApiExamples.SOLVE_RESPONSE,
                                objectMapper))
                        .addApiResponse("400", errorResponse(
                                "The board shape, cell values, or solver parameter is invalid.",
                                "Validation error",
                                OpenApiExamples.ERROR_400,
                                objectMapper))
                        .addApiResponse("500", errorResponse(
                                "Unexpected server error.",
                                "Unexpected error",
                                OpenApiExamples.ERROR_500,
                                objectMapper)));
    }

    private static void documentValidate(OpenAPI openApi, ObjectMapper objectMapper) {
        Operation operation = postOperation(openApi, "/api/v1/puzzles/validate");
        if (operation == null) {
            return;
        }
        operation.summary("Validate a Sudoku board")
                .description("Returns every row, column, and box rule violation found on the supplied board.")
                .addTagsItem("Puzzles")
                .requestBody(requestBody(
                        "Board to validate. Use 0 for empty cells.",
                        ValidatePuzzleRequest.class,
                        "Board with a duplicate",
                        OpenApiExamples.VALIDATE_REQUEST,
                        objectMapper))
                .responses(new ApiResponses()
                        .addApiResponse("200", response(
                                "Validation completed.",
                                ValidatePuzzleResponse.class,
                                "Violation response",
                                OpenApiExamples.VALIDATE_RESPONSE,
                                objectMapper))
                        .addApiResponse("400", errorResponse(
                                "The board shape or cell values are invalid.",
                                "Validation error",
                                OpenApiExamples.ERROR_400,
                                objectMapper))
                        .addApiResponse("500", errorResponse(
                                "Unexpected server error.",
                                "Unexpected error",
                                OpenApiExamples.ERROR_500,
                                objectMapper)));
    }

    private static void documentHint(OpenAPI openApi, ObjectMapper objectMapper) {
        Operation operation = postOperation(openApi, "/api/v1/puzzles/hint");
        if (operation == null) {
            return;
        }
        operation.summary("Get a safe next move")
                .description("Returns a cell, value, and explanation for a valid incomplete board.")
                .addTagsItem("Puzzles")
                .requestBody(requestBody(
                        "Current board state. Use 0 for empty cells.",
                        HintRequest.class,
                        "Incomplete board",
                        OpenApiExamples.HINT_REQUEST,
                        objectMapper))
                .responses(new ApiResponses()
                        .addApiResponse("200", response(
                                "Hint found.",
                                HintResponse.class,
                                "Hint",
                                OpenApiExamples.HINT_RESPONSE,
                                objectMapper))
                        .addApiResponse("204", new ApiResponse().description("The board is already complete."))
                        .addApiResponse("400", errorResponse(
                                "The board is malformed or violates Sudoku rules.",
                                "Invalid board",
                                OpenApiExamples.ERROR_400,
                                objectMapper))
                        .addApiResponse("422", errorResponse(
                                "The board is valid but no safe hint is available.",
                                "No hint",
                                OpenApiExamples.ERROR_422,
                                objectMapper))
                        .addApiResponse("500", errorResponse(
                                "Unexpected server error.",
                                "Unexpected error",
                                OpenApiExamples.ERROR_500,
                                objectMapper)));
    }

    private static Operation postOperation(OpenAPI openApi, String path) {
        if (openApi.getPaths() == null) {
            return null;
        }
        PathItem pathItem = openApi.getPaths().get(path);
        return pathItem == null ? null : pathItem.getPost();
    }

    private static RequestBody requestBody(
            String description,
            Class<?> schemaType,
            String exampleName,
            String exampleJson,
            ObjectMapper objectMapper) {
        return new RequestBody()
                .description(description)
                .required(true)
                .content(jsonContent(schemaType, exampleName, exampleJson, objectMapper));
    }

    private static ApiResponse response(
            String description,
            Class<?> schemaType,
            String exampleName,
            String exampleJson,
            ObjectMapper objectMapper) {
        return new ApiResponse()
                .description(description)
                .content(jsonContent(schemaType, exampleName, exampleJson, objectMapper));
    }

    private static ApiResponse errorResponse(
            String description,
            String exampleName,
            String exampleJson,
            ObjectMapper objectMapper) {
        return response(description, ApiErrorResponse.class, exampleName, exampleJson, objectMapper);
    }

    private static Content jsonContent(
            Class<?> schemaType,
            String exampleName,
            String exampleJson,
            ObjectMapper objectMapper) {
        MediaType mediaType = new MediaType().schema(schemaRef(schemaType));
        if (exampleName != null && exampleJson != null) {
            mediaType.addExamples(exampleName, new Example().value(jsonValue(exampleJson, objectMapper)));
        }
        return new Content().addMediaType(APPLICATION_JSON, mediaType);
    }

    private static Schema<?> schemaRef(Class<?> schemaType) {
        return new Schema<>().$ref("#/components/schemas/" + schemaType.getSimpleName());
    }

    private static Object jsonValue(String json, ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Invalid OpenAPI example JSON.", ex);
        }
    }

    private static void ensureComponents(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.components(new Components());
        }
        openApi.getComponents().addSchemas("ApiErrorResponse", new Schema<>()
                .type("object")
                .description("Common API error response."));
    }
}

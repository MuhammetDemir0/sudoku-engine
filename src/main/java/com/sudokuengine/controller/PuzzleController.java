package com.sudokuengine.controller;

import com.sudokuengine.dto.GeneratePuzzleRequest;
import com.sudokuengine.dto.GeneratePuzzleResponse;
import com.sudokuengine.dto.ApiErrorResponse;
import com.sudokuengine.dto.HintRequest;
import com.sudokuengine.dto.HintResponse;
import com.sudokuengine.dto.SolvePuzzleRequest;
import com.sudokuengine.dto.SolvePuzzleResponse;
import com.sudokuengine.dto.ValidatePuzzleRequest;
import com.sudokuengine.dto.ValidatePuzzleResponse;
import com.sudokuengine.exception.InvalidBoardException;
import com.sudokuengine.model.SolveResult;
import com.sudokuengine.model.SudokuBoard;
import com.sudokuengine.model.SudokuPuzzle;
import com.sudokuengine.service.BacktrackingSudokuSolver;
import com.sudokuengine.service.HintService;
import com.sudokuengine.service.MrvSudokuSolver;
import com.sudokuengine.service.SudokuValidator;
import com.sudokuengine.service.UniqueSudokuPuzzleGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for Sudoku puzzle generation.
 */
@RestController
@RequestMapping("/api/v1/puzzles")
@Tag(name = "Puzzles", description = "Generate, solve, validate, and request hints for Sudoku boards.")
public class PuzzleController {

    private final UniqueSudokuPuzzleGenerator puzzleGenerator;
    private final BacktrackingSudokuSolver backtrackingSolver;
    private final MrvSudokuSolver mrvSolver;
    private final SudokuValidator validator;
    private final HintService hintService;

    public PuzzleController(
            UniqueSudokuPuzzleGenerator puzzleGenerator,
            BacktrackingSudokuSolver backtrackingSolver,
            MrvSudokuSolver mrvSolver,
            SudokuValidator validator,
            HintService hintService) {
        this.puzzleGenerator = puzzleGenerator;
        this.backtrackingSolver = backtrackingSolver;
        this.mrvSolver = mrvSolver;
        this.validator = validator;
        this.hintService = hintService;
    }

    @PostMapping("/generate")
    @Operation(
            summary = "Generate a Sudoku puzzle",
            description = "Generates a puzzle for the requested difficulty. The full solution is intentionally omitted.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Difficulty to use while generating the puzzle.",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GeneratePuzzleRequest.class),
                    examples = @ExampleObject(name = "Medium puzzle", value = OpenApiExamples.GENERATE_REQUEST)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Puzzle generated successfully.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GeneratePuzzleResponse.class),
                            examples = @ExampleObject(name = "Generated puzzle", value = OpenApiExamples.GENERATE_RESPONSE))),
            @ApiResponse(
                    responseCode = "400",
                    description = "The request is malformed or the difficulty value is not supported.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(name = "Validation error", value = OpenApiExamples.ERROR_400))),
            @ApiResponse(
                    responseCode = "503",
                    description = "A puzzle could not be generated with the configured constraints.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(name = "Unexpected error", value = OpenApiExamples.ERROR_500)))
    })
    public ResponseEntity<GeneratePuzzleResponse> generate(@Valid @RequestBody GeneratePuzzleRequest request) {
        SudokuPuzzle puzzle = puzzleGenerator.generate(request.difficulty());
        return ResponseEntity.ok(GeneratePuzzleResponse.fromDomain(puzzle, request.difficulty()));
    }

    @PostMapping("/solve")
    @Operation(
            summary = "Solve a Sudoku board",
            description = "Solves the supplied board with the requested solver and optionally returns solving steps.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Board to solve. Use 0 for empty cells.",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SolvePuzzleRequest.class),
                    examples = @ExampleObject(name = "MRV with steps", value = OpenApiExamples.SOLVE_REQUEST)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Solver run completed. If the puzzle is unsolvable, solved is false.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SolvePuzzleResponse.class),
                            examples = @ExampleObject(name = "Solved puzzle", value = OpenApiExamples.SOLVE_RESPONSE))),
            @ApiResponse(
                    responseCode = "400",
                    description = "The board shape, cell values, or solver parameter is invalid.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(name = "Validation error", value = OpenApiExamples.ERROR_400))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(name = "Unexpected error", value = OpenApiExamples.ERROR_500)))
    })
    public ResponseEntity<SolvePuzzleResponse> solve(@Valid @RequestBody SolvePuzzleRequest request) {
        SolveResult result = switch (request.requestedSolver()) {
            case BACKTRACKING -> backtrackingSolver.solve(request.toBoard(), request.shouldIncludeSteps());
            case MRV -> mrvSolver.solve(request.toBoard(), request.shouldIncludeSteps());
        };
        return ResponseEntity.ok(SolvePuzzleResponse.fromDomain(result));
    }

    @PostMapping("/validate")
    @Operation(
            summary = "Validate a Sudoku board",
            description = "Returns every row, column, and box rule violation found on the supplied board.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Board to validate. Use 0 for empty cells.",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ValidatePuzzleRequest.class),
                    examples = @ExampleObject(name = "Board with a duplicate", value = OpenApiExamples.VALIDATE_REQUEST)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Validation completed.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidatePuzzleResponse.class),
                            examples = @ExampleObject(name = "Violation response", value = OpenApiExamples.VALIDATE_RESPONSE))),
            @ApiResponse(
                    responseCode = "400",
                    description = "The board shape or cell values are invalid.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(name = "Validation error", value = OpenApiExamples.ERROR_400))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(name = "Unexpected error", value = OpenApiExamples.ERROR_500)))
    })
    public ResponseEntity<ValidatePuzzleResponse> validate(@Valid @RequestBody ValidatePuzzleRequest request) {
        return ResponseEntity.ok(ValidatePuzzleResponse.fromDomain(validator.validate(request.toBoard())));
    }

    @PostMapping("/hint")
    @Operation(
            summary = "Get a safe next move",
            description = "Returns a cell, value, and explanation for a valid incomplete board.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Current board state. Use 0 for empty cells.",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = HintRequest.class),
                    examples = @ExampleObject(name = "Incomplete board", value = OpenApiExamples.HINT_REQUEST)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Hint found.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = HintResponse.class),
                            examples = @ExampleObject(name = "Hint", value = OpenApiExamples.HINT_RESPONSE))),
            @ApiResponse(responseCode = "204", description = "The board is already complete.", content = @Content),
            @ApiResponse(
                    responseCode = "400",
                    description = "The board is malformed or violates Sudoku rules.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(name = "Invalid board", value = OpenApiExamples.ERROR_400))),
            @ApiResponse(
                    responseCode = "422",
                    description = "The board is valid but no safe hint is available.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(name = "No hint", value = OpenApiExamples.ERROR_422))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(name = "Unexpected error", value = OpenApiExamples.ERROR_500)))
    })
    public ResponseEntity<?> hint(@Valid @RequestBody HintRequest request, HttpServletRequest httpRequest) {
        SudokuBoard board = request.toBoard();
        if (!validator.isValid(board)) {
            throw new InvalidBoardException("Board is invalid; fix rule violations before requesting a hint.");
        }
        if (isCompleted(board)) {
            return ResponseEntity.noContent().build();
        }

        return hintService.findHint(board)
                .<ResponseEntity<?>>map(hint -> ResponseEntity.ok(HintResponse.fromDomain(hint)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ApiErrorResponse.of(
                        HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
                        "No hint is available for this board.",
                        httpRequest.getRequestURI())));
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
}

package com.sudokuengine.controller;

public final class OpenApiExamples {

    public static final String GENERATE_REQUEST = """
            {
              "difficulty": "MEDIUM"
            }
            """;

    public static final String GENERATE_RESPONSE = """
            {
              "puzzle": [
                [5,3,0,0,7,0,0,0,0],
                [6,0,0,1,9,5,0,0,0],
                [0,9,8,0,0,0,0,6,0],
                [8,0,0,0,6,0,0,0,3],
                [4,0,0,8,0,3,0,0,1],
                [7,0,0,0,2,0,0,0,6],
                [0,6,0,0,0,0,2,8,0],
                [0,0,0,4,1,9,0,0,5],
                [0,0,0,0,8,0,0,7,9]
              ],
              "difficulty": "MEDIUM"
            }
            """;

    public static final String SOLVE_REQUEST = """
            {
              "board": [
                [5,3,0,0,7,0,0,0,0],
                [6,0,0,1,9,5,0,0,0],
                [0,9,8,0,0,0,0,6,0],
                [8,0,0,0,6,0,0,0,3],
                [4,0,0,8,0,3,0,0,1],
                [7,0,0,0,2,0,0,0,6],
                [0,6,0,0,0,0,2,8,0],
                [0,0,0,4,1,9,0,0,5],
                [0,0,0,0,8,0,0,7,9]
              ],
              "includeSteps": true,
              "solver": "MRV"
            }
            """;

    public static final String SOLVE_RESPONSE = """
            {
              "solved": true,
              "board": [
                [5,3,4,6,7,8,9,1,2],
                [6,7,2,1,9,5,3,4,8],
                [1,9,8,3,4,2,5,6,7],
                [8,5,9,7,6,1,4,2,3],
                [4,2,6,8,5,3,7,9,1],
                [7,1,3,9,2,4,8,5,6],
                [9,6,1,5,3,7,2,8,4],
                [2,8,7,4,1,9,6,3,5],
                [3,4,5,2,8,6,1,7,9]
              ],
              "metrics": {
                "visitedNodes": 51,
                "backtracks": 0,
                "maxRecursionDepth": 51,
                "elapsedNanos": 1200000
              },
              "steps": [
                {
                  "type": "PLACE_VALUE",
                  "row": 0,
                  "col": 2,
                  "value": 4,
                  "sequence": 1,
                  "depth": 1
                }
              ]
            }
            """;

    public static final String VALIDATE_REQUEST = """
            {
              "board": [
                [5,5,0,0,7,0,0,0,0],
                [6,0,0,1,9,5,0,0,0],
                [0,9,8,0,0,0,0,6,0],
                [8,0,0,0,6,0,0,0,3],
                [4,0,0,8,0,3,0,0,1],
                [7,0,0,0,2,0,0,0,6],
                [0,6,0,0,0,0,2,8,0],
                [0,0,0,4,1,9,0,0,5],
                [0,0,0,0,8,0,0,7,9]
              ]
            }
            """;

    public static final String VALIDATE_RESPONSE = """
            {
              "valid": false,
              "violations": [
                {
                  "type": "ROW_DUPLICATE",
                  "row": 0,
                  "col": 1,
                  "value": 5,
                  "message": "Value 5 appears more than once in row 0."
                }
              ]
            }
            """;

    public static final String HINT_REQUEST = """
            {
              "board": [
                [5,3,0,0,7,0,0,0,0],
                [6,0,0,1,9,5,0,0,0],
                [0,9,8,0,0,0,0,6,0],
                [8,0,0,0,6,0,0,0,3],
                [4,0,0,8,0,3,0,0,1],
                [7,0,0,0,2,0,0,0,6],
                [0,6,0,0,0,0,2,8,0],
                [0,0,0,4,1,9,0,0,5],
                [0,0,0,0,8,0,0,7,9]
              ]
            }
            """;

    public static final String HINT_RESPONSE = """
            {
              "row": 0,
              "col": 2,
              "value": 4,
              "reason": "Cell (0,2) has only one valid candidate."
            }
            """;

    public static final String ERROR_400 = """
            {
              "timestamp": "2026-07-17T10:15:30Z",
              "status": 400,
              "error": "Bad Request",
              "message": "Request validation failed.",
              "path": "/api/v1/puzzles/generate",
              "validationErrors": {
                "difficulty": "must not be null"
              }
            }
            """;

    public static final String ERROR_422 = """
            {
              "timestamp": "2026-07-17T10:15:30Z",
              "status": 422,
              "error": "Unprocessable Entity",
              "message": "No hint is available for this board.",
              "path": "/api/v1/puzzles/hint",
              "validationErrors": {}
            }
            """;

    public static final String ERROR_500 = """
            {
              "timestamp": "2026-07-17T10:15:30Z",
              "status": 500,
              "error": "Internal Server Error",
              "message": "An unexpected error occurred.",
              "path": "/api/v1/puzzles/solve",
              "validationErrors": {}
            }
            """;

    private OpenApiExamples() {
    }
}

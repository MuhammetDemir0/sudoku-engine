# Puzzle Difficulty

Puzzles are classified as `EASY`, `MEDIUM`, `HARD`, or `EXPERT` by
`DifficultyAnalysisService`.

## Calculation

`DifficultyAnalysisService.calculate(SudokuBoard puzzle)` evaluates two
signals:

- clue count, where more givens usually means an easier puzzle
- solver effort from `SolveResult.Metrics`, including visited nodes,
  backtracks, and maximum recursion depth

Each signal is mapped to a difficulty band using
`DifficultyThresholdConfig`. The final result is the harder of the clue-count
classification and the solver-metric classification.

## Thresholds

All thresholds live in `DifficultyThresholdConfig`; generation and analysis
must read them from there rather than duplicating numeric cutoffs elsewhere.

| Difficulty | Minimum clues | Generator target max clues | Max visited nodes | Max backtracks | Max recursion depth |
| --- | ---: | ---: | ---: | ---: | ---: |
| EASY | 36 | 45 | 80 | 0 | 45 |
| MEDIUM | 32 | 35 | 200 | 30 | 49 |
| HARD | 28 | 31 | 1,000 | 300 | 53 |
| EXPERT | 0 | 27 | unlimited | unlimited | 81 |

## Generation

`UniqueSudokuPuzzleGenerator.generate(Difficulty difficulty)` accepts the
requested difficulty. It removes values from a completed solution while keeping
the puzzle uniquely solvable, then returns only when the clue count is inside
the configured generation window and `DifficultyAnalysisService` calculates the
same requested difficulty.

The existing `generate()` method remains available and delegates to
`generate(Difficulty.MEDIUM)`.

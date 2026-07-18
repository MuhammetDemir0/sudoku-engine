# Solver Benchmark Report

This report compares the plain backtracking solver and the MRV solver with real
measurements from the standalone benchmark runner. The benchmark is separate
from the unit test suite and is not executed by `mvn test` or `mvn verify`.

## Reproduce

Run from the repository root:

```powershell
mvn -B -pl backend -Pbenchmark clean test-compile exec:java
```

The benchmark runner is
`backend/src/test/java/com/sudokuengine/benchmark/SolverBenchmark.java`.

## Method

- Test set: two fixed puzzles for each difficulty: `EASY`, `MEDIUM`, `HARD`,
  and `EXPERT`.
- Solvers: `BacktrackingSudokuSolver` and `MrvSudokuSolver`.
- Warm-up: 2 runs per solver and puzzle before measurement.
- Measurement: 5 runs per solver and puzzle.
- Time reported: median solver-reported elapsed time from the measured runs.
- Other metrics: visited nodes, backtracks, and maximum recursion depth from the
  same median run.

## Test Machine

Measured on `2026-07-18T12:14:01.768269700Z`.

- OS: `Windows 11 10.0 amd64`
- Available processors: `12`
- JVM: `Java HotSpot(TM) 64-Bit Server VM 21.0.10+8-LTS-217`
- Max JVM heap: `4032 MB`

## Results

| Difficulty | Puzzle | Solver | Median time (ms) | Visited nodes | Backtracks | Max depth |
| --- | --- | --- | ---: | ---: | ---: | ---: |
| EASY | easy-1 | BACKTRACKING | 0.009 | 2 | 0 | 1 |
| EASY | easy-1 | MRV | 0.004 | 1 | 0 | 1 |
| EASY | easy-2 | BACKTRACKING | 0.003 | 5 | 0 | 1 |
| EASY | easy-2 | MRV | 0.004 | 1 | 0 | 1 |
| MEDIUM | medium-1 | BACKTRACKING | 4.005 | 37652 | 4157 | 51 |
| MEDIUM | medium-1 | MRV | 0.584 | 51 | 0 | 51 |
| MEDIUM | medium-2 | BACKTRACKING | 2.743 | 20010 | 2193 | 57 |
| MEDIUM | medium-2 | MRV | 1.595 | 1293 | 1236 | 57 |
| HARD | hard-1 | BACKTRACKING | 1.332 | 170978 | 18969 | 53 |
| HARD | hard-1 | MRV | 0.231 | 130 | 77 | 53 |
| HARD | hard-2 | BACKTRACKING | 0.971 | 89833 | 9949 | 58 |
| HARD | hard-2 | MRV | 0.550 | 653 | 595 | 58 |
| EXPERT | expert-1 | BACKTRACKING | 0.652 | 80491 | 8911 | 58 |
| EXPERT | expert-1 | MRV | 0.193 | 219 | 161 | 58 |
| EXPERT | expert-2 | BACKTRACKING | 3.005 | 445778 | 49498 | 60 |
| EXPERT | expert-2 | MRV | 18.126 | 13810 | 13750 | 60 |

## Average By Difficulty

| Difficulty | Solver | Average median time (ms) | Average visited nodes | Average backtracks | Average max depth |
| --- | --- | ---: | ---: | ---: | ---: |
| EASY | BACKTRACKING | 0.006 | 3.5 | 0.0 | 1.0 |
| EASY | MRV | 0.004 | 1.0 | 0.0 | 1.0 |
| MEDIUM | BACKTRACKING | 3.374 | 28831.0 | 3175.0 | 54.0 |
| MEDIUM | MRV | 1.089 | 672.0 | 618.0 | 54.0 |
| HARD | BACKTRACKING | 1.151 | 130405.5 | 14459.0 | 55.5 |
| HARD | MRV | 0.390 | 391.5 | 336.0 | 55.5 |
| EXPERT | BACKTRACKING | 1.828 | 263134.5 | 29204.5 | 59.0 |
| EXPERT | MRV | 9.160 | 7014.5 | 6955.5 | 59.0 |

## Interpretation

MRV usually explores far fewer nodes because it chooses the most constrained
empty cell first. That advantage is visible in every measured non-trivial puzzle
by node count and backtrack count.

Elapsed time is not purely proportional to node count. The MRV solver recomputes
candidates while searching, so puzzle `expert-2` shows a useful counterexample:
MRV visits far fewer nodes but takes longer in wall-clock solver time. The result
suggests MRV is the better default for search effort and visualization, while a
future optimization pass could cache row, column, and box candidates to reduce
MRV overhead.

package com.sudokuengine.benchmark;

import com.sudokuengine.model.Difficulty;
import com.sudokuengine.model.SolveResult;
import com.sudokuengine.model.SudokuBoard;
import com.sudokuengine.service.BacktrackingSudokuSolver;
import com.sudokuengine.service.MrvSudokuSolver;
import com.sudokuengine.service.SudokuSolver;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Standalone benchmark runner for comparing solver algorithms.
 *
 * <p>
 * This class is intentionally not a unit test. It lives in test sources so it is
 * excluded from the production JAR and run explicitly with the Maven benchmark
 * profile documented in docs/benchmark.md.
 * </p>
 */
public final class SolverBenchmark {

    private static final int WARMUP_RUNS = 2;
    private static final int MEASURED_RUNS = 5;

    private SolverBenchmark() {
    }

    public static void main(String[] args) {
        List<PuzzleCase> cases = puzzleCases();
        List<SolverCase> solvers = List.of(
                new SolverCase("BACKTRACKING", new BacktrackingSudokuSolver()),
                new SolverCase("MRV", new MrvSudokuSolver()));

        System.out.println("# Solver Benchmark Results");
        System.out.println();
        System.out.println("Generated at: `" + Instant.now() + "`");
        System.out.println();
        printMachineSpecs();
        System.out.println();
        System.out.println("Warm-up runs per solver/puzzle: `" + WARMUP_RUNS + "`");
        System.out.println("Measured runs per solver/puzzle: `" + MEASURED_RUNS + "`");
        System.out.println();
        System.out.println("| Difficulty | Puzzle | Solver | Median time (ms) | Visited nodes | Backtracks | Max depth |");
        System.out.println("| --- | --- | --- | ---: | ---: | ---: | ---: |");

        List<BenchmarkRow> rows = new ArrayList<>();
        for (PuzzleCase puzzle : cases) {
            for (SolverCase solver : solvers) {
                warmUp(puzzle, solver);
                BenchmarkRow row = measure(puzzle, solver);
                rows.add(row);
                printRow(row);
            }
        }

        System.out.println();
        System.out.println("## Average By Difficulty");
        System.out.println();
        System.out.println("| Difficulty | Solver | Average median time (ms) | Average visited nodes | Average backtracks | Average max depth |");
        System.out.println("| --- | --- | ---: | ---: | ---: | ---: |");
        for (Difficulty difficulty : Difficulty.values()) {
            for (SolverCase solver : solvers) {
                List<BenchmarkRow> filtered = rows.stream()
                        .filter(row -> row.difficulty() == difficulty && row.solver().equals(solver.name()))
                        .toList();
                printAverageRow(difficulty, solver.name(), filtered);
            }
        }
    }

    private static void printMachineSpecs() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        Runtime runtime = Runtime.getRuntime();
        long maxMemoryMb = runtime.maxMemory() / (1024 * 1024);
        System.out.println("## Machine");
        System.out.println();
        System.out.println("- OS: `" + os.getName() + " " + os.getVersion() + " " + os.getArch() + "`");
        System.out.println("- Available processors: `" + os.getAvailableProcessors() + "`");
        System.out.println("- JVM: `" + System.getProperty("java.vm.name") + " "
                + System.getProperty("java.runtime.version") + "`");
        System.out.println("- Max JVM heap: `" + maxMemoryMb + " MB`");
    }

    private static void warmUp(PuzzleCase puzzle, SolverCase solver) {
        for (int index = 0; index < WARMUP_RUNS; index++) {
            SolveResult result = solver.solver().solve(new SudokuBoard(puzzle.board()));
            ensureSolved(puzzle, solver, result);
        }
    }

    private static BenchmarkRow measure(PuzzleCase puzzle, SolverCase solver) {
        List<SolveResult> results = new ArrayList<>();
        for (int index = 0; index < MEASURED_RUNS; index++) {
            SolveResult result = solver.solver().solve(new SudokuBoard(puzzle.board()));
            ensureSolved(puzzle, solver, result);
            results.add(result);
        }

        SolveResult median = results.stream()
                .sorted(Comparator.comparingLong(result -> result.getMetrics().getElapsedNanos()))
                .toList()
                .get(MEASURED_RUNS / 2);
        SolveResult.Metrics metrics = median.getMetrics();

        return new BenchmarkRow(
                puzzle.difficulty(),
                puzzle.name(),
                solver.name(),
                metrics.getElapsedNanos() / 1_000_000.0,
                metrics.getVisitedNodes(),
                metrics.getBacktracks(),
                metrics.getMaxRecursionDepth());
    }

    private static void ensureSolved(PuzzleCase puzzle, SolverCase solver, SolveResult result) {
        if (!result.isSolved()) {
            throw new IllegalStateException("Solver " + solver.name()
                    + " did not solve benchmark puzzle " + puzzle.name() + ".");
        }
    }

    private static void printRow(BenchmarkRow row) {
        System.out.printf(Locale.ROOT, "| %s | %s | %s | %.3f | %d | %d | %d |%n",
                row.difficulty(),
                row.puzzle(),
                row.solver(),
                row.medianMillis(),
                row.visitedNodes(),
                row.backtracks(),
                row.maxDepth());
    }

    private static void printAverageRow(Difficulty difficulty, String solver, List<BenchmarkRow> rows) {
        double count = rows.size();
        double averageMillis = rows.stream().mapToDouble(BenchmarkRow::medianMillis).sum() / count;
        double averageNodes = rows.stream().mapToInt(BenchmarkRow::visitedNodes).sum() / count;
        double averageBacktracks = rows.stream().mapToInt(BenchmarkRow::backtracks).sum() / count;
        double averageDepth = rows.stream().mapToInt(BenchmarkRow::maxDepth).sum() / count;

        System.out.printf(Locale.ROOT, "| %s | %s | %.3f | %.1f | %.1f | %.1f |%n",
                difficulty,
                solver,
                averageMillis,
                averageNodes,
                averageBacktracks,
                averageDepth);
    }

    private static List<PuzzleCase> puzzleCases() {
        return List.of(
                new PuzzleCase(Difficulty.EASY, "easy-1", new int[][] {
                        { 5, 3, 4, 6, 7, 8, 9, 1, 0 },
                        { 6, 7, 2, 1, 9, 5, 3, 4, 8 },
                        { 1, 9, 8, 3, 4, 2, 5, 6, 7 },
                        { 8, 5, 9, 7, 6, 1, 4, 2, 3 },
                        { 4, 2, 6, 8, 5, 3, 7, 9, 1 },
                        { 7, 1, 3, 9, 2, 4, 8, 5, 6 },
                        { 9, 6, 1, 5, 3, 7, 2, 8, 4 },
                        { 2, 8, 7, 4, 1, 9, 6, 3, 5 },
                        { 3, 4, 5, 2, 8, 6, 1, 7, 9 }
                }),
                new PuzzleCase(Difficulty.EASY, "easy-2", new int[][] {
                        { 0, 3, 4, 6, 7, 8, 9, 1, 2 },
                        { 6, 7, 2, 1, 9, 5, 3, 4, 8 },
                        { 1, 9, 8, 3, 4, 2, 5, 6, 7 },
                        { 8, 5, 9, 7, 6, 1, 4, 2, 3 },
                        { 4, 2, 6, 8, 5, 3, 7, 9, 1 },
                        { 7, 1, 3, 9, 2, 4, 8, 5, 6 },
                        { 9, 6, 1, 5, 3, 7, 2, 8, 4 },
                        { 2, 8, 7, 4, 1, 9, 6, 3, 5 },
                        { 3, 4, 5, 2, 8, 6, 1, 7, 9 }
                }),
                new PuzzleCase(Difficulty.MEDIUM, "medium-1", new int[][] {
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
                new PuzzleCase(Difficulty.MEDIUM, "medium-2", new int[][] {
                        { 0, 2, 0, 6, 0, 8, 0, 0, 0 },
                        { 5, 8, 0, 0, 0, 9, 7, 0, 0 },
                        { 0, 0, 0, 0, 4, 0, 0, 0, 0 },
                        { 3, 7, 0, 0, 0, 0, 5, 0, 0 },
                        { 6, 0, 0, 0, 0, 0, 0, 0, 4 },
                        { 0, 0, 8, 0, 0, 0, 0, 1, 3 },
                        { 0, 0, 0, 0, 2, 0, 0, 0, 0 },
                        { 0, 0, 9, 8, 0, 0, 0, 3, 6 },
                        { 0, 0, 0, 3, 0, 6, 0, 9, 0 }
                }),
                new PuzzleCase(Difficulty.HARD, "hard-1", new int[][] {
                        { 0, 0, 0, 0, 0, 0, 9, 0, 7 },
                        { 0, 0, 0, 4, 2, 0, 1, 8, 0 },
                        { 0, 0, 0, 7, 0, 5, 0, 2, 6 },
                        { 1, 0, 0, 9, 0, 4, 0, 0, 0 },
                        { 0, 5, 0, 0, 0, 0, 0, 4, 0 },
                        { 0, 0, 0, 5, 0, 7, 0, 0, 9 },
                        { 9, 2, 0, 1, 0, 8, 0, 0, 0 },
                        { 0, 3, 4, 0, 5, 9, 0, 0, 0 },
                        { 5, 0, 7, 0, 0, 0, 0, 0, 0 }
                }),
                new PuzzleCase(Difficulty.HARD, "hard-2", new int[][] {
                        { 0, 0, 5, 3, 0, 0, 0, 0, 0 },
                        { 8, 0, 0, 0, 0, 0, 0, 2, 0 },
                        { 0, 7, 0, 0, 1, 0, 5, 0, 0 },
                        { 4, 0, 0, 0, 0, 5, 3, 0, 0 },
                        { 0, 1, 0, 0, 7, 0, 0, 0, 6 },
                        { 0, 0, 3, 2, 0, 0, 0, 8, 0 },
                        { 0, 6, 0, 5, 0, 0, 0, 0, 9 },
                        { 0, 0, 4, 0, 0, 0, 0, 3, 0 },
                        { 0, 0, 0, 0, 0, 9, 7, 0, 0 }
                }),
                new PuzzleCase(Difficulty.EXPERT, "expert-1", new int[][] {
                        { 1, 0, 0, 0, 0, 7, 0, 9, 0 },
                        { 0, 3, 0, 0, 2, 0, 0, 0, 8 },
                        { 0, 0, 9, 6, 0, 0, 5, 0, 0 },
                        { 0, 0, 5, 3, 0, 0, 9, 0, 0 },
                        { 0, 1, 0, 0, 8, 0, 0, 0, 2 },
                        { 6, 0, 0, 0, 0, 4, 0, 0, 0 },
                        { 3, 0, 0, 0, 0, 0, 0, 1, 0 },
                        { 0, 4, 0, 0, 0, 0, 0, 0, 7 },
                        { 0, 0, 7, 0, 0, 0, 3, 0, 0 }
                }),
                new PuzzleCase(Difficulty.EXPERT, "expert-2", new int[][] {
                        { 8, 0, 0, 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 3, 6, 0, 0, 0, 0, 0 },
                        { 0, 7, 0, 0, 9, 0, 2, 0, 0 },
                        { 0, 5, 0, 0, 0, 7, 0, 0, 0 },
                        { 0, 0, 0, 0, 4, 5, 7, 0, 0 },
                        { 0, 0, 0, 1, 0, 0, 0, 3, 0 },
                        { 0, 0, 1, 0, 0, 0, 0, 6, 8 },
                        { 0, 0, 8, 5, 0, 0, 0, 1, 0 },
                        { 0, 9, 0, 0, 0, 0, 4, 0, 0 }
                }));
    }

    private record PuzzleCase(Difficulty difficulty, String name, int[][] board) {
    }

    private record SolverCase(String name, SudokuSolver solver) {
    }

    private record BenchmarkRow(
            Difficulty difficulty,
            String puzzle,
            String solver,
            double medianMillis,
            int visitedNodes,
            int backtracks,
            int maxDepth) {
    }
}

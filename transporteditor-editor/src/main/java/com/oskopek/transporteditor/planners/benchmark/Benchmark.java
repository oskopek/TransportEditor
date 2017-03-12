package com.oskopek.transporteditor.planners.benchmark;

import com.google.common.collect.ArrayTable;
import com.oskopek.transporteditor.model.planner.Planner;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.planners.benchmark.data.BenchmarkMatrix;
import com.oskopek.transporteditor.planners.benchmark.data.BenchmarkResults;
import com.oskopek.transporteditor.planners.benchmark.data.BenchmarkRun;
import javaslang.*;
import javaslang.collection.Stream;
import javaslang.control.Try;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Benchmark {

    private final BenchmarkMatrix matrix;
    private final ScoreFunction scoreFunction;
    private final Function2<Problem, Planner, Boolean> skipFunction;

    public Benchmark(BenchmarkMatrix matrix, ScoreFunction scoreFunction,
            Function2<Problem, Planner, Boolean> skipFunction) {
        this.matrix = matrix;
        this.scoreFunction = scoreFunction;
        this.skipFunction = skipFunction;
    }

    public BenchmarkResults benchmark(int threadCount) {
        ExecutorService service = Executors.newFixedThreadPool(threadCount);
        BenchmarkResults results = null;
        try {
             results = Try.of(() -> schedule(matrix, service))
                    .flatMap(futures -> Try.of(() -> waitFor(futures)))
                    .map(intermediates -> Benchmark.populateRunTable(matrix, intermediates))
                    .map(table -> BenchmarkResults.from(table))
                    .getOrElseThrow(t -> new IllegalStateException("Benchmark failed.", t));
        } finally {
            service.shutdown();
        }
        return results;
    }

    private List<CompletableFuture<BenchmarkRun>> schedule(BenchmarkMatrix matrix,
            ExecutorService executor) {
        return matrix.toBenchmarkRuns(skipFunction, scoreFunction)
                .map(benchmarkRun -> CompletableFuture.supplyAsync(() -> benchmarkRun.run(), executor))
                .toJavaList();
    }

    private static Stream<BenchmarkRun> waitFor(
            List<CompletableFuture<BenchmarkRun>> futures)
            throws ExecutionException, InterruptedException {
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        allOf.get();
        return Stream.ofAll(futures).map(f -> Try.of(() -> f.get())
                .getOrElseThrow(e -> new IllegalStateException("Future not completed.", e)));
    }

    private static ArrayTable<Problem, Planner, BenchmarkRun> populateRunTable(
            BenchmarkMatrix matrix, Stream<BenchmarkRun> intermediates) {
            ArrayTable<Problem, Planner, BenchmarkRun> table = ArrayTable.create(matrix.getProblems(),
                    matrix.getPlanners());
            intermediates.forEach(intermediate -> {
                table.put(intermediate.getProblem(), intermediate.getPlanner(), intermediate);
            });
            return table;
    }
}

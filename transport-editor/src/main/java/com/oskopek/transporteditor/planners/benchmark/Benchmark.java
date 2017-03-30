package com.oskopek.transporteditor.planners.benchmark;

import com.google.common.collect.ArrayTable;
import com.oskopek.transporteditor.model.planner.Planner;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.planners.benchmark.data.BenchmarkMatrix;
import com.oskopek.transporteditor.planners.benchmark.data.BenchmarkResults;
import com.oskopek.transporteditor.planners.benchmark.data.BenchmarkRun;
import com.oskopek.transporteditor.validation.Validator;
import javaslang.Function2;
import javaslang.collection.Stream;
import javaslang.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

/**
 * Facilitates the benchmarking process on top of the data.
 */
public class Benchmark {

    private final BenchmarkMatrix matrix;
    private final ScoreFunction scoreFunction;
    private final Function2<Problem, Planner, Boolean> skipFunction;
    private final Validator validator;

    private static final Logger logger = LoggerFactory.getLogger(Benchmark.class);

    /**
     * Default constructor.
     *
     * @param matrix the benchmarking matrix to execute
     * @param scoreFunction the score function to use
     * @param skipFunction the skipping function to use
     * @param validator the validator to use
     */
    public Benchmark(BenchmarkMatrix matrix, ScoreFunction scoreFunction,
            Function2<Problem, Planner, Boolean> skipFunction, Validator validator) {
        this.matrix = matrix;
        this.scoreFunction = scoreFunction;
        this.skipFunction = skipFunction;
        this.validator = validator;
    }

    /**
     * Waits for all the futures and extracts their results.
     *
     * @param futures the futures to wait for
     * @return a stream of benchmark runs as results from the futures
     */
    private static Stream<BenchmarkRun> waitFor(List<CompletableFuture<BenchmarkRun>> futures) {
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        Try.run(allOf::get).onFailure(e -> new IllegalStateException("Waiting for all futures failed.", e));
        logger.info("All benchmarks finished, writing results...");
        return Stream.ofAll(futures).map(
                f -> Try.of(f::get).getOrElseThrow(e -> new IllegalStateException("Future not completed.", e)));
    }

    /**
     * Populates the run table from the stream of benchmark run results.
     *
     * @param matrix the benchmark matrix
     * @param intermediates the intermediate results
     * @return the run table
     */
    private static ArrayTable<Problem, Planner, BenchmarkRun> populateRunTable(BenchmarkMatrix matrix,
            Stream<BenchmarkRun> intermediates) {
        ArrayTable<Problem, Planner, BenchmarkRun> table = ArrayTable.create(matrix.getProblems(),
                matrix.getPlanners());
        intermediates.forEach(intermediate -> table.put(intermediate.getProblem(), intermediate.getPlanner(),
                intermediate));
        return table;
    }

    /**
     * Run the benchmark in a thread pool with the given size.
     *
     * @param threadCount the thread pool size
     * @return the results
     */
    public BenchmarkResults benchmark(Integer threadCount) {
        if (threadCount == null) {
            threadCount = Runtime.getRuntime().availableProcessors();
            logger.warn("Thread count not set in config, using default: {}", threadCount);
        }

        logger.info("Starting all benchmarks...");
        ScheduledExecutorService schedule = Executors.newScheduledThreadPool(threadCount);
        ExecutorService service = Executors.newFixedThreadPool(threadCount);
        BenchmarkResults results;
        try {
            results = Try.of(() -> schedule(matrix, service, schedule))
                    .flatMap(futures -> Try.of(() -> waitFor(futures)))
                    .map(intermediates -> Benchmark.populateRunTable(matrix, intermediates)).map(BenchmarkResults::from)
                    .getOrElseThrow(t -> new IllegalStateException("Benchmark failed.", t));
        } finally {
            service.shutdown();
            schedule.shutdown();
        }
        return results;
    }

    /**
     * Schedule all the {@link BenchmarkRun}s into the executor. Doesn't block.
     *
     * @param matrix the benchmark matrix
     * @param service the executor service
     * @param schedule the timeout-keeping executor service
     * @return a list of futures of the scheduled runs
     */
    private List<CompletableFuture<BenchmarkRun>> schedule(BenchmarkMatrix matrix, ExecutorService service,
            ScheduledExecutorService schedule) {
        return matrix.toBenchmarkRuns(skipFunction, scoreFunction)
                .map(benchmarkRun -> {
                    CompletableFuture<BenchmarkRun> runFuture = new CompletableFuture<>();
                    CompletableFuture<BenchmarkRun> planningFuture = new CompletableFuture<>();
                    Callable<CompletableFuture<BenchmarkRun>> timeoutHandler = () -> {
                        logger.debug("Schedule timeout for {}, {}", benchmarkRun.getPlanner(),
                                benchmarkRun.getProblem());
                        if (!planningFuture.isDone()) {
                            BenchmarkRun defaultRun = new BenchmarkRun(benchmarkRun, new BenchmarkRun.Results(null,
                                    null, benchmarkRun.getBestScore(), BenchmarkRun.RunExitStatus.UNSOLVED,
                                    0, 0));
                            logger.trace("Not done {}, {}", benchmarkRun.getPlanner(), benchmarkRun.getProblem());
                            boolean result = benchmarkRun.getPlanner().cancel();
                            logger.trace("Cancel result {} for {}, {}", result, benchmarkRun.getPlanner(),
                                    benchmarkRun.getProblem());
                            if (!result) {
                                runFuture.complete(defaultRun);
                            } else {
                                logger.trace("Getting cancelled planner result {}, {}", benchmarkRun.getPlanner(),
                                        benchmarkRun.getProblem());
                                BenchmarkRun planned = Try.of(() -> planningFuture.get(5, TimeUnit.SECONDS))
                                        .getOrElse(defaultRun);
                                runFuture.complete(planned);
                            }
                        } else {
                            logger.trace("Done {}, {}", benchmarkRun.getPlanner(), benchmarkRun.getProblem());
                            runFuture.complete(Try.of(planningFuture::get).getOrElseThrow(e ->
                                    new IllegalStateException("Getting finished future failed.", e)));
                        }
                        return runFuture;
                    };

                    CompletableFuture.runAsync(() -> {
                                ScheduledFuture<CompletableFuture<BenchmarkRun>> future = schedule.schedule(
                                        timeoutHandler, benchmarkRun.getTimeout(), TimeUnit.SECONDS);
                                BenchmarkRun results = benchmarkRun.execute(validator);
                                future.cancel(false);
                                planningFuture.complete(results);
                                if (future.isCancelled()) {
                                    Try.run(timeoutHandler::call);
                                }
                            }, service);
                    return runFuture;
                })
                .toJavaList();
    }
}

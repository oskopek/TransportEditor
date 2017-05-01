package com.oskopek.transport.benchmark;

import com.oskopek.transport.model.planner.Planner;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.validation.Validator;
import com.oskopek.transport.benchmark.data.BenchmarkMatrix;
import com.oskopek.transport.benchmark.data.BenchmarkResults;
import com.oskopek.transport.benchmark.data.BenchmarkRun;
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
    private static Stream<BenchmarkResults.JsonRun> waitFor(List<CompletableFuture<BenchmarkResults.JsonRun>> futures) {
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        Try.run(allOf::get).onFailure(e -> new IllegalStateException("Waiting for all futures failed.", e));
        logger.info("All benchmarks finished, writing results...");
        return Stream.ofAll(futures).map(f -> Try.of(f::get)
                .getOrElseThrow(e -> new IllegalStateException("Future not completed.", e)));
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
        List<BenchmarkResults.JsonRun> results;
        try {
            results = Try.of(() -> schedule(matrix, service, schedule))
                    .flatMap(futures -> Try.of(() -> waitFor(futures)))
                    .getOrElseThrow(t -> new IllegalStateException("Benchmark failed.", t)).toJavaList();
        } finally {
            service.shutdown();
            schedule.shutdown();
        }
        return BenchmarkResults.from(results);
    }

    /**
     * Schedule all the {@link BenchmarkRun}s into the executor. Doesn't block.
     *
     * @param matrix the benchmark matrix
     * @param service the executor service
     * @param schedule the timeout-keeping executor service
     * @return a list of futures of the scheduled runs
     */
    private List<CompletableFuture<BenchmarkResults.JsonRun>> schedule(BenchmarkMatrix matrix, ExecutorService service,
            ScheduledExecutorService schedule) {
        return matrix.toBenchmarkRuns(skipFunction, scoreFunction)
                .map(benchmarkRun -> {
                    CompletableFuture<BenchmarkRun> runFuture = new CompletableFuture<>();
                    CompletableFuture<BenchmarkRun> planningFuture = new CompletableFuture<>();
                    Callable<CompletableFuture<BenchmarkRun>> timeoutHandler = () -> {
                        logger.debug("Schedule timeout for {}, {}", benchmarkRun.getPlanner().getName(),
                                benchmarkRun.getProblem().getName());
                        if (!planningFuture.isDone()) {
                            BenchmarkRun defaultRun = new BenchmarkRun(benchmarkRun, new BenchmarkRun.Results(null,
                                    null, benchmarkRun.getBestScore(), BenchmarkRun.RunExitStatus.FAILED_TO_OBTAIN_PLAN,
                                    0, 0));
                            logger.debug("Not done {}, {}", benchmarkRun.getPlanner().getName(),
                                    benchmarkRun.getProblem().getName());
                            boolean result = benchmarkRun.getPlanner().cancel();
                            logger.debug("Cancel result {} for {}, {}", result, benchmarkRun.getPlanner().getName(),
                                    benchmarkRun.getProblem().getName());
                            if (!result) {
                                runFuture.complete(defaultRun);
                            } else {
                                logger.debug("Getting cancelled planner result {}, {}",
                                        benchmarkRun.getPlanner().getName(), benchmarkRun.getProblem().getName());
                                BenchmarkRun planned = Try.of(() -> planningFuture.get(10, TimeUnit.SECONDS))
                                        .onFailure(e -> logger.warn("Waiting for planning future failed.", e))
                                        .getOrElse(defaultRun);
                                runFuture.complete(planned);
                            }
                        } else {
                            logger.debug("Done {}, {}", benchmarkRun.getPlanner().getName(),
                                    benchmarkRun.getProblem().getName());
                            runFuture.complete(Try.of(planningFuture::get).getOrElseThrow(e ->
                                    new IllegalStateException("Getting finished future failed.", e)));
                        }
                        logger.debug("Completing timeout handler {}, {}.", benchmarkRun.getPlanner().getName(),
                                benchmarkRun.getProblem().getName());
                        return runFuture;
                    };

                    CompletableFuture.runAsync(() -> {
                                ScheduledFuture<CompletableFuture<BenchmarkRun>> future = schedule.schedule(
                                        timeoutHandler, benchmarkRun.getTimeout(), TimeUnit.SECONDS);
                                logger.debug("Executing {} @ {}.", benchmarkRun.getProblem().getName(),
                                        benchmarkRun.getPlanner().getName());
                                BenchmarkRun results = benchmarkRun.execute(validator);
                                logger.debug("Returned results from execute for {} @ {}.",
                                        benchmarkRun.getProblem().getName(), benchmarkRun.getPlanner().getName());
                                future.cancel(false);
                                logger.debug("Cancelled future {} @ {}.", benchmarkRun.getProblem().getName(),
                                        benchmarkRun.getPlanner().getName());
                                planningFuture.complete(results);
                                logger.debug("Completed planning future {} @ {}.",
                                benchmarkRun.getProblem().getName(), benchmarkRun.getPlanner().getName());
                                if (future.isCancelled()) {
                                    logger.debug("Running timeout handler {} @ {}.",
                                            benchmarkRun.getProblem().getName(), benchmarkRun.getPlanner().getName());
                                    Try.run(timeoutHandler::call);
                                    logger.debug("Timeout handler finished {} @ {}.",
                                            benchmarkRun.getProblem().getName(), benchmarkRun.getPlanner().getName());
                                }
                                logger.debug("Completed run future {} @ {}.",
                                    benchmarkRun.getProblem().getName(), benchmarkRun.getPlanner().getName());
                            }, service);
                    return runFuture.thenApply(BenchmarkResults.JsonRun::of);
                })
                .toJavaList();
    }
}

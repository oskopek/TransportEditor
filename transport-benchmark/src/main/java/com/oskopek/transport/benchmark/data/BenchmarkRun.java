package com.oskopek.transport.benchmark.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.planner.Planner;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.validation.Validator;
import com.oskopek.transport.benchmark.ScoreFunction;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A immutable single benchmark planning run. Essentially represents and element in the benchmark matrix.
 * <p>
 * Has two stages:
 * <ol>
 * <li>Basic - contains only the information necessary to run the planner.</li>
 * <li>With results - after the run finishes, fills in the results. A new instance is returned from the
 * {@link #execute(Validator)} method.</li>
 * </ol>
 */
public class BenchmarkRun {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkRun.class);
    private final Domain domain;
    private final Problem problem;
    private final Planner planner;
    private final Double bestScore;
    private final Integer timeout;
    private final ScoreFunction scoreFunction;
    private final Results runResults;


    /**
     * Ammend the run results to a benchmark run.
     *
     * @param run the benchmark run
     * @param runResults the run results
     */
    public BenchmarkRun(BenchmarkRun run, Results runResults) {
        this(run.domain, run.problem, run.planner, run.bestScore, run.timeout, run.scoreFunction,
                runResults);
    }

    /**
     * Create a benchmark run with no results yet.
     *
     * @param domain the domain
     * @param problem the problem
     * @param planner the planner
     * @param bestScore the best available score
     * @param timeout the maximum allowed run time
     * @param scoreFunction the score function
     */
    public BenchmarkRun(Domain domain, Problem problem, Planner planner, Double bestScore, Integer timeout,
            ScoreFunction scoreFunction) {
        this(domain, problem, planner, bestScore, timeout, scoreFunction, null);
    }

    /**
     * Default constructor.
     *
     * @param domain the domain
     * @param problem the problem
     * @param planner the planner
     * @param bestScore the best available score
     * @param timeout the maximum allowed run time
     * @param scoreFunction the score function
     * @param runResults the run results
     */
    protected BenchmarkRun(Domain domain, Problem problem, Planner planner, Double bestScore, Integer timeout,
            ScoreFunction scoreFunction, Results runResults) {
        this.domain = domain;
        this.problem = problem;
        this.planner = planner.copy();
        this.planner.setName(planner.getName());
        this.bestScore = bestScore;
        this.timeout = timeout;
        this.scoreFunction = scoreFunction;
        this.runResults = runResults;
    }

    /**
     * Execute this benchmark run and validate. This is a blocking call.
     *
     * @param validator the validator to use, may be null (no validation)
     * @return the benchmark run with results
     */
    public BenchmarkRun execute(Validator validator) {
        logger.info("Starting benchmark run for domain {}, problem {}, planner {}", domain.getName(), problem.getName(),
                planner.getName());
        long startTime = System.currentTimeMillis();
        Plan plan = planner.startAndWait(domain, problem);
        long endTime = System.currentTimeMillis();
        logger.info("Ending benchmark run for domain {}, problem {}, planner {}", domain.getName(), problem.getName(),
                planner.getName());

        Double score = plan == null ? null : scoreFunction.apply(domain, problem, plan);
        RunExitStatus exitStatus;
        if (plan == null) {
            exitStatus = RunExitStatus.UNSOLVED;
        } else {
            if (validator == null) {
                exitStatus = RunExitStatus.NOTVALIDATED;
            } else {
                exitStatus = validator.isValid(domain, problem, plan) ? RunExitStatus.VALID : RunExitStatus.INVALID;
            }
        }
        return new BenchmarkRun(this, new Results(plan, score, bestScore, exitStatus, startTime, endTime));
    }

    /**
     * Get the domain.
     *
     * @return the domain
     */
    public Domain getDomain() {
        return domain;
    }

    /**
     * Get the problem.
     *
     * @return the problem
     */
    public Problem getProblem() {
        return problem;
    }

    /**
     * Get the planner.
     *
     * @return the planner
     */
    public Planner getPlanner() {
        return planner;
    }

    /**
     * Get the results.
     *
     * @return the results
     */
    public Results getRunResults() {
        return runResults;
    }

    /**
     * Get the timeout.
     *
     * @return the timeout
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Get the bestScore.
     *
     * @return the bestScore
     */
    public Double getBestScore() {
        return bestScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BenchmarkRun)) {
            return false;
        }
        BenchmarkRun that = (BenchmarkRun) o;
        return new EqualsBuilder().append(domain, that.domain).append(problem, that.problem).append(
                planner, that.planner).append(runResults, that.runResults).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(domain).append(problem).append(planner).append(
                runResults).toHashCode();
    }

    /**
     * Designates the result of the benchmark based on the generated plan and validation.
     */
    public enum RunExitStatus {
        /**
         * A plan was not generated.
         */
        UNSOLVED,

        /**
         * A plan was generated but failed to validate.
         */
        INVALID,

        /**
         * A valid plan was generated.
         */
        VALID,

        /**
         * A plan was generated but not validated.
         */
        NOTVALIDATED,

        /**
         * A plan was generated and is valid, but not optimal. Not used at the moment.
         */
        SUBOPT
    }

    /**
     * Results of a benchmark run.
     */
    public static class Results {

        private final Plan plan;
        private final Double score;
        private final Double bestScore;
        private final RunExitStatus exitStatus;
        private final long startTimeMs;
        private final long endTimeMs;
        private final long durationMs;
        private final double quality;

        /**
         * Empty constructor for Jackson.
         */
        @JsonCreator
        private Results() {
            this(null, null, null, null, -1, -1);
        }

        /**
         * Default constructor.
         *
         * @param plan the plan
         * @param score the score
         * @param bestScore the best score for this problem, used to calculate the quality (bestScore / score)
         * @param exitStatus the exit status
         * @param startTimeMs the start time in milliseconds
         * @param endTimeMs the end time in milliseconds
         */
        public Results(Plan plan, Double score, Double bestScore, RunExitStatus exitStatus, long startTimeMs,
                long endTimeMs) {
            this.plan = plan;
            this.score = score;
            this.bestScore = bestScore;
            this.exitStatus = exitStatus;
            this.startTimeMs = startTimeMs;
            this.endTimeMs = endTimeMs;
            this.durationMs = endTimeMs - startTimeMs;
            this.quality = bestScore == null || score == null ? 0.0d : bestScore / (double) score;
        }

        /**
         * Get the plan.
         *
         * @return the plan
         */
        @JsonIgnore
        public Plan getPlan() {
            return plan;
        }

        /**
         * Get the duration in milliseconds.
         *
         * @return the duration in milliseconds
         */
        public long getDurationMs() {
            return durationMs;
        }

        /**
         * Get the start time in milliseconds.
         *
         * @return the start time in millseconds
         */
        public long getStartTimeMs() {
            return startTimeMs;
        }

        /**
         * Get the end time in milliseconds.
         *
         * @return the end time in millseconds
         */
        public long getEndTimeMs() {
            return endTimeMs;
        }

        /**
         * Get the score.
         *
         * @return the score
         */
        public Double getScore() {
            return score;
        }

        /**
         * Get the best score known for this domain.
         *
         * @return the best score
         */
        public Double getBestScore() {
            return bestScore;
        }

        /**
         * Get the exit status.
         *
         * @return the exit status
         */
        public RunExitStatus getExitStatus() {
            return exitStatus;
        }

        /**
         * Get the quality. Defined as {@code bestScore / achievedScore}.
         * @return the quality, NaN if either of the scores is null
         */
        public double getQuality() {
            return quality;
        }
    }

}

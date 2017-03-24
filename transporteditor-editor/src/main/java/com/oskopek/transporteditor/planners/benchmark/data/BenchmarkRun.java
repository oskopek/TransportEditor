package com.oskopek.transporteditor.planners.benchmark.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.planner.Planner;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.planners.benchmark.ScoreFunction;
import com.oskopek.transporteditor.validation.Validator;
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
    private final Integer bestScore;
    private final ScoreFunction scoreFunction;
    private final Results runResults;


    /**
     * Ammend the run results to a benchmark run.
     *
     * @param run the benchmark run
     * @param runResults the run results
     */
    public BenchmarkRun(BenchmarkRun run, Results runResults) {
        this(run.getDomain(), run.getProblem(), run.getPlanner(), run.bestScore, run.scoreFunction, runResults);
    }

    /**
     * Create a benchmark run with no results yet.
     *
     * @param domain the domain
     * @param problem the problem
     * @param planner the planner
     * @param bestScore the best available score
     * @param scoreFunction the score function
     */
    public BenchmarkRun(Domain domain, Problem problem, Planner planner, Integer bestScore,
            ScoreFunction scoreFunction) {
        this(domain, problem, planner, bestScore, scoreFunction, null);
    }

    /**
     * Default constructor.
     *
     * @param domain the domain
     * @param problem the problem
     * @param planner the planner
     * @param bestScore the best available score
     * @param scoreFunction the score function
     * @param runResults the run results
     */
    protected BenchmarkRun(Domain domain, Problem problem, Planner planner, Integer bestScore,
            ScoreFunction scoreFunction, Results runResults) {
        this.domain = domain;
        this.problem = problem;
        this.planner = planner;
        this.bestScore = bestScore;
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

        Integer score = plan == null ? -1 : scoreFunction.apply(domain, problem, plan);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BenchmarkRun)) {
            return false;
        }
        BenchmarkRun that = (BenchmarkRun) o;
        return new EqualsBuilder().append(getDomain(), that.getDomain()).append(getProblem(), that.getProblem()).append(
                getPlanner(), that.getPlanner()).append(getRunResults(), that.getRunResults()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getDomain()).append(getProblem()).append(getPlanner()).append(
                getRunResults()).toHashCode();
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
        NOTVALIDATED
    }

    /**
     * Results of a benchmark run.
     */
    public static class Results {

        private final Plan plan;
        private final Integer score;
        private final Integer bestScore;
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
        public Results(Plan plan, Integer score, Integer bestScore, RunExitStatus exitStatus, long startTimeMs,
                long endTimeMs) {
            this.plan = plan;
            this.score = score;
            this.bestScore = bestScore;
            this.exitStatus = exitStatus;
            this.startTimeMs = startTimeMs;
            this.endTimeMs = endTimeMs;
            this.durationMs = endTimeMs - startTimeMs;
            this.quality = bestScore == null || score == null ? Double.NaN : bestScore / (double) score;
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
        public Integer getScore() {
            return score;
        }

        /**
         * Get the best score known for this domain.
         *
         * @return the best score
         */
        public Integer getBestScore() {
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

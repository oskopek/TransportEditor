package com.oskopek.transporteditor.planners.benchmark.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.planner.Planner;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.planners.benchmark.ScoreFunction;
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
 * {@link #execute()} method.</li>
 * </ol>
 */
public class BenchmarkRun {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkRun.class);
    private final Domain domain;
    private final Problem problem;
    private final Planner planner;
    private final ScoreFunction scoreFunction;
    private final Results runResults;

    /**
     * Ammend the run results to a benchmark run.
     *
     * @param run the benchmark run
     * @param runResults the run results
     */
    public BenchmarkRun(BenchmarkRun run, Results runResults) {
        this(run.getDomain(), run.getProblem(), run.getPlanner(), run.scoreFunction, runResults);
    }

    /**
     * Create a benchmark run with no results yet.
     *
     * @param domain the domain
     * @param problem the problem
     * @param planner the planner
     * @param scoreFunction the score function
     */
    public BenchmarkRun(Domain domain, Problem problem, Planner planner, ScoreFunction scoreFunction) {
        this(domain, problem, planner, scoreFunction, null);
    }

    /**
     * Default constructor.
     *
     * @param domain the domain
     * @param problem the problem
     * @param planner the planner
     * @param scoreFunction the score function
     * @param runResults the run results
     */
    protected BenchmarkRun(Domain domain, Problem problem, Planner planner, ScoreFunction scoreFunction,
            Results runResults) {
        this.domain = domain;
        this.problem = problem;
        this.planner = planner;
        this.scoreFunction = scoreFunction;
        this.runResults = runResults;
    }

    /**
     * Execute this benchmark run. This is a blocking call.
     *
     * @return the benchmark run with results
     */
    public BenchmarkRun execute() {
        logger.info("Starting benchmark run for domain {}, problem {}, planner {}", domain.getName(), problem.getName(),
                planner.getName());
        long startTime = System.currentTimeMillis();
        Plan plan = planner.startAndWait(domain, problem);
        long endTime = System.currentTimeMillis();
        return new BenchmarkRun(this,
                new Results(plan, plan == null ? -1 : scoreFunction.apply(domain, problem, plan), startTime, endTime));
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
     * Results of a benchmark run.
     */
    public static class Results {

        private final Plan plan;
        private final Integer score;
        private final long startTimeMs;
        private final long endTimeMs;

        /**
         * Default constructor.
         *
         * @param plan the plan
         * @param score the score
         * @param startTimeMs the start time in milliseconds
         * @param endTimeMs the end time in milliseconds
         */
        public Results(Plan plan, Integer score, long startTimeMs, long endTimeMs) {
            this.plan = plan;
            this.score = score;
            this.startTimeMs = startTimeMs;
            this.endTimeMs = endTimeMs;
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
            return endTimeMs - startTimeMs;
        }

        /**
         * Get the score.
         *
         * @return the score
         */
        public Integer getScore() {
            return score;
        }
    }

}

package com.oskopek.transport.benchmark.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ArrayTable;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.planner.Planner;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.persistence.SequentialPlanIO;
import com.oskopek.transport.persistence.TemporalPlanIO;
import javaslang.control.Try;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the aggregated results of a {@link com.oskopek.transport.benchmark.Benchmark}.
 */
public final class BenchmarkResults {

    private final transient ArrayTable<Problem, Planner, BenchmarkRun> runTable;
    private final List<JsonRun> runs;

    /**
     * Empty constructor for Jackson.
     */
    @JsonCreator
    private BenchmarkResults() {
        this(null, null);
    }

    /**
     * Default constructor.
     *
     * @param runTable the run table
     * @param runs the JSON run representation
     */
    private BenchmarkResults(ArrayTable<Problem, Planner, BenchmarkRun> runTable, List<JsonRun> runs) {
        this.runTable = runTable;
        this.runs = runs;
    }

    /**
     * Create a results instance from the run table.
     *
     * @param runTable the run table
     * @return the initialized results
     */
    public static BenchmarkResults from(ArrayTable<Problem, Planner, BenchmarkRun> runTable) {
        return new BenchmarkResults(runTable, runTable.values().stream().map(JsonRun::of).collect(Collectors.toList()));
    }

    /**
     * Create a results instance from the run table.
     *
     * @param runs the JSON run representation
     * @return the initialized results
     */
    public static BenchmarkResults from(List<JsonRun> runs) {
        return new BenchmarkResults(null, runs);
    }

    /**
     * Serializes a single {@link BenchmarkRun} into a {@link JsonRun}.
     * @param run the run to serialize
     * @return the serialized run
     */
    public static JsonRun serializeRun(BenchmarkRun run) {
        return JsonRun.of(run);
    }

    /**
     * Serialize the results to JSON.
     *
     * @return the JSON representation
     */
    public String toJson() {
        return new BenchmarkResultsIO().serialize(this);
    }

    /**
     * Get the run table.
     *
     * @return the run table
     */
    @JsonIgnore
    public ArrayTable<Problem, Planner, BenchmarkRun> getRunTable() {
        return runTable;
    }

    /**
     * Get the runs.
     *
     * @return the runs
     */
    @JsonSerialize
    public List<JsonRun> getRuns() {
        return runs;
    }

    /**
     * Data container class, used for direct JSON serialization of the results.
     */
    public static final class JsonRun {
        private final String domain;
        private final String problem;
        private final String planner;
        private final List<String> temporalPlanActions;
        private final List<String> actions;
        private final BenchmarkRun.Results results;

        /**
         * Empty constructor for Jackson.
         */
        @JsonCreator
        private JsonRun() {
            this(null, null, null, null, null, null);
        }

        /**
         * Default constructor.
         *
         * @param domain the domain
         * @param problem the problem
         * @param planner the planner
         * @param temporalPlanActions the temporal actions
         * @param actions the actions
         * @param results the results
         */
        private JsonRun(String domain, String problem, String planner, List<String> temporalPlanActions,
                List<String> actions, BenchmarkRun.Results results) {
            this.domain = domain;
            this.problem = problem;
            this.planner = planner;
            this.temporalPlanActions = temporalPlanActions;
            this.actions = actions;
            this.results = results;
        }

        /**
         * Construct a JSON run instance from the benchmark run instance. Serializes the plan to actions.
         *
         * @param run the benchmark run to serialize
         * @return the initialized JSON run
         */
        public static JsonRun of(BenchmarkRun run) {
            Plan plan = run.getRunResults().getPlan();
            String sequential = Try.of(() -> new SequentialPlanIO(run.getDomain(), run.getProblem()).serialize(plan))
                    .getOrElse((String) null);
            List<String> actions = sequential == null ? null : Arrays.asList(sequential.split("\n"));
            String temporal = new TemporalPlanIO(run.getDomain(), run.getProblem()).serialize(plan);
            List<String> temporalPlanActions = temporal == null ? null : Arrays.asList(temporal.split("\n"));
            return new JsonRun(run.getDomain().getName(), run.getProblem().getName(), run.getPlanner().getName(),
                    temporalPlanActions, actions, run.getRunResults());
        }

        /**
         * Get the domainName.
         *
         * @return the domainName
         */
        public String getDomain() {
            return domain;
        }

        /**
         * Get the problemName.
         *
         * @return the problemName
         */
        public String getProblem() {
            return problem;
        }

        /**
         * Get the plannerName.
         *
         * @return the plannerName
         */
        public String getPlanner() {
            return planner;
        }

        /**
         * Get the temporalPlanActions.
         *
         * @return the temporalPlanActions
         */
        public List<String> getTemporalPlanActions() {
            return temporalPlanActions;
        }

        /**
         * Get the actions.
         *
         * @return the actions
         */
        public List<String> getActions() {
            return actions;
        }

        /**
         * Get the results.
         *
         * @return the results
         */
        public BenchmarkRun.Results getResults() {
            return results;
        }
    }

}

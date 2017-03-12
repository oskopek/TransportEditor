package com.oskopek.transporteditor.planners.benchmark.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ArrayTable;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.planner.Planner;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.persistence.SequentialPlanIO;
import com.oskopek.transporteditor.persistence.TemporalPlanIO;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class BenchmarkResults {

    private final transient ArrayTable<Problem, Planner, BenchmarkRun> runTable;
    private final List<JsonRun> runs;
//    private final SummaryStatistics statistics; TODO

    private BenchmarkResults(ArrayTable<Problem, Planner, BenchmarkRun> runTable, List<JsonRun> runs) {
        this.runTable = runTable;
        this.runs = runs;
    }

    public static BenchmarkResults from(ArrayTable<Problem, Planner, BenchmarkRun> runTable) {
        return new BenchmarkResults(runTable, runTable.values().stream().map(JsonRun::of)
                .collect(Collectors.toList()));
    }

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
    private List<JsonRun> getRuns() {
        return runs;
    }

    private static final class JsonRun {
        private final String domain;
        private final String problem;
        private final String planner;
        private final List<String> temporalPlanActions;
        private final List<String> actions;
        private final BenchmarkRun.Results results;

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

        public static JsonRun of(BenchmarkRun run) {
            Plan plan = run.getRunResults().getPlan();
            List<String> actions = Arrays.asList(new SequentialPlanIO(run.getDomain(), run.getProblem())
                    .serialize(plan).split("\n"));
            List<String> temporalPlanActions = Arrays.asList(new TemporalPlanIO(run.getDomain(), run.getProblem())
                    .serialize(plan).split("\n"));
            return new JsonRun(run.getDomain().getName(), run.getProblem().getName(), run.getPlanner().getName(),
                    temporalPlanActions, actions, run.getRunResults());
        }
    }

}

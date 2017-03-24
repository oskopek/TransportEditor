package com.oskopek.transporteditor.planners.benchmark;

import com.google.common.collect.ArrayTable;
import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.SequentialDomain;
import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.planner.Planner;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.persistence.SequentialPlanIOIT;
import com.oskopek.transporteditor.planners.FastDownwardExternalPlanner;
import com.oskopek.transporteditor.planners.PrologBFSExternalPlanner;
import com.oskopek.transporteditor.planners.benchmark.config.ScoreFunctionType;
import com.oskopek.transporteditor.planners.benchmark.data.BenchmarkMatrix;
import com.oskopek.transporteditor.planners.benchmark.data.BenchmarkResults;
import com.oskopek.transporteditor.planners.benchmark.data.BenchmarkRun;
import static org.assertj.core.api.Assertions.*;

import com.oskopek.transporteditor.planners.benchmark.data.ProblemInfo;
import com.oskopek.transporteditor.validation.SequentialPlanValidator;
import org.assertj.core.api.IterableAssert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Ignore("Use internal planners for this test") // TODO un-ignore
public class BenchmarkIT {

    private BenchmarkMatrix matrix;
    private List<Planner> planners;
    private List<Problem> problems;
    private Map<Problem, ProblemInfo> problemInfo;
    private Domain domain;

    @Before
    public void setUp() throws Exception {
        domain = new SequentialDomain("");
        problems = Arrays.asList(SequentialPlanIOIT.P01SequentialProblem(), SequentialPlanIOIT.P01SequentialProblem()
                .removeVehicle("truck-2"));
        problemInfo = new HashMap<>();
        problemInfo.put(problems.get(0), new ProblemInfo("", 54, ""));
        problemInfo.put(problems.get(1), new ProblemInfo("", 54, ""));
        planners = Arrays.asList(new FastDownwardExternalPlanner(), new PrologBFSExternalPlanner());
        matrix = new BenchmarkMatrix(domain, problems, planners, problemInfo);
    }

    @Test
    public void benchmark() throws Exception {
        Benchmark benchmark = new Benchmark(matrix, ScoreFunctionType.ACTION_COUNT.toScoreFunction(),
                (problem, planner) -> !planner.isAvailable(), new SequentialPlanValidator());

        BenchmarkResults results = benchmark.benchmark(4);
        ArrayTable<Problem, Planner, BenchmarkRun> runTable = results.getRunTable();
        assertThat(runTable).isNotNull();
        assertThat(runTable.size()).isEqualTo(4);
        Plan plan = runTable.at(0, 0).getRunResults().getPlan();
        IterableAssert<TemporalPlanAction> planAssert = assertThat(plan);
        for (BenchmarkRun run : runTable.values()) {
            assertThat(run.getRunResults().getDurationMs()).isGreaterThan(0);
            assertThat(run.getRunResults().getScore()).isEqualTo(6);
            assertThat(run.getRunResults().getExitStatus()).isEqualTo(BenchmarkRun.RunExitStatus.VALID);
        }
    }

}

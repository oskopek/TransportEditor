package com.oskopek.transport.benchmark;

import com.google.common.collect.ArrayTable;
import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.SequentialDomain;
import com.oskopek.transport.model.domain.action.TemporalPlanAction;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.planner.Planner;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.planners.sequential.ForwardBFSPlanner;
import com.oskopek.transport.planners.sequential.SFA1Planner;
import com.oskopek.transport.tools.test.TestUtils;
import com.oskopek.transport.validation.SequentialPlanValidator;
import com.oskopek.transport.benchmark.config.ScoreFunctionType;
import com.oskopek.transport.benchmark.data.BenchmarkMatrix;
import com.oskopek.transport.benchmark.data.BenchmarkResults;
import com.oskopek.transport.benchmark.data.BenchmarkRun;
import com.oskopek.transport.benchmark.data.ProblemInfo;
import org.assertj.core.api.IterableAssert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class BenchmarkIT {

    private BenchmarkMatrix matrix;
    private List<Planner> planners;
    private List<Problem> problems;
    private Map<Problem, ProblemInfo> problemInfo;
    private Domain domain;

    @Before
    public void setUp() throws Exception {
        domain = new SequentialDomain("");
        Problem seqProblem = TestUtils.P01SequentialProblem();
        problems = Arrays.asList(seqProblem, seqProblem.removeVehicle("truck-2"));
        problemInfo = new HashMap<>();
        problemInfo.put(problems.get(0), new ProblemInfo("", 54d, ""));
        problemInfo.put(problems.get(1), new ProblemInfo("", 54d, ""));
        planners = Arrays.asList(new SFA1Planner(), new ForwardBFSPlanner());
        matrix = new BenchmarkMatrix(domain, problems, planners, problemInfo, 100);
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

package com.oskopek.transport.benchmark;

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
        matrix = new BenchmarkMatrix(domain, problems, planners, problemInfo, 15);
    }

    @Test
    public void benchmark() throws Exception {
        Benchmark benchmark = new Benchmark(matrix, ScoreFunctionType.ACTION_COUNT.toScoreFunction(),
                (problem, planner) -> !planner.isAvailable(), new SequentialPlanValidator());

        BenchmarkResults results = benchmark.benchmark(4);
        assertThat(results.getRuns().size()).isEqualTo(4);
        Plan plan = results.getRuns().get(0).getResults().getPlan();
        IterableAssert<TemporalPlanAction> planAssert = assertThat(plan);
        for (BenchmarkResults.JsonRun run : results.getRuns()) {
            assertThat(run.getResults().getDurationMs()).isGreaterThan(0);
            assertThat(run.getResults().getScore()).isEqualTo(6);
            assertThat(run.getResults().getExitStatus()).isEqualTo(BenchmarkRun.RunExitStatus.VALID);
        }
    }

}

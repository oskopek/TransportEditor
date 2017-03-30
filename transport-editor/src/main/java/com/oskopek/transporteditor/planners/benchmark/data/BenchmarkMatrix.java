package com.oskopek.transporteditor.planners.benchmark.data;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.planner.Planner;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transporteditor.planners.benchmark.ScoreFunction;
import javaslang.Function2;
import javaslang.collection.Iterator;
import javaslang.collection.Stream;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;
import java.util.Map;

/**
 * Data container representing the matrix to be benchmarked - all planners with all problems in the given domain.
 */
public class BenchmarkMatrix {

    private final Domain domain;
    private final List<Problem> problems;
    private final List<Planner> planners;

    private final Map<Problem, ProblemInfo> problemInfo;
    private final Integer timeout;

    /**
     * Default constructor.
     *
     * @param domain the domain
     * @param problems the problems
     * @param planners the planners
     * @param problemInfo the problem information
     * @param timeout the timeout for benchmark runs in seconds
     */
    public BenchmarkMatrix(Domain domain, List<Problem> problems, List<Planner> planners,
            Map<Problem, ProblemInfo> problemInfo, Integer timeout) {
        this.domain = domain;
        this.problems = problems;
        this.planners = planners;
        this.problemInfo = problemInfo;
        this.timeout = timeout;
    }

    /**
     * Create individual benchmark runs from the matrix and given parameters.
     *
     * @param skipFunction whether to skip the given benchmark run
     * @param scoreFunction the score function to use for evaluating the run results
     * @return an iterator of initialized benchmark runs
     */
    public Iterator<BenchmarkRun> toBenchmarkRuns(Function2<Problem, Planner, Boolean> skipFunction,
            ScoreFunction scoreFunction) {
        return Stream.ofAll(getProblems()).crossProduct(getPlanners()).filter(t -> !skipFunction.apply(t._1, t._2)).map(
                t -> new BenchmarkRun(domain, t._1, t._2.copy(), problemInfo.get(t._1).getBestScore(), timeout,
                        scoreFunction));
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
     * Get the problems.
     *
     * @return the problems
     */
    public List<Problem> getProblems() {
        return problems;
    }

    /**
     * Get the planners.
     *
     * @return the planners
     */
    public List<Planner> getPlanners() {
        return planners;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BenchmarkMatrix)) {
            return false;
        }
        BenchmarkMatrix that = (BenchmarkMatrix) o;
        return new EqualsBuilder().append(getPlanners(), that.getPlanners()).append(getDomain(), that.getDomain())
                .append(getProblems(), that.getProblems()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getPlanners()).append(getDomain()).append(getProblems()).toHashCode();
    }


}

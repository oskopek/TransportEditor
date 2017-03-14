package com.oskopek.transporteditor.planners.benchmark.data;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.planner.Planner;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.planners.benchmark.ScoreFunction;
import javaslang.Function2;
import javaslang.collection.Iterator;
import javaslang.collection.Stream;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class BenchmarkMatrix {

    private final Domain domain;
    private final List<Problem> problems;
    private final List<Planner> planners;

    public BenchmarkMatrix(Domain domain, List<Problem> problems, List<Planner> planners) {
        this.domain = domain;
        this.problems = problems;
        this.planners = planners;
    }

    public Iterator<BenchmarkRun> toBenchmarkRuns(Function2<Problem, Planner, Boolean> skipFunction,
            ScoreFunction scoreFunction) {
        return Stream.ofAll(getProblems()).crossProduct(getPlanners()).filter(t -> !skipFunction.apply(t._1, t._2)).map(
                t -> new BenchmarkRun(domain, t._1, t._2.copy(), scoreFunction));
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

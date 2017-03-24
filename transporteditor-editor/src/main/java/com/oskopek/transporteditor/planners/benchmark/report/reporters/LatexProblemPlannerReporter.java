package com.oskopek.transporteditor.planners.benchmark.report.reporters;

import com.oskopek.transporteditor.planners.benchmark.data.BenchmarkResults;
import com.oskopek.transporteditor.planners.benchmark.report.FreemarkerFiller;
import com.oskopek.transporteditor.planners.benchmark.report.Reporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Creates a LaTeX table with problems as rows and planners as columns, with the
 * table elements specified by a getter function.
 */
public abstract class LatexProblemPlannerReporter implements Reporter {

    private final Function<BenchmarkResults.JsonRun, Object> dataGetter;

    /**
     * Default constructor.
     *
     * @param dataGetter the getter function for table elements
     */
    public LatexProblemPlannerReporter(Function<BenchmarkResults.JsonRun, Object> dataGetter) {
        this.dataGetter = dataGetter;
    }

    @Override
    public String generateReport(List<BenchmarkResults.JsonRun> results) {
        return FreemarkerFiller.generate("problem_planner_table.tex.ftl", fillInfo(results, dataGetter));
    }

    /**
     * Fill the Freemarker info map with the planners, problems and element data.
     *
     * @param runs the benchmark runs to take data from
     * @param dataGetter the getter to use for element data
     * @return the filled-in map to be passed to a Freemarker template
     */
    private Map<String, Object> fillInfo(List<BenchmarkResults.JsonRun> runs,
            Function<BenchmarkResults.JsonRun, Object> dataGetter) {

        Map<String, Object> info = new HashMap<>();
        // all domains are the same
        info.put("domain", runs.size() == 0 ? "" : runs.get(0).getDomain());
        info.put("planners", runs.stream().flatMap(r -> Stream.of(r.getPlanner())).sorted().distinct()
                .collect(Collectors.toList()));
        info.put("problems", runs.stream().flatMap(r -> Stream.of(r.getProblem())).sorted().distinct()
                .collect(Collectors.toList()));

        info.put("elements", javaslang.collection.Stream.ofAll(runs)
                .groupBy(BenchmarkResults.JsonRun::getPlanner)
                .mapValues(v -> v
                        .groupBy(BenchmarkResults.JsonRun::getProblem)
                        .mapValues(v2 -> v2.map(dataGetter).toJavaOptional().get()) // TODO: fixme
                        .toJavaMap())
                .toJavaMap()
        );
        return info;
    }
}

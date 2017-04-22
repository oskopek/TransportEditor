package com.oskopek.transport.report.reporters;

import com.oskopek.transport.benchmark.data.BenchmarkResults;
import com.oskopek.transport.report.FreemarkerFiller;
import com.oskopek.transport.report.Reporter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
     * Used for formatting quality and any other decimal numbers (fixed 2 decimal places).
     */
    protected static final DecimalFormat decimalFormat;

    /**
     * Used for formatting score (optional two decimal places, if possible no decimal places).
     */
    protected static final DecimalFormat scoreFormat;

    static {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        symbols.setDecimalSeparator('.');
        decimalFormat = new DecimalFormat("0.00");
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setDecimalFormatSymbols(symbols);

        scoreFormat = new DecimalFormat();
        scoreFormat.setGroupingUsed(false);
        scoreFormat.setDecimalFormatSymbols(symbols);
        scoreFormat.setMinimumFractionDigits(0);
        scoreFormat.setMaximumFractionDigits(2);
    }

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
    private static Map<String, Object> fillInfo(List<BenchmarkResults.JsonRun> runs,
            Function<BenchmarkResults.JsonRun, Object> dataGetter) {
        Map<String, Object> info = new HashMap<>(10);
        // all domains are the same
        info.put("domain", runs.size() == 0 ? "" : runs.get(0).getDomain());
        info.put("planners", runs.stream().flatMap(r -> Stream.of(r.getPlanner())).sorted().distinct()
                .collect(Collectors.toList()));
        info.put("problems", runs.stream().flatMap(r -> Stream.of(r.getProblem())).sorted().distinct()
                .collect(Collectors.toList()));

        javaslang.collection.Map<String, javaslang.collection.Map<String, BenchmarkResults.JsonRun>> grouped
                = javaslang.collection.Stream.ofAll(runs)
                .groupBy(BenchmarkResults.JsonRun::getPlanner)
                .mapValues(v -> v
                        .groupBy(BenchmarkResults.JsonRun::getProblem)
                        .mapValues(v2 -> v2.getOrElse((BenchmarkResults.JsonRun) null)));
        Map<String, Map<String, String>> status = grouped.mapValues(v ->
                v.mapValues(r -> r.getResults().getExitStatus().toString()).toJavaMap()).toJavaMap();
        info.put("status", status);

        info.put("elements", grouped.mapValues(v -> v.mapValues(dataGetter).toJavaMap()).toJavaMap());
        return info;
    }
}

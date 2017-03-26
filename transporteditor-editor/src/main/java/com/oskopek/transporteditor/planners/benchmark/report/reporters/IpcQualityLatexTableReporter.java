package com.oskopek.transporteditor.planners.benchmark.report.reporters;

import com.oskopek.transporteditor.planners.benchmark.data.BenchmarkResults;
import com.oskopek.transporteditor.planners.benchmark.data.BenchmarkRun;
import com.oskopek.transporteditor.planners.benchmark.report.FreemarkerFiller;
import com.oskopek.transporteditor.planners.benchmark.report.Reporter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Creates a LaTeX table formatted like the IPC 2008 result tables with problems as rows and planners as columns,
 * with the table elements consisting of score and quality or {@code uns.} if the instance was not solved.
 */
public class IpcQualityLatexTableReporter implements Reporter {

    @Override
    public String generateReport(List<BenchmarkResults.JsonRun> results) {
        return FreemarkerFiller.generate("problem_planner_table_ipc.tex.ftl", fillInfo(results));
    }

    /**
     * Fill the Freemarker info map with the planners, problems and quality data.
     *
     * @param runs the benchmark runs to take data from
     * @return the filled-in map to be passed to a Freemarker template
     */
    private Map<String, Object> fillInfo(List<BenchmarkResults.JsonRun> runs) {
        Map<String, Object> info = new HashMap<>();
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

        DecimalFormat qualityFormat = new DecimalFormat("0.00");
        qualityFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
        Map<String, Map<String, String>> quality = grouped
                .mapValues(v -> v.mapValues(jsonRun -> jsonRun.getResults().getQuality())
                        .mapValues(qualityFormat::format).toJavaMap()).toJavaMap();
        Map<String, Map<String, String>> score = grouped
                .mapValues(v -> v.mapValues(jsonRun -> jsonRun.getResults().getScore())
                        .mapValues(score2 -> score2 == null ? null : score2.toString()).toJavaMap()).toJavaMap();
        Map<String, String> total = javaslang.collection.Stream.ofAll(runs)
                .groupBy(BenchmarkResults.JsonRun::getPlanner).mapValues(groupedRuns -> groupedRuns.toJavaStream()
                        .mapToDouble(r -> r.getResults().getQuality()).sum())
                .mapValues(qualityFormat::format).toJavaMap();

        javaslang.collection.Map<String, javaslang.collection.List<Integer>> bestList
                = javaslang.collection.Stream.ofAll(runs).groupBy(BenchmarkResults.JsonRun::getProblem)
                .mapValues(v -> v
                        .map(BenchmarkResults.JsonRun::getResults)
                        .map(BenchmarkRun.Results::getBestScore)
                        .filter(l -> l != null && l != 0)
                        .distinct()
                        .toList())
                .mapValues(list -> list.isEmpty() ? list.append(null) : list);
        bestList.forEach((problem, bestScores) -> {
            if (bestScores.size() != 1) {
                throw new IllegalStateException("Problem does not have equal best scores: " + bestScores);
            }
        });
        Map<String, String> best = bestList.mapValues(l -> l.get(0)).mapValues(l -> l == null ? null : l.toString())
                .toJavaMap();

        Map<String, Map<String, String>> status = grouped.mapValues(v ->
                v.mapValues(r -> r.getResults().getExitStatus().toString()).toJavaMap()).toJavaMap();

        info.put("quality", quality);
        info.put("score", score);
        info.put("total", total);
        info.put("best", best);
        info.put("status", status);
        return info;
    }

    @Override
    public String getFileName() {
        return "ipc_score_table.tex";
    }
}

package com.oskopek.transport.report.reporters;

import com.oskopek.transport.report.Reporter;

/**
 * Generates a line chart using scores as the values.
 */
public class ScoreLineChartReporter extends LineChartReporter implements Reporter {

    /**
     * Default constructor.
     */
    public ScoreLineChartReporter() {
        super(r -> r.getResults().getScore(), DEFAULT_WIDTH, DEFAULT_HEIGHT, "Planner scores", "Score");
    }

    @Override
    public String getFileName() {
        return "score_line_chart.svg";
    }
}

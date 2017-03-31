package com.oskopek.transport.report.reporters;

import com.oskopek.transport.report.Reporter;

/**
 * Generates a line chart using runtimes in seconds as the values.
 */
public class RunTimeLineChartReporter extends LineChartReporter implements Reporter {

    /**
     * Default constructor.
     */
    public RunTimeLineChartReporter() {
        super(r -> {
                    long duration = r.getResults().getDurationMs();
                    if (duration == 0L) {
                        return null;
                    } else {
                        return duration / 1000d;
                    }
                }, DEFAULT_WIDTH, DEFAULT_HEIGHT, "Planner runtimes",
                "Runtime in seconds");
    }

    @Override
    public String getFileName() {
        return "runtime_line_chart.svg";
    }
}

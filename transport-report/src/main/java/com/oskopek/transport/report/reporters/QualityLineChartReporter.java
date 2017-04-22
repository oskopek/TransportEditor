package com.oskopek.transport.report.reporters;

import com.oskopek.transport.report.Reporter;

/**
 * Generates a line chart using qualities as the values.
 */
public class QualityLineChartReporter extends LineChartReporter implements Reporter {

    /**
     * Default constructor.
     */
    public QualityLineChartReporter() {
        super(r -> r.getResults().getQuality(), DEFAULT_WIDTH, DEFAULT_HEIGHT, "Planner qualities", "Quality");
    }

    @Override
    public String getFileName() {
        return "quality_line_chart.svg";
    }
}

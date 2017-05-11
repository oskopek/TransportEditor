package com.oskopek.transport.report.reporters;

import com.oskopek.transport.report.Reporter;
import org.jfree.chart.axis.*;

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
    protected ValueAxis createValueAxis() {
        ValueAxis valueAxis = new NumberAxis(getValueAxisLabel());
        TickUnits units = new TickUnits();
        units.add(new NumberTickUnit(0.1d));
        valueAxis.setStandardTickUnits(units);
        return valueAxis;
    }

    @Override
    public String getFileName() {
        return "quality_line_chart.svg";
    }
}

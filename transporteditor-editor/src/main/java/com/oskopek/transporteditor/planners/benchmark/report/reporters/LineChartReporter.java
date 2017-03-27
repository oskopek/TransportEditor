package com.oskopek.transporteditor.planners.benchmark.report.reporters;

import com.oskopek.transporteditor.planners.benchmark.data.BenchmarkResults;
import com.oskopek.transporteditor.planners.benchmark.report.Reporter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Generates JFreeChart line charts with problems on the X axis and planners as the lines. The values for
 * a problem and planner pair are obtained using the given data getter.
 */
public abstract class LineChartReporter implements Reporter {

    private final Function<BenchmarkResults.JsonRun, Number> dataGetter;
    private final int width;
    private final int height;
    private final String title;
    private final String valueAxis;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected static final int DEFAULT_WIDTH = 500;
    protected static final int DEFAULT_HEIGHT = 500;

    /**
     * Default constructor.
     *
     * @param dataGetter the getter function for values
     * @param width the width of the plot
     * @param height the height of the plot
     * @param title the title of the plot
     * @param valueAxis the value axis (Y-axis) label
     */
    public LineChartReporter(Function<BenchmarkResults.JsonRun, Number> dataGetter, int width, int height,
            String title, String valueAxis) {
        this.dataGetter = dataGetter;
        this.width = width;
        this.height = height;
        this.title = title;
        this.valueAxis = valueAxis;
    }

    @Override
    public String generateReport(List<BenchmarkResults.JsonRun> results) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        results.stream().sorted(Comparator.comparing(BenchmarkResults.JsonRun::getProblem))
                .forEach(r -> dataset.addValue(dataGetter.apply(r), r.getPlanner(), r.getProblem()));

        if (GraphicsEnvironment.isHeadless()) {
            logger.warn("Headless environment, skipping JFreeChart chart creation.");
            return "";
        }

        JFreeChart chart = ChartFactory.createLineChart(title, "Problem", valueAxis,
                dataset, PlotOrientation.VERTICAL, true, false, false);

        int efficientWidth = width + width * Math.round(dataset.getColumnCount() * 25 / (float) width);
        SVGGraphics2D g2 = new SVGGraphics2D(efficientWidth, height);
        Rectangle r = new Rectangle(0, 0, efficientWidth, height);
        chart.draw(g2, r);
        return "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\""
            + ">\n" + g2.getSVGElement();
    }
}

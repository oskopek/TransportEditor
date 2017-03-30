package com.oskopek.transport.view.plan;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A horizontal Gantt chart extension of {@link XYChart} with discrete time steps and auto-layout.
 */
public class GanttChart extends XYChart<Number, String> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int BLOCK_HEIGHT;

    /**
     * Default constructor with 10 blockHeight.
     *
     * @param xLabel the x axis label
     * @param yLabel the y axis label
     */
    public GanttChart(String xLabel, String yLabel) {
        this(xLabel, yLabel, 10);
    }

    /**
     * Default constructor.
     *
     * @param xLabel the x axis label
     * @param yLabel the y axis label
     * @param blockHeight the height of blocks
     */
    public GanttChart(String xLabel, String yLabel, int blockHeight) {
        super(new NumberAxis(), new CategoryAxis());
        this.BLOCK_HEIGHT = blockHeight;
        getXAxis().setLabel(xLabel);
        getYAxis().setLabel(yLabel);

        getYAxis().setTickLabelGap(BLOCK_HEIGHT);
    }

    @Override
    protected void dataItemAdded(Series<Number, String> series, int itemIndex, Data<Number, String> item) {
        getPlotChildren().add(createBlock(item));
    }

    @Override
    protected void dataItemRemoved(Data<Number, String> item, Series<Number, String> series) {
        getPlotChildren().remove(item.getNode());
        removeDataItemFromDisplay(series, item);
    }

    @Override
    protected void dataItemChanged(Data<Number, String> item) {
        // intentionally empty
    }

    @Override
    protected void seriesAdded(Series<Number, String> series, int seriesIndex) {
        ObservableList<Data<Number, String>> seriesData = series.getData();
        for (int i = 0; i < series.getData().size(); i++) {
            Data<Number, String> data = seriesData.get(i);
            dataItemAdded(series, i, data);
        }
    }

    @Override
    protected void seriesRemoved(Series<Number, String> series) {
        series.getData().forEach(data -> dataItemRemoved(data, series));
        removeSeriesFromDisplay(series);
    }

    @Override
    protected void updateAxisRange() {
        final Axis<Number> xAxis = getXAxis();
        final Axis<String> yAxis = getYAxis();
        List<Number> xData = null;
        if (xAxis.isAutoRanging()) {
            xData = new ArrayList<>();
        }
        List<String> yData = null;
        if (yAxis.isAutoRanging()) {
            yData = new ArrayList<>();
        }
        for (Series<Number, String> series : getData()) {
            for (Data<Number, String> data : series.getData()) {
                if (xData != null) {
                    xData.add(data.getXValue());
                    xData.add(xAxis.toRealValue(((ExtraValue) data.getExtraValue()).getTo().doubleValue()));
                }
                if (yData != null) {
                    yData.add(data.getYValue());
                }
            }
        }
        if (xData != null) {
            xAxis.invalidateRange(xData);
        }
        if (yData != null) {
            yAxis.invalidateRange(yData);
        }
    }

    @Override
    protected void layoutPlotChildren() {
        for (Series<Number, String> series : getData()) {
            Iterator<Data<Number, String>> iterator = getDisplayedDataIterator(series);
            while (iterator.hasNext()) {
                Data<Number, String> item = iterator.next();
                double x = getXAxis().getDisplayPosition(item.getXValue());
                double y = getYAxis().getDisplayPosition(item.getYValue());
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                Node block = item.getNode();
                Number from = ((ExtraValue) item.getExtraValue()).getFrom();
                Number to = ((ExtraValue) item.getExtraValue()).getTo();
                double length = to.doubleValue() - from.doubleValue();
                if (block == null) {
                    logger.warn("While plotting gantt chart found null block.");
                    continue;
                }
                if (!(block instanceof StackPane)) {
                    logger.warn("While plotting gantt chart found block that isn't StackPane.");
                    continue;
                }

                StackPane region = (StackPane) item.getNode();
                Rectangle rectangle;
                if (region.getShape() == null) {
                    rectangle = new Rectangle(length, BLOCK_HEIGHT);
                } else if (region.getShape() instanceof Rectangle) {
                    rectangle = (Rectangle) region.getShape();
                } else {
                    return;
                }
                rectangle.setWidth(length * Math.abs(((NumberAxis) getXAxis()).getScale()));
                rectangle.setHeight(BLOCK_HEIGHT);
                y -= BLOCK_HEIGHT / 2.0;

                region.setShape(null);
                region.setShape(rectangle);
                region.setScaleShape(false);
                region.setCenterShape(false);
                region.setCacheShape(false);

                block.setLayoutX(x);
                block.setLayoutY(y);
            }
        }
    }

    /**
     * Create the actual displayable {@link Node} for a given data item.
     *
     * @param item the item to display
     * @return the node that will be displayed
     */
    private static Node createBlock(final Data<Number, String> item) {
        if (item.getNode() == null) {
            Pane block = new StackPane();
            block.setBackground(new Background(
                    new BackgroundFill(((ExtraValue) item.getExtraValue()).getColor(), CornerRadii.EMPTY,
                            Insets.EMPTY)));
            String tooltipText = ((ExtraValue) item.getExtraValue()).getTooltipText();
            if (tooltipText != null && !tooltipText.isEmpty()) {
                Tooltip.install(block, new Tooltip(tooltipText));
            }
            item.setNode(block);
        }
        return item.getNode();
    }

    /**
     * Data object to be displayed in the chart.
     */
    protected static class ExtraValue {

        private final Color color;
        private final Number from;
        private final Number to;
        private final String tooltipText;

        /**
         * Default constructor.
         *
         * @param color the color
         * @param from the from time
         * @param to the to time
         * @param tooltipText the tooltipText
         */
        protected ExtraValue(Color color, Number from, Number to, String tooltipText) {
            this.color = color;
            this.from = from;
            this.to = to;
            this.tooltipText = tooltipText;
        }

        /**
         * Get the color.
         *
         * @return the color
         */
        public Color getColor() {
            return color;
        }

        /**
         * Get the from time.
         *
         * @return the from time
         */
        public Number getFrom() {
            return from;
        }

        /**
         * Get the to time.
         *
         * @return the to time
         */
        public Number getTo() {
            return to;
        }

        /**
         * Get the action.
         *
         * @return the action
         */
        public String getTooltipText() {
            return tooltipText;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(getColor()).append(getFrom()).append(getTo())
                    .append(tooltipText).toHashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ExtraValue)) {
                return false;
            }
            ExtraValue that = (ExtraValue) o;
            return new EqualsBuilder().append(getColor(), that.getColor()).append(getFrom(), that.getFrom())
                    .append(getTo(), that.getTo()).append(getTooltipText(), that.getTooltipText()).isEquals();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("color", color).append("from", from).append("to", to)
                    .append("tooltipText", tooltipText).toString();
        }
    }
}

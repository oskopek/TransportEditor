package com.oskopek.transporteditor.view.plan;

import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import com.oskopek.transporteditor.model.problem.ActionObject;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.persistence.SequentialPlanIO;
import javafx.collections.FXCollections;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javaslang.Tuple;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Gantt chart specialized for {@link TemporalPlanAction}s. Distinguishes actions by color and provides useful tooltips.
 */
public final class TemporalGanttChart extends GanttChart {

    private final Set<TemporalPlanAction> temporalPlanActionSet;
    private final Map<String, Color> colorMap;
    private final Color defaultColor = Color.BLACK;

    /**
     * Default constructor.
     *
     * @param actions the actions to display
     */
    private TemporalGanttChart(Collection<? extends TemporalPlanAction> actions) {
        super("Time", "Action Object");
        this.temporalPlanActionSet = Collections.unmodifiableSet(new HashSet<>(actions));

        setTitle("");
        setLegendVisible(false);
        colorMap = javaslang.collection.HashMap.of("drive", Color.BLUE).put("pick-up", Color.GREEN).put("refuel",
                Color.VIOLET).put("drop", Color.RED).toJavaMap();
        setData(FXCollections.observableList(computeData()));
        double minWidth = temporalPlanActionSet.stream().map(TemporalPlanAction::getEndTimestamp)
                .max(Double::compareTo).map(d -> d * 10).orElse(0d);
        setMinWidth(Math.max(minWidth, 300d));
    }

    /**
     * Default builder method.
     *
     * @param actions the actions to display
     * @return a gantt chart showing the actions
     */
    public static TemporalGanttChart build(Collection<? extends TemporalPlanAction> actions) {
        return new TemporalGanttChart(actions);
    }

    /**
     * Util method for converting temporal actions into chartable data.
     *
     * @return the list of data {@link javafx.scene.chart.XYChart.Series}
     */
    private List<Series<Number, String>> computeData() {
        javaslang.collection.Map<ActionObject, javaslang.collection.Stream<TemporalPlanAction>> who = Stream.ofAll(
                temporalPlanActionSet).groupBy(ta -> ta.getAction().getWho());
        javaslang.collection.Map<ActionObject, javaslang.collection.Stream<TemporalPlanAction>> what = Stream.ofAll(
                temporalPlanActionSet).groupBy(ta -> ta.getAction().getWhat())
                .filter(t -> !Location.class.isInstance(t._1));
        javaslang.collection.Map<ActionObject, javaslang.collection.Stream<TemporalPlanAction>> objs = who.merge(what);

        return objs.map((actionObject, temporalPlanActions) -> {
            XYChart.Series<Number, String> series = new Series<>();
            series.setName(actionObject.getName());
            temporalPlanActions.forEach(t -> {
                Color color = colorMap.get(t.getAction().getName());
                if (color == null) {
                    color = defaultColor;
                }
                series.getData().add(new XYChart.Data<>(t.getStartTimestamp(), actionObject.getName(),
                        new ExtraValue(color, t.getStartTimestamp(), t.getEndTimestamp(),
                                SequentialPlanIO.toApproximateValFormat(t.getAction()))));
            });
            return Tuple.of(actionObject, series);
        }).values().sortBy(Series::getName).reverseIterator().toJavaList();
    }

}

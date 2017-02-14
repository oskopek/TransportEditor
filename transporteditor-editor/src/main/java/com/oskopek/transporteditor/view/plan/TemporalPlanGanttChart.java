package com.oskopek.transporteditor.view.plan;

import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.ActionObject;
import com.oskopek.transporteditor.model.problem.Location;
import javafx.collections.FXCollections;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javaslang.Tuple;
import javaslang.collection.Stream;

import java.util.*;

public final class TemporalPlanGanttChart extends GanttChart {

    private final Set<TemporalPlanAction> temporalPlanActionSet;
    private final Map<String, Color> colorMap;
    private final Color defaultColor = Color.BLACK;

    private TemporalPlanGanttChart(Collection<TemporalPlanAction> actions) {
        super("Time", "Action Object");
        this.temporalPlanActionSet = Collections.unmodifiableSet(new HashSet<>(actions));

        setTitle("");
        setLegendVisible(false);
        colorMap = javaslang.collection.HashMap.of("drive", Color.BLUE).put("pick-up", Color.GREEN).put("refuel",
                Color.VIOLET).put("drop", Color.RED).toJavaMap();
        setData(FXCollections.observableList(computeData()));
        double minWidth = temporalPlanActionSet.stream().map(TemporalPlanAction::getEndTimestamp)
                .max(Integer::compareTo).map(Double::valueOf).map(d -> d * 10).orElse(0d);
        setMinWidth(Math.max(minWidth, 300d));
    }

    public static TemporalPlanGanttChart build(Plan plan) {
        return TemporalPlanGanttChart.build(plan.getTemporalPlanActions());
    }

    public static TemporalPlanGanttChart build(Collection<TemporalPlanAction> actions) {
        return new TemporalPlanGanttChart(actions);
    }

    private List<Series<Number, String>> computeData() {
        javaslang.collection.Map<ActionObject, javaslang.collection.Stream<TemporalPlanAction>> who = Stream.ofAll(
                temporalPlanActionSet).groupBy(ta -> ta.getAction().getWho());
        javaslang.collection.Map<ActionObject, javaslang.collection.Stream<TemporalPlanAction>> what = Stream.ofAll(
                temporalPlanActionSet).groupBy(ta -> ta.getAction().getWhat())
                .filter(t -> !Location.class.isInstance(t._1));
        javaslang.collection.Map<ActionObject, javaslang.collection.Stream<TemporalPlanAction>> objs = who.merge(what);

        return objs.map((actionObject, temporalPlanActions) -> {
            XYChart.Series<Number, String> series = new Series<>();
            temporalPlanActions.forEach(t -> {
                Color color = colorMap.get(t.getAction().getName());
                if (color == null) {
                    color = defaultColor;
                }
                series.getData().add(new XYChart.Data<>(t.getStartTimestamp(), actionObject.getName(),
                        new ExtraValue(color, t.getStartTimestamp(), t.getEndTimestamp())));
            });
            return Tuple.of(actionObject, series);
        }).values().toJavaList();
    }

}

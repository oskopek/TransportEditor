package com.oskopek.transporteditor.view.plan;

import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.Locatable;
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

        javaslang.collection.Map<Locatable, javaslang.collection.Stream<TemporalPlanAction>> who = Stream.ofAll(
                temporalPlanActionSet).groupBy(ta -> ta.getAction().getWho());

        javaslang.collection.Seq<XYChart.Series<Number, String>> series = who.map((locatable, temporalPlanActions) -> {
            XYChart.Series<Number, String> serie = new Series<>();

            temporalPlanActions.forEach(t -> {
                Color color = colorMap.get(t.getAction().getName());
                if (color == null) {
                    color = defaultColor;
                }
                serie.getData().add(new XYChart.Data<>(t.getStartTimestamp(), t.getAction().getWho().getName(),
                        new ExtraValue(color, t.getStartTimestamp(), t.getEndTimestamp())));
            });

            return Tuple.of(locatable, serie);
        }).values();

        getData().addAll(series.toJavaList());
    }

    public static TemporalPlanGanttChart build(Plan plan) {
        return new TemporalPlanGanttChart(plan.getTemporalPlanActions());
    }

    public TemporalPlanGanttChart rotate(double degrees) {
        setRotate(degrees);
        return this;
    }


}

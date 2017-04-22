package com.oskopek.transport.persistence;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.domain.action.TemporalPlanAction;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.TemporalPlan;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.persistence.antlr4.PlanLexer;
import com.oskopek.transport.persistence.antlr4.PlanParser;
import javaslang.Tuple;
import javaslang.Tuple2;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reader and writer for {@link TemporalPlan} to and from the VAL format (supports only Transport domain plans).
 * Uses ANTLR and {@link SequentialPlanIO}.
 */
public class TemporalPlanIO implements DataIO<Plan> {

    private final Domain domain;
    private final Problem problem;

    /**
     * Default constructor.
     *
     * @param domain the domain associated
     * @param problem the problem associated
     */
    public TemporalPlanIO(Domain domain, Problem problem) {
        this.domain = domain;
        this.problem = problem;
    }

    /**
     * Transforms a temporal action into a VAL-format plan action.
     * Uses {@link SequentialPlanIO#serializeActionSimple(Action)} for the action itself.
     *
     * @param temporalPlanAction the temporal action to serialize
     * @return the serialized action in a single line
     */
    private static String serializeTemporalPlanAction(TemporalPlanAction temporalPlanAction) {
        String action = SequentialPlanIO.serializeActionSimple(temporalPlanAction.getAction()).append(')').toString();
        StringBuilder str = new StringBuilder();
        DecimalFormat df = new DecimalFormat("0.00000");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);
        Double duration = temporalPlanAction.getEndTimestamp() - temporalPlanAction.getStartTimestamp();
        str.append(df.format(temporalPlanAction.getStartTimestamp())).append(':').append(' ').append(action).append(
                " [").append(df.format(duration)).append("]\n");
        return str.toString();
    }

    @Override
    public String serialize(Plan plan) {
        if (plan == null) {
            return null;
        }
        StringBuilder str = new StringBuilder();
        Collection<TemporalPlanAction> actionSet = plan.getTemporalPlanActions();
        Comparator<Tuple2<TemporalPlanAction, String>> comparator = Comparator.comparing(a -> a._1);
        comparator = comparator.thenComparing(a -> a._2);

        actionSet.stream().map(a -> Tuple.of(a, TemporalPlanIO.serializeTemporalPlanAction(a))).sorted(comparator)
                .forEach(t -> str.append(t._2));

        Double totalTime = 0d;
        Optional<Double> last = actionSet.stream().map(TemporalPlanAction::getEndTimestamp).max(Double::compare);
        Optional<Double> first = actionSet.stream().map(TemporalPlanAction::getStartTimestamp).min(Double::compare);
        if (last.isPresent() && first.isPresent()) {
            totalTime = last.get() - first.get();
        }
        str.append("; total-time = ").append(totalTime).append(" (general-cost)\n");
        return str.toString();
    }

    @Override
    public TemporalPlan parse(String contents) {
        Set<TemporalPlanAction> actions = new HashSet<>();

        PlanParser parser = new PlanParser(new CommonTokenStream(new PlanLexer(new ANTLRInputStream(contents))));
        ErrorDetectionListener listener = new ErrorDetectionListener();
        parser.addErrorListener(listener);
        PlanParser.PlanContext context = parser.plan();
        if (listener.isFail()) {
            throw new IllegalArgumentException("Plan failed to parse.");
        }
        PlanParser.TemporalPlanContext tempContext = context.temporalPlan();
        if (tempContext == null) {
            return null;
        }
        for (PlanParser.TemporalActionContext action : tempContext.temporalAction()) {
            double duration = Double.parseDouble(action.duration().NUMBER().getText());
            double time = Double.parseDouble(action.time().NUMBER().getText());
            String actionString = String.format("(%s)",
                    String.join(" ", action.sequentialAction().action().NAME().getText(),
                            action.sequentialAction().object().stream().map(o -> o.NAME().getText())
                                    .collect(Collectors.joining(" "))));
            Action actionObj = SequentialPlanIO.parsePlanAction(domain, problem, actionString);
            TemporalPlanAction temporalPlanAction = new TemporalPlanAction(actionObj, time, time + duration);
            actions.add(temporalPlanAction);
        }

        return new TemporalPlan(actions);
    }

}

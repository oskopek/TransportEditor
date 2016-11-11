/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import com.oskopek.transporteditor.model.plan.TemporalPlan;
import com.oskopek.transporteditor.model.problem.Problem;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TemporalPlanIO implements DataReader<TemporalPlan>, DataWriter<TemporalPlan> {

    private final Domain domain;
    private final Problem problem;

    public TemporalPlanIO(Domain domain, Problem problem) {
        this.domain = domain;
        this.problem = problem;
    }

    private static String serializeTemporalPlanAction(TemporalPlanAction temporalPlanAction) {
        String action = SequentialPlanIO.serializeActionSimple(temporalPlanAction.getAction()).append(")").toString();
        StringBuilder str = new StringBuilder();
        DecimalFormat df = new DecimalFormat("0.000");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);
        Integer duration = temporalPlanAction.getEndTimestamp() - temporalPlanAction.getStartTimestamp();
        str.append(df.format(temporalPlanAction.getStartTimestamp())).append(':').append(" ").append(action).append(
                " [").append(df.format(duration)).append("]\n");
        return str.toString();
    }

    @Override
    public String serialize(TemporalPlan plan) throws IllegalArgumentException {
        StringBuilder str = new StringBuilder();
        Set<TemporalPlanAction> actionSet = plan.getTemporalPlanActions();
        actionSet.stream().map(TemporalPlanIO::serializeTemporalPlanAction).sorted().forEach(str::append);

        Integer totalTime = 0;
        Optional<Integer> last = actionSet.stream().map(TemporalPlanAction::getEndTimestamp).max(Integer::compare);
        Optional<Integer> first = actionSet.stream().map(TemporalPlanAction::getStartTimestamp).min(Integer::compare);
        if (last.isPresent() && first.isPresent()) {
            totalTime = last.get() - first.get();
        }
        str.append("; total-time = ").append(totalTime).append(" (general-cost)\n");
        return str.toString();
    }

    @Override
    public TemporalPlan parse(String contents) throws IllegalArgumentException {
        Set<TemporalPlanAction> actions = new HashSet<>();

        PlanParser parser = new PlanParser(new CommonTokenStream(new PlanLexer(new ANTLRInputStream(contents))));
        ErrorDetectionListener listener = new ErrorDetectionListener();
        parser.addErrorListener(listener);
        PlanParser.PlanContext context = parser.plan();
        if (listener.isFail()) {
            throw new IllegalArgumentException("Plan failed to parse.");
        }
        PlanParser.TemporalPlanContext tempContext = context.temporalPlan();
        for (PlanParser.TemporalActionContext action : tempContext.temporalAction()) {
            double duration = Double.parseDouble(action.duration().NUMBER().getText());
            if ((duration != Math.floor(duration)) || Double.isInfinite(duration)) {
                throw new IllegalArgumentException("We do not support non-integer durations."
                        + " There's no need for them in most Transport domains.");
            }
            double time = Double.parseDouble(action.time().NUMBER().getText());
            if ((time != Math.floor(time)) || Double.isInfinite(time)) {
                throw new IllegalArgumentException("We do not support non-integer times. There's no need for them in"
                        + " most Transport domains.");
            }

            String actionString = String.format("(%s)",
                    String.join(" ", action.sequentialAction().action().NAME().getText(),
                            action.sequentialAction().object().stream().map(o -> o.NAME().getText())
                                    .collect(Collectors.joining(" "))));
            Action actionObj = SequentialPlanIO.parsePlanAction(domain, problem, actionString);
            int timeInt = (int) time;
            int durationInt = (int) duration;
            TemporalPlanAction temporalPlanAction = new TemporalPlanAction(actionObj, timeInt, timeInt + durationInt);
            actions.add(temporalPlanAction);
        }

        return new TemporalPlan(actions);
    }


}

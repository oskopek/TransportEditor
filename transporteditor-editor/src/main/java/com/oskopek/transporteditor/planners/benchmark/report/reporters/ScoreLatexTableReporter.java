package com.oskopek.transporteditor.planners.benchmark.report.reporters;

/**
 * Generates a {@link LatexProblemPlannerReporter} table with scores as elements.
 */
public class ScoreLatexTableReporter extends LatexProblemPlannerReporter {

    /**
     * Default constructor.
     */
    public ScoreLatexTableReporter() {
        super(r -> r.getResults().getScore());
    }

    @Override
    public String getFileName() {
        return "score_table.tex";
    }
}

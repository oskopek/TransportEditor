package com.oskopek.transport.report.reporters;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Generates a {@link LatexProblemPlannerReporter} table with scores as elements.
 */
public class ScoreLatexTableReporter extends LatexProblemPlannerReporter {

    private static final DecimalFormat scoreFormat = new DecimalFormat();

    static {
        scoreFormat.setMinimumFractionDigits(0);
        scoreFormat.setMaximumFractionDigits(2);
        scoreFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
    }

    /**
     * Default constructor.
     */
    public ScoreLatexTableReporter() {
        super(r -> r.getResults().getScore() == null ? null : scoreFormat.format(r.getResults().getScore()));
    }

    @Override
    public String getFileName() {
        return "score_table.tex";
    }
}

package com.oskopek.transport.report.reporters;

/**
 * Generates a {@link LatexProblemPlannerReporter} table with quality as elements.
 */
public class QualityLatexTableReporter extends LatexProblemPlannerReporter {

    /**
     * Default constructor.
     */
    public QualityLatexTableReporter() {
        super(r -> decimalFormat.format(r.getResults().getQuality()));
    }

    @Override
    public String getFileName() {
        return "quality_table.tex";
    }
}

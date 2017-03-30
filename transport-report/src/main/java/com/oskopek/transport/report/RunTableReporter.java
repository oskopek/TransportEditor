package com.oskopek.transport.report;

import com.google.common.collect.ArrayTable;
import com.oskopek.transport.benchmark.data.BenchmarkRun;
import com.oskopek.transport.model.planner.Planner;
import com.oskopek.transport.model.problem.Problem;

/**
 * Represents a benchmark run table report generator.
 * Only used when generating results directly after a benchmark run, not when doing so from the result file.
 */
public interface RunTableReporter {

    /**
     * Generate this report from the given result data.
     *
     * @param runTable the benchmark run table
     * @return the generated report, as if contents of a file
     */
    String generateReport(ArrayTable<Problem, Planner, BenchmarkRun> runTable);

    /**
     * The local filename of the to-be-generated report.
     *
     * @return the proposed filename
     */
    String getFileName();

}

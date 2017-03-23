package com.oskopek.transporteditor.planners.benchmark.report;

import com.oskopek.transporteditor.planners.benchmark.data.BenchmarkResults;

import java.nio.file.Path;

/**
 * Represents a benchmark result report generator.
 * Any implementation of this interface will get picked up automatically
 * by {@link ReportGenerator#main(String[])}
 * and used to generate a report
 * when {@link ReportGenerator#generate(Path)} is called.
 */
public interface Reporter {

    /**
     * Generate this report from the given result data.
     *
     * @param results the results
     * @return the generated report, as if contents of a file
     */
    String generateReport(BenchmarkResults results);

    /**
     * The local filename of the to-be-generated report.
     *
     * @return the proposed filename
     */
    String getFileName();

}

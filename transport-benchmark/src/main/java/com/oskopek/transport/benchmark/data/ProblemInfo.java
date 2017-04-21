package com.oskopek.transport.benchmark.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Supporting information for problem files.
 */
public class ProblemInfo {

    private final String filePath;
    private final Double bestScore;
    private final transient String fileContents;

    /**
     * Empty constructor for Jackson.
     */
    @JsonCreator
    private ProblemInfo() {
        this(null, null, null);
    }

    /**
     * Default constructor.
     *
     * @param filePath the file path
     * @param bestScore the best score
     * @param fileContents the file contents
     */
    public ProblemInfo(String filePath, Double bestScore, String fileContents) {
        this.filePath = filePath;
        this.bestScore = bestScore;
        this.fileContents = fileContents;
    }

    /**
     * Get the problem file path.
     *
     * @return the file path
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Get the best score.
     *
     * @return the best score
     */
    public Double getBestScore() {
        return bestScore;
    }

    /**
     * Get the file contents. De/serialized manually.
     *
     * @return the file contents, may be null
     */
    @JsonIgnore
    public String getFileContents() {
        return fileContents;
    }

    /**
     * Update the file contents, creating a new instance (immutably).
     *
     * @param fileContents the new file contents
     * @return a new info instance
     */
    public ProblemInfo updateFileContents(String fileContents) {
        return new ProblemInfo(filePath, bestScore, fileContents);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProblemInfo)) {
            return false;
        }
        ProblemInfo that = (ProblemInfo) o;
        return new EqualsBuilder().append(filePath, that.filePath)
                .append(bestScore, that.bestScore).append(fileContents, that.fileContents)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(filePath)
                .append(bestScore).append(fileContents).toHashCode();
    }

    @Override
    public String toString() {
        return "ProblemInfo{" + filePath + '}';
    }
}

package com.github.kevinjdolan.intervaltree;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * The Interval class maintains an interval with some associated data.
 *
 * @param <Type> The type of data being stored
 * @author Kevin Dolan
 */
public class Interval<Type> implements Comparable<Interval<Type>> {

    private final long start;
    private final long end;
    private final Type data;

    /**
     * Create an interval with the given parameters.
     *
     * @param start the start time
     * @param end the end time
     */
    public Interval(long start, long end) {
        this(start, end, null);
    }

    /**
     * Create an interval with the given parameters.
     *
     * @param start the start time
     * @param end the end time
     * @param data the associated data
     */
    public Interval(long start, long end, Type data) {
        if (start > end) {
            throw new IllegalArgumentException("Interval start cannot be later than end.");
        }
        this.start = start;
        this.end = end;
        this.data = data;
    }

    /**
     * Get the start time.
     *
     * @return the start time
     */
    public long getStart() {
        return start;
    }

    /**
     * Get the end time.
     *
     * @return the end time
     */
    public long getEnd() {
        return end;
    }

    /**
     * Get the stored data.
     *
     * @return the stored data
     */
    public Type getData() {
        return data;
    }

    /**
     * Verify that the {@code time} is in the interval. Inclusive from the left, exclusive from the right.
     * Example:
     * <ul>
     * <li>{@code contains(start) == true}</li>
     * <li>{@code contains(end) == false}</li>
     * </ul>
     *
     * @param time the time
     * @return true iff time is in [start, end)
     */
    public boolean contains(long time) {
        return time < end && time >= start;
    }

    /**
     * Verify that the {@code time} is outside the interval.
     * Overlapping border times are not considered as intersections.
     * Example:
     * <ul>
     * <li>{@code Interval(0, 2).intersects(Interval(1, 3)) == true}</li>
     * <li>{@code Interval(0, 2).intersects(Interval(2, 3)) == false}</li>
     * <li>{@code Interval(0, 2).intersects(Interval(-1, 0)) == false}</li>
     * </ul>
     *
     * @param other the other interval
     * @return true iff {@code |[start, end] intersection [other.start, other.end]| > 1}
     */
    public boolean intersects(Interval<?> other) {
        return other.getEnd() > start && other.getStart() < end;
    }

    /**
     * Return -1 if this interval's start time is less than the other, 1 if greater.
     * In the event of a tie, -1 if this interval's end time is less than the other, 1 if greater, 0 if same.
     *
     * @param other the interval to compare to
     * @return the comparison of the interval's start and end times
     */
    @Override
    public int compareTo(Interval<Type> other) {
        if (start < other.getStart()) {
            return -1;
        } else if (start > other.getStart()) {
            return 1;
        } else if (end < other.getEnd()) {
            return -1;
        } else if (end > other.getEnd()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Interval)) {
            return false;
        }
        Interval<?> interval = (Interval<?>) o;
        return new EqualsBuilder().append(getStart(), interval.getStart()).append(getEnd(), interval.getEnd()).append(
                getData(), interval.getData()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getStart()).append(getEnd()).append(getData()).toHashCode();
    }
}

package com.github.kevinjdolan.intervaltree;

/**
 * The Interval class maintains an interval with some associated data
 *
 * @param <Type> The type of data being stored
 * @author Kevin Dolan
 */
public class Interval<Type> implements Comparable<Interval<Type>> {

    private long start;
    private long end;
    private Type data;

    public Interval(long start, long end, Type data) {
        if (start > end) {
            throw new IllegalArgumentException("Interval start cannot be later than end.");
        }
        this.start = start;
        this.end = end;
        this.data = data;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public Type getData() {
        return data;
    }

    public void setData(Type data) {
        this.data = data;
    }

    /**
     * @param time
     */
    public boolean contains(long time) {
        return time < end && time >= start;
    }

    /**
     * @param other
     */
    public boolean intersects(Interval<?> other) {
        return other.getEnd() > start && other.getStart() < end;
    }

    /**
     * Return -1 if this interval's start time is less than the other, 1 if greater
     * In the event of a tie, -1 if this interval's end time is less than the other, 1 if greater, 0 if same
     *
     * @param other
     * @return 1 or -1
     */
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

}

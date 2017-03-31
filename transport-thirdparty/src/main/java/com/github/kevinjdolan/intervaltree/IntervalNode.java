package com.github.kevinjdolan.intervaltree;

import java.util.*;
import java.util.Map.Entry;

/**
 * The Node class contains the interval tree information for one single node.
 *
 * @param <Type> the type of data contained in the individual intervals
 * @author Kevin Dolan
 */
public class IntervalNode<Type> {

    private final SortedMap<Interval<Type>, List<Interval<Type>>> intervals;
    private final long center;
    private final IntervalNode<Type> leftNode;
    private final IntervalNode<Type> rightNode;

    /**
     * The default constructor.
     *
     * @param intervals the intervals
     * @param center the center
     * @param leftNode the left node
     * @param rightNode the right node
     */
    protected IntervalNode(SortedMap<Interval<Type>, List<Interval<Type>>> intervals, long center,
            IntervalNode<Type> leftNode, IntervalNode<Type> rightNode) {
        this.intervals = intervals;
        this.center = center;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }

    /**
     * Create an empty interval node.
     *
     * @param <Type_> the type of data contained in the individual intervals
     * @return a new empty interval node
     */
    public static <Type_> IntervalNode<Type_> empty() {
        return new IntervalNode<>(new TreeMap<>(), 0, null, null);
    }

    /**
     * Create an interval node containing the given intervals.
     *
     * @param intervalList the list of intervals to store
     * @param <Type_> the type of data contained in the individual intervals
     * @return a new interval node containing the given intervals
     */
    public static <Type_> IntervalNode<Type_> of(Collection<Interval<Type_>> intervalList) {
        SortedMap<Interval<Type_>, List<Interval<Type_>>> intervals = new TreeMap<>();
        if (intervalList.isEmpty()) {
            return empty();
        }
        SortedSet<Long> endpoints = new TreeSet<>();

        for (Interval<Type_> interval : intervalList) {
            endpoints.add(interval.getStart());
            endpoints.add(interval.getEnd());
        }
        long median = getMedian(endpoints);

        List<Interval<Type_>> left = new ArrayList<>();
        List<Interval<Type_>> right = new ArrayList<>();
        for (Interval<Type_> interval : intervalList) {
            if (interval.getEnd() < median) {
                left.add(interval);
            } else if (interval.getStart() > median) {
                right.add(interval);
            } else {
                List<Interval<Type_>> posting = intervals.computeIfAbsent(interval, k -> new ArrayList<>());
                posting.add(interval);
            }
        }
        IntervalNode<Type_> leftNode = null;
        if (!left.isEmpty()) {
            leftNode = IntervalNode.of(left);
        }
        IntervalNode<Type_> rightNode = null;
        if (!right.isEmpty()) {
            rightNode = IntervalNode.of(right);
        }
        return new IntervalNode<>(intervals, median, leftNode, rightNode);
    }

    /**
     * Perform a stabbing query on the node.
     *
     * @param time the time to query at
     * @return the set of intervals which contain the time
     * @see Interval#contains(long)
     */
    public Set<Interval<Type>> stab(long time) {
        Set<Interval<Type>> result = new HashSet<>();

        for (Entry<Interval<Type>, List<Interval<Type>>> entry : intervals.entrySet()) {
            if (entry.getKey().contains(time)) {
                result.addAll(entry.getValue());
            } else if (entry.getKey().getStart() > time) {
                break;
            }
        }

        if (time < center && leftNode != null) {
            result.addAll(leftNode.stab(time));
        } else if (time > center && rightNode != null) {
            result.addAll(rightNode.stab(time));
        }
        return result;
    }

    /**
     * Perform an interval intersection query on the node.
     *
     * @param target the interval to intersect
     * @return the set of intervals that intersect the given interval
     * @see Interval#intersects(Interval)
     */
    public Set<Interval<Type>> query(Interval<?> target) {
        Set<Interval<Type>> result = new HashSet<>();

        for (Entry<Interval<Type>, List<Interval<Type>>> entry : intervals.entrySet()) {
            if (entry.getKey().intersects(target)) {
                result.addAll(entry.getValue());
            } else if (entry.getKey().getStart() > target.getEnd()) {
                break;
            }
        }

        if (target.getStart() < center && leftNode != null) {
            result.addAll(leftNode.query(target));
        }
        if (target.getEnd() > center && rightNode != null) {
            result.addAll(rightNode.query(target));
        }
        return result;
    }

    /**
     * Get the left sub-node.
     *
     * @return the left sub-node
     */
    public IntervalNode<Type> getLeft() {
        return leftNode;
    }

    /**
     * Get the right sub-node.
     *
     * @return the right sub-node
     */
    public IntervalNode<Type> getRight() {
        return rightNode;
    }

    /**
     * Calculates the median of a sorted set.
     *
     * @param set the set to look on
     * @return the median, the value in the middle of a sorted vector of numbers
     * @throws IllegalArgumentException if the set is empty
     */
    private static long getMedian(SortedSet<Long> set) {
        int i = 0;
        int middle = set.size() / 2;
        for (Long point : set) {
            if (i == middle) {
                return point;
            }
            i++;
        }
        throw new IllegalArgumentException("Cannot calculate median of empty set.");
    }

    /**
     * Represent the subtree as a string (for visualization).
     *
     * @param level the level of indentation (number of tabs)
     * @return the text representation of the subtree
     */
    public String toText(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append('\t');
        }
        sb.append(toText()).append('\n');
        if (getLeft() != null) {
            sb.append(getLeft().toText(level + 1));
        }
        if (getRight() != null) {
            sb.append(getRight().toText(level + 1));
        }
        return sb.toString();
    }

    /**
     * Represent the current node as a string (for visualization).
     *
     * @return the text representation of the node
     */
    private String toText() {
        StringBuilder sb = new StringBuilder();
        sb.append(center).append(": ");
        for (Entry<Interval<Type>, List<Interval<Type>>> entry : intervals.entrySet()) {
            sb.append('[').append(entry.getKey().getStart()).append(',').append(entry.getKey().getEnd()).append("]:{");
            for (Interval<Type> interval : entry.getValue()) {
                sb.append('(').append(interval.getStart()).append(',').append(interval.getEnd()).append(',').append(
                        interval.getData()).append(')');
            }
            sb.append("} ");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toText();
    }

}

package com.github.kevinjdolan.intervaltree;

import java.util.*;
import java.util.Map.Entry;

/**
 * The Node class contains the interval tree information for one single node
 *
 * @author Kevin Dolan
 */
public class IntervalNode<Type> {

    private SortedMap<Interval<Type>, List<Interval<Type>>> intervals;
    private long center;
    private IntervalNode<Type> leftNode;
    private IntervalNode<Type> rightNode;

    public IntervalNode() {
        intervals = new TreeMap<>();
        center = 0;
        leftNode = null;
        rightNode = null;
    }

    public IntervalNode(List<Interval<Type>> intervalList) {

        intervals = new TreeMap<>();

        if (intervalList.isEmpty()) {
            return;
        }

        SortedSet<Long> endpoints = new TreeSet<>();

        for (Interval<Type> interval : intervalList) {
            endpoints.add(interval.getStart());
            endpoints.add(interval.getEnd());
        }

        long median = getMedian(endpoints);
        center = median;

        List<Interval<Type>> left = new ArrayList<>();
        List<Interval<Type>> right = new ArrayList<>();

        for (Interval<Type> interval : intervalList) {
            if (interval.getEnd() < median) {
                left.add(interval);
            } else if (interval.getStart() > median) {
                right.add(interval);
            } else {
                List<Interval<Type>> posting = intervals.get(interval);
                if (posting == null) {
                    posting = new ArrayList<>();
                    intervals.put(interval, posting);
                }
                posting.add(interval);
            }
        }

        if (left.size() > 0) {
            leftNode = new IntervalNode<>(left);
        }
        if (right.size() > 0) {
            rightNode = new IntervalNode<>(right);
        }
    }

    /**
     * Perform a stabbing query on the node
     *
     * @param time the time to query at
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
     * Perform an interval intersection query on the node
     *
     * @param target the interval to intersect
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

    public long getCenter() {
        return center;
    }

    public void setCenter(long center) {
        this.center = center;
    }

    public IntervalNode<Type> getLeft() {
        return leftNode;
    }

    public void setLeft(IntervalNode<Type> left) {
        this.leftNode = left;
    }

    public IntervalNode<Type> getRight() {
        return rightNode;
    }

    public void setRight(IntervalNode<Type> right) {
        this.rightNode = right;
    }

    /**
     * @param set the set to look on
     */
    private long getMedian(SortedSet<Long> set) {
        if (set.isEmpty()) {
            throw new IllegalArgumentException("Cannot calculate median of empty set.");
        }
        int i = 0;
        int middle = set.size() / 2;
        for (Long point : set) {
            if (i == middle) {
                return point;
            }
            i++;
        }
        throw new IllegalStateException("Dead code.");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(center).append(": ");
        for (Entry<Interval<Type>, List<Interval<Type>>> entry : intervals.entrySet()) {
            sb.append("[").append(entry.getKey().getStart()).append(",").append(entry.getKey().getEnd()).append("]:{");
            for (Interval<Type> interval : entry.getValue()) {
                sb.append("(").append(interval.getStart()).append(",").append(interval.getEnd()).append(",").append(
                        interval.getData()).append(")");
            }
            sb.append("} ");
        }
        return sb.toString();
    }

}

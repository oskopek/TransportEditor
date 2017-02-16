package com.github.kevinjdolan.intervaltree;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An Interval Tree is essentially a map from intervals to objects, which
 * can be queried for all data associated with a particular interval of
 * time.
 *
 * @param <Type> the type of data contained in the individual intervals
 * @author Kevin Dolan
 */
public class IntervalTree<Type> {

    private transient IntervalNode<Type> head;
    private final List<Interval<Type>> intervalList;
    private transient boolean inSync;
    private transient int size;

    /**
     * Instantiate a new interval tree with no intervals.
     */
    protected IntervalTree() {
        this(Collections.emptyList());
    }

    /**
     * Instantiate and build an interval tree with a preset list of intervals.
     *
     * @param intervalList the list of intervals to use
     */
    protected IntervalTree(List<Interval<Type>> intervalList) {
        this.head = IntervalNode.of(intervalList);
        this.intervalList = new ArrayList<>(intervalList);
        this.inSync = true;
        this.size = intervalList.size();
    }

    /**
     * Instantiate a new interval tree with no intervals.
     *
     * @param <Type_> the type of data contained in the individual intervals
     * @return an empty interval tree
     */
    public static <Type_> IntervalTree<Type_> empty() {
        return new IntervalTree<>();
    }

    /**
     * Instantiate and build an interval tree with a preset list of intervals.
     *
     * @param intervalList the list of intervals to use
     * @param <Type_> the type of data contained in the individual intervals
     * @return a filled in interval tree
     */
    public static <Type_> IntervalTree<Type_> of(List<Interval<Type_>> intervalList) {
        return new IntervalTree<>(intervalList);
    }

    /**
     * Perform a stabbing query, returning the associated data.
     * Will rebuild the tree if out of sync.
     *
     * @param time the time to stab
     * @return a set of data contained in the intervals that contain the given time
     * @see Interval#contains(long)
     */
    public Set<Type> get(long time) {
        return getIntervals(time).stream().map(Interval::getData).collect(Collectors.toSet());
    }

    /**
     * Perform a stabbing query, returning the interval objects.
     * Will rebuild the tree if out of sync.
     *
     * @param time the time to stab
     * @return a set of intervals contained that contain the given time
     * @see Interval#contains(long)
     */
    public Set<Interval<Type>> getIntervals(long time) {
        build();
        return head.stab(time);
    }

    /**
     * Perform an interval query, returning the associated data.
     * Will rebuild the tree if out of sync.
     *
     * @param start the start of the interval to check
     * @param end the end of the interval to check
     * @return a set of data contained in the intervals that intersect the given [start, end] interval
     * @see Interval#intersects(Interval)
     */
    public Set<Type> get(long start, long end) {
        return getIntervals(start, end).stream().map(Interval::getData).collect(Collectors.toSet());
    }

    /**
     * Perform an interval query, returning the interval objects.
     * Will rebuild the tree if out of sync.
     *
     * @param start the start of the interval to check
     * @param end the end of the interval to check
     * @return a set of intervals that intersect the given [start, end] interval
     * @see Interval#intersects(Interval)
     */
    public Set<Interval<Type>> getIntervals(long start, long end) {
        build();
        return head.query(new Interval<Type>(start, end, null));
    }

    /**
     * Add an interval object to the interval tree's list.
     * Will not rebuild the tree until the next query or call to build.
     *
     * @param interval the interval object to add
     */
    public void addInterval(Interval<Type> interval) {
        intervalList.add(interval);
        inSync = false;
    }

    /**
     * Add an interval object to the interval tree's list.
     * Will not rebuild the tree until the next query or call to build.
     *
     * @param begin the beginning of the interval
     * @param end the end of the interval
     * @param data the data to associate
     */
    public void addInterval(long begin, long end, Type data) {
        intervalList.add(new Interval<>(begin, end, data));
        inSync = false;
    }

    /**
     * Determine whether this interval tree is currently a reflection of all intervals in the interval list.
     *
     * @return true iff no changes have been made since the last build
     */
    public boolean inSync() {
        return inSync;
    }

    /**
     * Build the interval tree to reflect the list of intervals.
     * Will not run if this is currently in sync.
     */
    public void build() {
        if (!inSync) {
            head = IntervalNode.of(intervalList);
            inSync = true;
            size = intervalList.size();
        }
    }

    /**
     * The number of intervals represented by this tree.
     *
     * @return the number of entries in the currently built interval tree
     * @see #listSize()
     */
    public int currentSize() {
        return size;
    }

    /**
     * The number of intervals in the internal list.
     *
     * @return the number of entries in the interval list, equal to .size() if inSync()
     * @see #currentSize()
     */
    public int listSize() {
        return intervalList.size();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(intervalList).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IntervalTree)) {
            return false;
        }
        IntervalTree<?> that = (IntervalTree<?>) o;
        return new EqualsBuilder().append(intervalList, that.intervalList).isEquals();
    }

    @Override
    public String toString() {
        return "IntervalTree[" + (head == null ? "" : head.toText(0)) + "]";
    }
}

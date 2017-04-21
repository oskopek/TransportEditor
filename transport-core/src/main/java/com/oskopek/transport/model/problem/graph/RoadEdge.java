package com.oskopek.transport.model.problem.graph;

import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Road;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a road along with the two locations between which it spans. Is not used internally
 * in the graph, serves as public API that is calculated based on the internal representation.
 */
public final class RoadEdge {

    private final Road road;
    private final Location from;
    private final Location to;

    /**
     * Private default constructor.
     *
     * @param road the road
     * @param from the from location
     * @param to the to location
     */
    private RoadEdge(Road road, Location from, Location to) {
        this.road = road;
        this.from = from;
        this.to = to;
    }

    /**
     * Build a road edge from the arguments.
     *
     * @param road the road
     * @param from the from location
     * @param to the to location
     * @return the built road edge
     */
    public static RoadEdge of(Road road, Location from, Location to) {
        return new RoadEdge(road, from, to);
    }

    /**
     * Get the road.
     *
     * @return the road
     */
    public Road getRoad() {
        return road;
    }

    /**
     * Get the from location.
     *
     * @return the from location
     */
    public Location getFrom() {
        return from;
    }

    /**
     * Get the to location.
     *
     * @return the to location
     */
    public Location getTo() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RoadEdge)) {
            return false;
        }
        RoadEdge roadEdge = (RoadEdge) o;
        return new EqualsBuilder().append(road, roadEdge.road).append(from, roadEdge.from)
                .append(to, roadEdge.to).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(road).append(from)
                .append(to).toHashCode();
    }

    @Override
    public String toString() {
        return "RoadEdge{" + "road=" + road + ", from=" + from + ", to=" + to + '}';
    }

}

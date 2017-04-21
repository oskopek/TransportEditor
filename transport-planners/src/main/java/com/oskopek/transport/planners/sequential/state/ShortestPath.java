package com.oskopek.transport.planners.sequential.state;

import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.graph.RoadEdge;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public final class ShortestPath {

    private final List<RoadEdge> roads;
    private final Integer distance;

    public ShortestPath(List<RoadEdge> roads, Integer distance) {
        this.roads = roads;
        this.distance = distance;
    }

    public Location lastLocation() {
        if (roads.isEmpty()) {
            return null;
        }
        return roads.get(roads.size() - 1).getTo();
    }

    /**
     * Get the roads.
     *
     * @return the roads
     */
    public List<RoadEdge> getRoads() {
        return roads;
    }

    /**
     * Get the distance.
     *
     * @return the distance
     */
    public Integer getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("distance", distance).append("roads", roads).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShortestPath)) {
            return false;
        }
        ShortestPath that = (ShortestPath) o;
        return new EqualsBuilder().append(getRoads(), that.getRoads()).append(getDistance(), that.getDistance())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getRoads()).append(getDistance()).toHashCode();
    }

}

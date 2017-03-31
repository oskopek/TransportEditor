package com.oskopek.transport.model.problem.graph;

import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Road;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.stream.Stream;

/**
 * Wrapper interface around a GraphStream oriented {@link Graph} type. Represents an <strong>oriented</strong>
 * graph. Currently supports only a single oriented edge between two ordered nodes
 * (i.e. a total of two edges between a set of two nodes).
 */
public interface RoadGraph extends Graph {

    /**
     * Copy method.
     *
     * @return a new instance of the graph
     */
    RoadGraph copy();

    /**
     * Add a new node to the graph representing the location.
     *
     * @param location the location to add
     * @param <T> the type of the node object
     * @return the created node
     */
    <T extends Node> T addLocation(Location location);

    /**
     * Removes the location and associated node from the graph. Does checking and removes any
     * associated roads too.
     *
     * @param location the location to remove
     */
    void removeLocation(Location location);

    /**
     * Removes all the locations.
     *
     * @param locations the locations to remove
     * @see #removeLocation(Location)
     */
    void removeLocations(Iterable<? extends Location> locations);

    /**
     * Move the given location to new X and Y coordinates.
     *
     * @param name the name of the location to move
     * @param newX the new X coordinate
     * @param newY the new Y coordinate
     * @return the updated location
     */
    Location moveLocation(String name, int newX, int newY);

    /**
     * Update the location's petrol station property. Creates a new location instance and replaces
     * it in the graph.
     *
     * @param locationName the location's name
     * @param hasPetrolStation the new petrol station value
     * @return the updated location
     */
    Location setPetrolStation(String locationName, boolean hasPetrolStation);

    /**
     * Get the location of the given name.
     *
     * @param name the name of the location
     * @return the location, or null if there is no such location
     */
    Location getLocation(String name);

    /**
     * Get a stream of all the locations in the graph.
     *
     * @return a stream of all the locations
     */
    Stream<Location> getAllLocations();

    /**
     * Get a stream of all the road edges in the graph.
     *
     * @return a stream of all the road edges
     */
    Stream<RoadEdge> getAllRoads();

    /**
     * Add a new edge to the graph representing the road.
     *
     * @param road the road to add
     * @param from the source location
     * @param to the destination location
     * @param <T> the type of the edge object
     * @param <R> the type of the road object
     * @return the created edge
     */
    <T extends Edge, R extends Road> T addRoad(R road, Location from, Location to);

    /**
     * Put (add with override) a new edge to the graph representing the road.
     *
     * @param road the road to add
     * @param from the source location
     * @param to the destination location
     * @param <T> the type of the edge object
     * @param <R> the type of the road object
     * @return the created edge
     */
    <T extends Edge, R extends Road> T putRoad(R road, Location from, Location to);

    /**
     * Get stream of all the roads going from the first to the second location.
     * In the current implementation, should return the same
     * as;@link #getShortestRoadBetween(Location, Location)}.
     *
     * @param l1 the from location
     * @param l2 the to location
     * @return a stream of all the roads from {@code l1 -> l2}
     */
    Stream<Road> getAllRoadsBetween(Location l1, Location l2);

    /**
     * Only a single road is currently permitted between two nodes in practice. This method returns that
     * road, or null if there is none.
     *
     * @param l1 the from location
     * @param l2 the to location
     * @return the shorted road between l1 and l2, or null if no such road exists
     */
    Road getShortestRoadBetween(Location l1, Location l2);

    /**
     * Removes all nodes going from l1 to l2.
     *
     * @param l1 the from location
     * @param l2 the to location
     * @see #getAllRoadsBetween(Location, Location)
     */
    void removeAllRoadsBetween(Location l1, Location l2);

    /**
     * Get the road given by the name.
     *
     * @param name the name of the road
     * @return the road, null if a road like that doesn't exist
     */
    Road getRoad(String name);

    /**
     * Get the road edge given by the name of the road.
     *
     * @param name the name of the road
     * @return the road edge, null if a road like that doesn't exist
     */
    RoadEdge getRoadEdge(String name);

    /**
     * Remove the road given by the name. Also removes its edge sprite and edge.
     *
     * @param name the name of the road
     */
    void removeRoad(String name);

    /**
     * Remove all the roads by their names.
     *
     * @param names the names of roads to remove
     * @see #removeRoad(String)
     */
    void removeRoads(Iterable<? extends String> names);

}

/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.problem;

import org.apache.commons.io.IOUtils;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.view.Viewer;

import java.io.IOException;
import java.util.List;

/**
 * Wrapper interface around a GraphStream graph type.
 */
public class RoadGraph extends MultiGraph implements Graph {

    public RoadGraph(String id) {
        super(id);

        addDefaultStyling();
    }

    private RoadGraph(String id, boolean strictChecking, boolean autoCreate) {
        super(id, strictChecking, autoCreate);
    }

    private RoadGraph(String id, boolean strictChecking, boolean autoCreate, int initialNodeCapacity,
            int initialEdgeCapacity) {
        super(id, strictChecking, autoCreate, initialNodeCapacity, initialEdgeCapacity);
    }

    private void addDefaultStyling() {
        String style;
        try {
            style = String.join("\n",
                    (List<String>) IOUtils.readLines(getClass().getResourceAsStream("stylesheet.css"), "UTF-8"));
        } catch (IOException e) {
            throw new IllegalStateException("Could not load graph stylesheet.", e);
        }
        this.addAttribute("ui.stylesheet", style);
        //        this.addAttribute("ui.quality");
        this.addAttribute("ui.antialias");
    }

    public <T extends Node> T addLocation(Location location) {
        addAttribute(location.getName(), location);
        T node = addNode(location.getName());
        node.addAttribute("ui.label", location.getName());
        return node;
    }

    public Location getLocation(String name) {
        return getAttribute(name);
    }

    public <T extends Edge, R extends Road> T addRoad(R road, Location from, Location to) {
        addAttribute(road.getName(), road);
        return addEdge(road.getName(), from.getName(), to.getName(), true);
    }

    public Road getRoad(String name) {
        return getAttribute(name);
    }

    @Override
    public Viewer display() {
        return display(true);
    }

    @Override
    public Viewer display(boolean autoLayout) {
        Viewer viewer = new Viewer(this, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        if (autoLayout) {
            Layout layout = Layouts.newLayoutAlgorithm();
            viewer.enableAutoLayout(layout);
        }
        return viewer;
    }
}

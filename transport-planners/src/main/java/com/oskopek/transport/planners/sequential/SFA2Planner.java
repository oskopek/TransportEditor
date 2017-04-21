package com.oskopek.transport.planners.sequential;

import com.google.common.collect.ArrayTable;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.Vehicle;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.algorithm.Kruskal;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.*;
import java.util.stream.Collectors;

public class SFA2Planner extends ForwardAstarPlanner {

    public SFA2Planner() {
        setName(SFA2Planner.class.getSimpleName());
    }

    @Override
    protected Integer calculateHeuristic(ImmutablePlanState state, ArrayTable<String, String, Integer> distanceMatrix,
            Collection<Package> unfinishedPackages) {
        int heuristic = 0;

        Graph T = mst(state.getProblem().getRoadGraph());

        Map<Location, Set<Package>> targetMap = new HashMap<>();
        Map<Package, Location> packageMap = new HashMap<>();
        for (Vehicle vehicle : state.getProblem().getAllVehicles()) {
            for (Package pkg : vehicle.getPackageList()) {
                packageMap.put(pkg, vehicle.getLocation());
                heuristic += 1; // drop
            }
        }
        for (Package pkg : unfinishedPackages) {
            targetMap.computeIfAbsent(pkg.getTarget(), n -> new HashSet<>()).add(pkg);
            if (pkg.getLocation() != null) {
                packageMap.put(pkg, pkg.getLocation());
                heuristic += 2; // pickup and drop
            }
        }

        for (Map.Entry<Location, Set<Package>> entry : targetMap.entrySet()) {
            Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "weight");
            dijkstra.init(T);
            dijkstra.setSource(T.getNode(entry.getKey().getName()));
            dijkstra.compute();
            for (Package pkg : entry.getValue()) {
                String loc = packageMap.get(pkg).getName();
                Path path = dijkstra.getPath(T.getNode(loc));
                for (Edge e : path.getEachEdge()) {
                    e.setAttribute("marked");
                }
            }
        }

        for (Edge e : T.getEdgeSet()) {
            if (e.hasAttribute("marked")) {
                heuristic += (int) e.getAttribute("weight");
            }
        }

        return heuristic;
    }

    private Graph mst(RoadGraph graph) {
        graph.getAllRoads().forEach(re -> graph.getEdge(re.getRoad().getName())
                .setAttribute("weight", re.getRoad().getLength().getCost()));
        Kruskal kruskal = new Kruskal("weight", "mst", "on", "off");
        kruskal.init(graph);
        kruskal.compute();
        Collection<Edge> edgeSet = graph.getEdgeSet().stream().filter(e -> e.getAttribute("mst").equals("on"))
                .collect(Collectors.toSet());
        Graph T = new SingleGraph("tree");
        graph.getNodeSet().forEach(n -> T.addNode(n.getId()));
        for (Edge e : edgeSet) {
            Edge e2 = T.addEdge(e.getId(), e.getNode0().getIndex(), e.getNode1().getIndex(), false);
            e2.addAttribute("weight", (int) e.getAttribute("weight"));
        }
        return T;
    }

    @Override
    public SFA2Planner copy() {
        return new SFA2Planner();
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof SFA2Planner;
    }

}

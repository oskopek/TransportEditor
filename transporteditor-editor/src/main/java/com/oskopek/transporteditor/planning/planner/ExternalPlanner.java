package com.oskopek.transporteditor.planning.planner;

import com.oskopek.transporteditor.planning.domain.Domain;
import com.oskopek.transporteditor.planning.plan.Plan;
import com.oskopek.transporteditor.planning.problem.Problem;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.nio.file.Path;

public class ExternalPlanner implements Planner {

    private ObjectProperty<Path> path = new SimpleObjectProperty<>();

    public ExternalPlanner(Path path) {
        this.path.setValue(path);
    }

    public Path getPath() {
        return path.get();
    }

    public void setPath(Path path) {
        this.path.set(path);
    }

    public ObjectProperty<Path> pathProperty() {
        return path;
    }

    @Override
    public void startPlanning(Domain domain, Problem problem) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void stopPlanning() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Plan getBestPlan() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}

package com.oskopek.transporteditor.model.problem.builder;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.problem.DefaultRoad;

public class DefaultRoadBuilder<Road_ extends DefaultRoad> extends DefaultActionObjectBuilder<Road_> {

    private ActionCost length;

    public DefaultRoadBuilder() {
        // intentionally empty
    }

    @FieldLocalization(key = "length", priority = 2)
    public ActionCost getLength() {
        return length;
    }

    public void setLength(ActionCost length) {
        this.length = length;
    }

    @Override
    public Road_ build() {
        return (Road_) new DefaultRoad(getName(), getLength());
    }

    @Override
    public void from(Road_ instance) {
        super.from(instance);
        setLength(instance.getLength());
    }
}

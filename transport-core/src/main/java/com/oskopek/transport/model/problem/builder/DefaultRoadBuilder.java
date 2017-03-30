package com.oskopek.transport.model.problem.builder;

import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.problem.DefaultRoad;

/**
 * Action object builder for {@link DefaultRoad}s.
 *
 * @param <Road_> the exact type of the road
 */
public class DefaultRoadBuilder<Road_ extends DefaultRoad> extends DefaultActionObjectBuilder<Road_> {

    private ActionCost length;

    /**
     * Default constructor.
     */
    public DefaultRoadBuilder() {
        // intentionally empty
    }

    /**
     * Get the length.
     *
     * @return the length
     */
    @FieldLocalization(key = "length", priority = 2)
    public ActionCost getLength() {
        return length;
    }

    /**
     * Set the length.
     *
     * @param length the length to set
     */
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

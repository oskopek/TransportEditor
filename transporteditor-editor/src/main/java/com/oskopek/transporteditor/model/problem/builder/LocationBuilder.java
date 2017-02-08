package com.oskopek.transporteditor.model.problem.builder;

import com.oskopek.transporteditor.model.problem.Location;

public class LocationBuilder extends DefaultActionObjectBuilder<Location> {

    private Integer xCoordinate;
    private Integer yCoordinate;

    public LocationBuilder() {
        // intentionally empty
    }

    @FieldLocalization(key = "location.X")
    public Integer getxCoordinate() {
        return xCoordinate;
    }

    public void setxCoordinate(Integer xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    @FieldLocalization(key = "location.Y")
    public Integer getyCoordinate() {
        return yCoordinate;
    }

    public void setyCoordinate(Integer yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    @Override
    public Location build() {
        return new Location(getName(), getxCoordinate(), getyCoordinate());
    }

    public void from(Location location) {
        super.from(location);
        setxCoordinate(location.getxCoordinate());
        setyCoordinate(location.getyCoordinate());
    }
}

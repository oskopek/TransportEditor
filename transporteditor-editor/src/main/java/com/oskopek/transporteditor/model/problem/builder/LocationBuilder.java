package com.oskopek.transporteditor.model.problem.builder;

import com.oskopek.transporteditor.model.problem.Location;

public class LocationBuilder extends DefaultActionObjectBuilder<Location> {

    private Integer xCoordinate;
    private Integer yCoordinate;
    private Boolean petrolStation;

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

    @FieldLocalization(key = "petrolstation", priority = 6)
    public Boolean getPetrolStation() {
        return petrolStation;
    }

    public void setPetrolStation(Boolean petrolStation) {
        this.petrolStation = petrolStation;
    }

    @Override
    public Location build() {
        return new Location(getName(), getxCoordinate(), getyCoordinate(), getPetrolStation());
    }

    @Override
    public void from(Location location) {
        super.from(location);
        setxCoordinate(location.getxCoordinate());
        setyCoordinate(location.getyCoordinate());
        setPetrolStation(location.getPetrolStation());
    }
}

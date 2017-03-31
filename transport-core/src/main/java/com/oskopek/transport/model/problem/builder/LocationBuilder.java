package com.oskopek.transport.model.problem.builder;

import com.oskopek.transport.model.problem.Location;

/**
 * Action object builder for {@link Location}s.
 */
public class LocationBuilder extends DefaultActionObjectBuilder<Location> {

    private Integer xCoordinate;
    private Integer yCoordinate;
    private Boolean petrolStation;

    /**
     * Default constructor.
     */
    public LocationBuilder() {
        // intentionally empty
    }

    /**
     * Get the X coordinate.
     *
     * @return the X coordinate
     */
    @FieldLocalization(key = "location.X")
    public Integer getxCoordinate() {
        return xCoordinate;
    }

    /**
     * Set the X coordinate.
     *
     * @param xCoordinate the X coordinate to set
     */
    public void setxCoordinate(Integer xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    /**
     * Get the Y coordinate.
     *
     * @return the Y coordinate
     */
    @FieldLocalization(key = "location.Y")
    public Integer getyCoordinate() {
        return yCoordinate;
    }

    /**
     * Set the Y coordinate.
     *
     * @param yCoordinate the Y coordinate to set
     */
    public void setyCoordinate(Integer yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    /**
     * Get whether the location has a petrol station.
     *
     * @return true if the location has a petrol station, if not, may be false or null
     */
    @FieldLocalization(key = "petrolstation", priority = 6)
    public Boolean getPetrolStation() {
        return petrolStation;
    }

    /**
     * Set whether the location has a petrol station.
     *
     * @param petrolStation true iff the location has a petrol station
     */
    public void setPetrolStation(Boolean petrolStation) {
        this.petrolStation = petrolStation;
    }

    @Override
    public Location build() {
        return new Location(getName(), xCoordinate, yCoordinate, petrolStation);
    }

    @Override
    public void from(Location location) {
        super.from(location);
        this.xCoordinate = location.getxCoordinate();
        this.yCoordinate = location.getyCoordinate();
        this.petrolStation = location.getPetrolStation();
    }
}

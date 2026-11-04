package com.battleship.domain.model.ship;

import com.battleship.domain.model.coordinate.Coordinate;
import com.battleship.domain.model.coordinate.Orientation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ShipPlacement {
    private final ShipType shipType;
    private final Coordinate startCoordinate;
    private final Orientation orientation;
    private final List<Coordinate> coordinates;

    public ShipPlacement(ShipType shipType, Coordinate startCoordinate, Orientation orientation) {
        this.shipType = Objects.requireNonNull(shipType, "ShipType cannot be null");
        this.startCoordinate = Objects.requireNonNull(startCoordinate, "Start coordinate cannot be null");
        this.orientation = Objects.requireNonNull(orientation, "Orientation cannot be null");
        this.coordinates = Collections.unmodifiableList(startCoordinate.getSequence(orientation, shipType.getSize()));
    }

    public ShipType getShipType() {
        return shipType;
    }

    public Coordinate getStartCoordinate() {
        return startCoordinate;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShipPlacement that = (ShipPlacement) o;
        return shipType == that.shipType &&
                Objects.equals(startCoordinate, that.startCoordinate) &&
                orientation == that.orientation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shipType, startCoordinate, orientation);
    }

    @Override
    public String toString() {
        return shipType.getDisplayName() + " at " + startCoordinate + " (" + orientation + ")";
    }
}

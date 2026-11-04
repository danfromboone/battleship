package com.battleship.domain.model.ship;

import com.battleship.domain.model.coordinate.Coordinate;

import java.util.*;

public class Ship {
    private final ShipType shipType;
    private final List<Coordinate> coordinates;
    private final Set<Coordinate> hitCoordinates = new LinkedHashSet<>();

    public Ship(ShipType shipType, List<Coordinate> coordinates) {
        this.shipType = Objects.requireNonNull(shipType, "ShipType cannot be null");
        Objects.requireNonNull(coordinates, "Coordinates cannot be null");
        if (coordinates.size() != shipType.getSize()) {
            throw new IllegalArgumentException("Coordinate count (" + coordinates.size() + 
                    ") does not match ship size (" + shipType.getSize() + ") for " + shipType);
        }
        this.coordinates = List.copyOf(coordinates);
    }

    public static Ship fromPlacement(ShipPlacement placement) {
        return new Ship(placement.getShipType(), placement.getCoordinates());
    }

    public ShipType getShipType() {
        return shipType;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public Set<Coordinate> getHitCoordinates() {
        return Collections.unmodifiableSet(hitCoordinates);
    }

    public boolean occupies(Coordinate coordinate) {
        return coordinates.contains(coordinate);
    }

    public boolean isHitAt(Coordinate coordinate) {
        return hitCoordinates.contains(coordinate);
    }

    /**
     * Records a hit on this ship at the given coordinate.
     * @return true if this was a new hit, false if already hit at this coordinate
     */
    public boolean recordHit(Coordinate coordinate) {
        if (!occupies(coordinate)) {
            throw new IllegalArgumentException("Coordinate " + coordinate + " does not belong to " + shipType);
        }
        return hitCoordinates.add(coordinate);
    }

    public boolean isSunk() {
        return hitCoordinates.size() == coordinates.size();
    }

    public int getRemainingHealth() {
        return coordinates.size() - hitCoordinates.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ship ship = (Ship) o;
        return shipType == ship.shipType && Objects.equals(coordinates, ship.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shipType, coordinates);
    }

    @Override
    public String toString() {
        return shipType.getDisplayName() + (isSunk() ? " [SUNK]" : " [HP: " + getRemainingHealth() + "/" + coordinates.size() + "]");
    }
}

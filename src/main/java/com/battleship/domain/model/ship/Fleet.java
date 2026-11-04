package com.battleship.domain.model.ship;

import com.battleship.domain.exception.ShipOverlapException;
import com.battleship.domain.model.coordinate.Coordinate;

import java.util.*;

public class Fleet {
    public static final Set<ShipType> REQUIRED_SHIP_TYPES = Set.of(
            ShipType.CARRIER,
            ShipType.BATTLESHIP,
            ShipType.CRUISER,
            ShipType.SUBMARINE,
            ShipType.DESTROYER
    );

    private final Map<ShipType, Ship> ships = new EnumMap<>(ShipType.class);

    public Fleet() {
    }

    public synchronized void addShip(Ship ship) {
        Objects.requireNonNull(ship, "Ship cannot be null");
        if (ships.containsKey(ship.getShipType())) {
            throw new IllegalArgumentException("Fleet already has a " + ship.getShipType().getDisplayName());
        }

        // Check for collision with existing ships
        for (Coordinate newCoord : ship.getCoordinates()) {
            for (Ship existing : ships.values()) {
                if (existing.occupies(newCoord)) {
                    throw new ShipOverlapException(
                            "Cannot place " + ship.getShipType().getDisplayName() + 
                            " at " + newCoord + ": overlaps with " + existing.getShipType().getDisplayName()
                    );
                }
            }
        }

        ships.put(ship.getShipType(), ship);
    }

    public synchronized Optional<Ship> findShipAt(Coordinate coordinate) {
        for (Ship ship : ships.values()) {
            if (ship.occupies(coordinate)) {
                return Optional.of(ship);
            }
        }
        return Optional.empty();
    }

    public synchronized Optional<Ship> getShip(ShipType type) {
        return Optional.ofNullable(ships.get(type));
    }

    public synchronized boolean hasShip(ShipType type) {
        return ships.containsKey(type);
    }

    public synchronized boolean isComplete() {
        return ships.keySet().containsAll(REQUIRED_SHIP_TYPES);
    }

    public synchronized boolean isAllSunk() {
        if (!isComplete()) {
            return false;
        }
        for (Ship ship : ships.values()) {
            if (!ship.isSunk()) {
                return false;
            }
        }
        return true;
    }

    public synchronized List<Ship> getShips() {
        return new ArrayList<>(ships.values());
    }

    public synchronized int getAfloatCount() {
        int count = 0;
        for (Ship ship : ships.values()) {
            if (!ship.isSunk()) {
                count++;
            }
        }
        return count;
    }

    public synchronized int getSunkCount() {
        int count = 0;
        for (Ship ship : ships.values()) {
            if (ship.isSunk()) {
                count++;
            }
        }
        return count;
    }

    public synchronized void clear() {
        ships.clear();
    }

    public synchronized Set<Coordinate> getAllOccupiedCoordinates() {
        Set<Coordinate> allCoords = new HashSet<>();
        for (Ship ship : ships.values()) {
            allCoords.addAll(ship.getCoordinates());
        }
        return allCoords;
    }
}

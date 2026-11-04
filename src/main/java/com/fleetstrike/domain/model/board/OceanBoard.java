package com.fleetstrike.domain.model.board;

import com.fleetstrike.domain.exception.CellAlreadyAttackedException;
import com.fleetstrike.domain.exception.OutOfBoundsException;
import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.coordinate.GridDimension;
import com.fleetstrike.domain.model.ship.Fleet;
import com.fleetstrike.domain.model.ship.Ship;
import com.fleetstrike.domain.model.ship.ShipPlacement;

import java.util.*;

public class OceanBoard {
    private final GridDimension dimension;
    private final Fleet fleet;
    private final Set<Coordinate> incomingShots = new LinkedHashSet<>();

    public OceanBoard() {
        this(GridDimension.STANDARD, new Fleet());
    }

    public OceanBoard(GridDimension dimension, Fleet fleet) {
        this.dimension = Objects.requireNonNull(dimension, "GridDimension cannot be null");
        this.fleet = Objects.requireNonNull(fleet, "Fleet cannot be null");
    }

    public GridDimension getDimension() {
        return dimension;
    }

    public Fleet getFleet() {
        return fleet;
    }

    public Set<Coordinate> getIncomingShots() {
        return Collections.unmodifiableSet(incomingShots);
    }

    public synchronized void clear() {
        fleet.clear();
        incomingShots.clear();
    }

    public synchronized void placeShip(ShipPlacement placement) {
        Objects.requireNonNull(placement, "ShipPlacement cannot be null");
        for (Coordinate coord : placement.getCoordinates()) {
            validateWithinBounds(coord);
        }
        Ship ship = Ship.fromPlacement(placement);
        fleet.addShip(ship);
    }

    public synchronized void placeShip(Ship ship) {
        Objects.requireNonNull(ship, "Ship cannot be null");
        for (Coordinate coord : ship.getCoordinates()) {
            validateWithinBounds(coord);
        }
        fleet.addShip(ship);
    }

    public synchronized ShotResult receiveShot(Coordinate coordinate) {
        Objects.requireNonNull(coordinate, "Coordinate cannot be null");
        validateWithinBounds(coordinate);

        if (incomingShots.contains(coordinate)) {
            throw new CellAlreadyAttackedException("Coordinate " + coordinate + " has already been attacked");
        }

        incomingShots.add(coordinate);
        Optional<Ship> targetShip = fleet.findShipAt(coordinate);

        if (targetShip.isPresent()) {
            Ship ship = targetShip.get();
            ship.recordHit(coordinate);
            if (ship.isSunk()) {
                boolean allSunk = fleet.isAllSunk();
                return ShotResult.sunk(coordinate, ship, allSunk);
            }
            return ShotResult.hit(coordinate, ship.getShipType());
        }

        return ShotResult.miss(coordinate);
    }

    public synchronized boolean hasBeenAttacked(Coordinate coordinate) {
        return incomingShots.contains(coordinate);
    }

    public synchronized CellStatus getCellStatus(Coordinate coordinate) {
        validateWithinBounds(coordinate);
        boolean attacked = incomingShots.contains(coordinate);
        Optional<Ship> ship = fleet.findShipAt(coordinate);

        if (attacked) {
            if (ship.isPresent()) {
                return ship.get().isSunk() ? CellStatus.SUNK : CellStatus.HIT;
            } else {
                return CellStatus.MISS;
            }
        } else {
            return ship.isPresent() ? CellStatus.SHIP : CellStatus.WATER;
        }
    }

    private void validateWithinBounds(Coordinate coordinate) {
        if (!dimension.contains(coordinate.getRow(), coordinate.getCol())) {
            throw new OutOfBoundsException(
                    "Coordinate " + coordinate + " is outside grid bounds " + dimension
            );
        }
    }
}

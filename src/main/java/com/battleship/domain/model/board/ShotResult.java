package com.battleship.domain.model.board;

import com.battleship.domain.model.coordinate.Coordinate;
import com.battleship.domain.model.ship.Ship;
import com.battleship.domain.model.ship.ShipType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ShotResult {
    private final Coordinate coordinate;
    private final ShotStatus status;
    private final ShipType hitShipType;
    private final Ship sunkShip;
    private final boolean fleetDestroyed;
    private final String message;

    private ShotResult(Coordinate coordinate,
                       ShotStatus status,
                       ShipType hitShipType,
                       Ship sunkShip,
                       boolean fleetDestroyed,
                       String message) {
        this.coordinate = Objects.requireNonNull(coordinate, "Coordinate cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.hitShipType = hitShipType;
        this.sunkShip = sunkShip;
        this.fleetDestroyed = fleetDestroyed;
        this.message = message != null ? message : "";
    }

    public static ShotResult miss(Coordinate coordinate) {
        return new ShotResult(coordinate, ShotStatus.MISS, null, null, false, "Splash! Shot at " + coordinate + " was a MISS.");
    }

    public static ShotResult hit(Coordinate coordinate, ShipType shipType) {
        return new ShotResult(coordinate, ShotStatus.HIT, shipType, null, false, 
                "BOOM! Direct hit on " + shipType.getDisplayName() + " at " + coordinate + "!");
    }

    public static ShotResult sunk(Coordinate coordinate, Ship ship, boolean fleetDestroyed) {
        String msg = "BOOM! You sank the " + ship.getShipType().getDisplayName() + "!";
        if (fleetDestroyed) {
            msg += " All enemy ships destroyed!";
        }
        return new ShotResult(coordinate, ShotStatus.SUNK, ship.getShipType(), ship, fleetDestroyed, msg);
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public ShotStatus getStatus() {
        return status;
    }

    public Optional<ShipType> getHitShipType() {
        return Optional.ofNullable(hitShipType);
    }

    public Optional<Ship> getSunkShip() {
        return Optional.ofNullable(sunkShip);
    }

    public List<Coordinate> getSunkShipCoordinates() {
        return sunkShip != null ? sunkShip.getCoordinates() : Collections.emptyList();
    }

    public boolean isFleetDestroyed() {
        return fleetDestroyed;
    }

    public String getMessage() {
        return message;
    }

    public boolean isHit() {
        return status == ShotStatus.HIT || status == ShotStatus.SUNK;
    }

    @Override
    public String toString() {
        return message;
    }
}

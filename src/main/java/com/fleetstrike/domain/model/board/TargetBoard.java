package com.fleetstrike.domain.model.board;

import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.coordinate.GridDimension;

import java.util.*;

public class TargetBoard {
    private final GridDimension dimension;
    private final Map<Coordinate, CellStatus> targetedCells = new HashMap<>();

    public TargetBoard() {
        this(GridDimension.STANDARD);
    }

    public TargetBoard(GridDimension dimension) {
        this.dimension = Objects.requireNonNull(dimension, "GridDimension cannot be null");
    }

    public GridDimension getDimension() {
        return dimension;
    }

    public synchronized void recordShot(ShotResult result) {
        Objects.requireNonNull(result, "ShotResult cannot be null");
        Coordinate coord = result.getCoordinate();

        switch (result.getStatus()) {
            case MISS -> targetedCells.put(coord, CellStatus.MISS);
            case HIT -> targetedCells.put(coord, CellStatus.HIT);
            case SUNK -> {
                targetedCells.put(coord, CellStatus.SUNK);
                for (Coordinate c : result.getSunkShipCoordinates()) {
                    targetedCells.put(c, CellStatus.SUNK);
                }
            }
        }
    }

    public synchronized CellStatus getCellStatus(Coordinate coordinate) {
        return targetedCells.getOrDefault(coordinate, CellStatus.WATER);
    }

    public synchronized boolean hasBeenAttacked(Coordinate coordinate) {
        return targetedCells.containsKey(coordinate);
    }

    public synchronized Map<Coordinate, CellStatus> getTargetedCells() {
        return Collections.unmodifiableMap(new HashMap<>(targetedCells));
    }
}

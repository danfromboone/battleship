package com.battleship.domain.policy;

import com.battleship.domain.model.board.OceanBoard;
import com.battleship.domain.model.coordinate.Coordinate;
import com.battleship.domain.model.coordinate.GridDimension;
import com.battleship.domain.model.coordinate.Orientation;
import com.battleship.domain.model.ship.Fleet;
import com.battleship.domain.model.ship.ShipPlacement;
import com.battleship.domain.model.ship.ShipType;

import java.util.*;

public class FleetPlacementService {
    private final Random random;

    public FleetPlacementService() {
        this(new Random());
    }

    public FleetPlacementService(Random random) {
        this.random = Objects.requireNonNull(random, "Random cannot be null");
    }

    /**
     * Randomly places all standard ships onto the given ocean board.
     */
    public void deployRandomFleet(OceanBoard board) {
        List<ShipType> shipsToPlace = new ArrayList<>(Fleet.REQUIRED_SHIP_TYPES);
        // Sort largest ship first to make random placement faster and less prone to collisions
        shipsToPlace.sort((a, b) -> Integer.compare(b.getSize(), a.getSize()));

        GridDimension dim = board.getDimension();
        for (ShipType shipType : shipsToPlace) {
            boolean placed = false;
            int attempts = 0;
            while (!placed && attempts < 1000) {
                attempts++;
                Orientation orientation = random.nextBoolean() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
                int maxRow = (orientation == Orientation.VERTICAL) ? dim.getRows() - shipType.getSize() : dim.getRows() - 1;
                int maxCol = (orientation == Orientation.HORIZONTAL) ? dim.getColumns() - shipType.getSize() : dim.getColumns() - 1;

                int row = random.nextInt(maxRow + 1);
                int col = random.nextInt(maxCol + 1);
                Coordinate start = new Coordinate(row, col);
                ShipPlacement placement = new ShipPlacement(shipType, start, orientation);

                if (canPlace(board, placement)) {
                    board.placeShip(placement);
                    placed = true;
                }
            }

            if (!placed) {
                throw new IllegalStateException("Failed to randomly place ship " + shipType + " after 1000 attempts");
            }
        }
    }

    public boolean canPlace(OceanBoard board, ShipPlacement placement) {
        GridDimension dim = board.getDimension();
        for (Coordinate c : placement.getCoordinates()) {
            if (!dim.contains(c.getRow(), c.getCol())) {
                return false;
            }
            if (board.getFleet().findShipAt(c).isPresent()) {
                return false;
            }
        }
        return true;
    }
}

package com.fleetstrike.domain.model.board;

import com.fleetstrike.domain.exception.CellAlreadyAttackedException;
import com.fleetstrike.domain.exception.OutOfBoundsException;
import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.coordinate.Orientation;
import com.fleetstrike.domain.model.ship.ShipPlacement;
import com.fleetstrike.domain.model.ship.ShipType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OceanBoardTest {

    @Test
    @DisplayName("Should receive hits, misses, and sinking shot correctly")
    void shouldProcessIncomingShots() {
        OceanBoard board = new OceanBoard();
        // Place Destroyer at A1-A2 (0,0 and 0,1)
        board.placeShip(new ShipPlacement(ShipType.DESTROYER, Coordinate.of(0, 0), Orientation.HORIZONTAL));

        assertThat(board.getCellStatus(Coordinate.of(0, 0))).isEqualTo(CellStatus.SHIP);
        assertThat(board.getCellStatus(Coordinate.of(5, 5))).isEqualTo(CellStatus.WATER);

        // Fire a miss at (5, 5)
        ShotResult missResult = board.receiveShot(Coordinate.of(5, 5));
        assertThat(missResult.getStatus()).isEqualTo(ShotStatus.MISS);
        assertThat(board.getCellStatus(Coordinate.of(5, 5))).isEqualTo(CellStatus.MISS);

        // Fire a hit at (0, 0)
        ShotResult hitResult = board.receiveShot(Coordinate.of(0, 0));
        assertThat(hitResult.getStatus()).isEqualTo(ShotStatus.HIT);
        assertThat(hitResult.getHitShipType()).contains(ShipType.DESTROYER);
        assertThat(board.getCellStatus(Coordinate.of(0, 0))).isEqualTo(CellStatus.HIT);

        // Fire a sinking hit at (0, 1)
        ShotResult sinkResult = board.receiveShot(Coordinate.of(0, 1));
        assertThat(sinkResult.getStatus()).isEqualTo(ShotStatus.SUNK);
        assertThat(sinkResult.getSunkShip()).isPresent();
        assertThat(board.getCellStatus(Coordinate.of(0, 0))).isEqualTo(CellStatus.SUNK);
        assertThat(board.getCellStatus(Coordinate.of(0, 1))).isEqualTo(CellStatus.SUNK);
    }

    @Test
    @DisplayName("Should reject shot on already attacked cell")
    void shouldRejectRepeatedShot() {
        OceanBoard board = new OceanBoard();
        Coordinate target = Coordinate.of(3, 3);
        board.receiveShot(target);

        assertThatThrownBy(() -> board.receiveShot(target))
                .isInstanceOf(CellAlreadyAttackedException.class);
    }

    @Test
    @DisplayName("Should reject ship placement out of bounds")
    void shouldRejectOutOfBoundsShip() {
        OceanBoard board = new OceanBoard();
        // Carrier length 5 placed at column 7 horizontally exceeds 10 columns (7,8,9,10,11)
        ShipPlacement outOfBounds = new ShipPlacement(ShipType.CARRIER, Coordinate.of(0, 7), Orientation.HORIZONTAL);

        assertThatThrownBy(() -> board.placeShip(outOfBounds))
                .isInstanceOf(OutOfBoundsException.class);
    }
}

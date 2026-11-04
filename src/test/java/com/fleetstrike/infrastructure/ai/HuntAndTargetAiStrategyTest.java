package com.fleetstrike.infrastructure.ai;

import com.fleetstrike.domain.model.board.CellStatus;
import com.fleetstrike.domain.model.board.ShotResult;
import com.fleetstrike.domain.model.board.TargetBoard;
import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.coordinate.Orientation;
import com.fleetstrike.domain.model.ship.Ship;
import com.fleetstrike.domain.model.ship.ShipPlacement;
import com.fleetstrike.domain.model.ship.ShipType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class HuntAndTargetAiStrategyTest {

    @Test
    @DisplayName("In hunt mode with empty board, AI should choose valid unattacked coordinate with checkerboard parity")
    void shouldPickParityInHuntMode() {
        TargetBoard board = new TargetBoard();
        HuntAndTargetAiStrategy strategy = new HuntAndTargetAiStrategy(new Random(42));

        Coordinate choice = strategy.chooseTarget(board);
        assertThat(choice).isNotNull();
        assertThat(board.hasBeenAttacked(choice)).isFalse();
        assertThat((choice.getRow() + choice.getCol()) % 2).isEqualTo(0);
    }

    @Test
    @DisplayName("In target mode with one hit, AI should choose an adjacent unattacked cell")
    void shouldTargetAdjacentToSingleHit() {
        TargetBoard board = new TargetBoard();
        Coordinate hitCoord = Coordinate.of(4, 4);
        board.recordShot(ShotResult.hit(hitCoord, ShipType.DESTROYER));

        HuntAndTargetAiStrategy strategy = new HuntAndTargetAiStrategy(new Random(42));
        Coordinate choice = strategy.chooseTarget(board);

        assertThat(choice).isNotNull();
        assertThat(board.hasBeenAttacked(choice)).isFalse();
        // Choice must be orthogonal neighbor of (4, 4)
        assertThat(hitCoord.getNeighbors(board.getDimension())).contains(choice);
    }

    @Test
    @DisplayName("In target mode with collinear hits, AI should choose one of the line endpoints")
    void shouldTargetCollinearEndpoints() {
        TargetBoard board = new TargetBoard();
        // Collinear horizontal hits at (3, 3) and (3, 4)
        Coordinate hit1 = Coordinate.of(3, 3);
        Coordinate hit2 = Coordinate.of(3, 4);
        board.recordShot(ShotResult.hit(hit1, ShipType.CRUISER));
        board.recordShot(ShotResult.hit(hit2, ShipType.CRUISER));

        HuntAndTargetAiStrategy strategy = new HuntAndTargetAiStrategy(new Random(42));
        Coordinate choice = strategy.chooseTarget(board);

        // Expected endpoints: (3, 2) or (3, 5)
        assertThat(choice).isIn(Coordinate.of(3, 2), Coordinate.of(3, 5));
    }

    @Test
    @DisplayName("When ship is sunk, AI should return to hunt mode")
    void shouldResumeHuntAfterShipIsSunk() {
        TargetBoard board = new TargetBoard();
        Coordinate c1 = Coordinate.of(2, 2);
        Coordinate c2 = Coordinate.of(2, 3);

        Ship destroyer = Ship.fromPlacement(new ShipPlacement(ShipType.DESTROYER, c1, Orientation.HORIZONTAL));
        destroyer.recordHit(c1);
        destroyer.recordHit(c2);

        board.recordShot(ShotResult.sunk(c2, destroyer, false));
        // Cells are now SUNK, not active HIT
        assertThat(board.getCellStatus(c1)).isEqualTo(CellStatus.SUNK);
        assertThat(board.getCellStatus(c2)).isEqualTo(CellStatus.SUNK);

        HuntAndTargetAiStrategy strategy = new HuntAndTargetAiStrategy(new Random(42));
        Coordinate choice = strategy.chooseTarget(board);

        // Should be in hunt mode with parity
        assertThat(board.hasBeenAttacked(choice)).isFalse();
        assertThat((choice.getRow() + choice.getCol()) % 2).isEqualTo(0);
    }
}

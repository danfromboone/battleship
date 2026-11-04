package com.fleetstrike.domain.model.ship;

import com.fleetstrike.domain.exception.ShipOverlapException;
import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.coordinate.Orientation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FleetTest {

    @Test
    @DisplayName("Should add ships and detect when complete and when all sunk")
    void shouldManageFleetState() {
        Fleet fleet = new Fleet();
        assertThat(fleet.isComplete()).isFalse();

        fleet.addShip(Ship.fromPlacement(new ShipPlacement(ShipType.CARRIER, Coordinate.of(0, 0), Orientation.HORIZONTAL)));
        fleet.addShip(Ship.fromPlacement(new ShipPlacement(ShipType.BATTLESHIP, Coordinate.of(1, 0), Orientation.HORIZONTAL)));
        fleet.addShip(Ship.fromPlacement(new ShipPlacement(ShipType.CRUISER, Coordinate.of(2, 0), Orientation.HORIZONTAL)));
        fleet.addShip(Ship.fromPlacement(new ShipPlacement(ShipType.SUBMARINE, Coordinate.of(3, 0), Orientation.HORIZONTAL)));
        fleet.addShip(Ship.fromPlacement(new ShipPlacement(ShipType.DESTROYER, Coordinate.of(4, 0), Orientation.HORIZONTAL)));

        assertThat(fleet.isComplete()).isTrue();
        assertThat(fleet.getAfloatCount()).isEqualTo(5);
        assertThat(fleet.getSunkCount()).isEqualTo(0);
        assertThat(fleet.isAllSunk()).isFalse();

        // Sink the destroyer
        Ship destroyer = fleet.getShip(ShipType.DESTROYER).orElseThrow();
        destroyer.recordHit(Coordinate.of(4, 0));
        destroyer.recordHit(Coordinate.of(4, 1));

        assertThat(fleet.getAfloatCount()).isEqualTo(4);
        assertThat(fleet.getSunkCount()).isEqualTo(1);
        assertThat(fleet.isAllSunk()).isFalse();
    }

    @Test
    @DisplayName("Should reject overlapping ships")
    void shouldRejectOverlappingShips() {
        Fleet fleet = new Fleet();
        fleet.addShip(Ship.fromPlacement(new ShipPlacement(ShipType.DESTROYER, Coordinate.of(0, 0), Orientation.HORIZONTAL)));

        // Attempt to place Cruiser intersecting with Destroyer at (0, 1)
        ShipPlacement overlapping = new ShipPlacement(ShipType.CRUISER, Coordinate.of(0, 1), Orientation.VERTICAL);
        assertThatThrownBy(() -> fleet.addShip(Ship.fromPlacement(overlapping)))
                .isInstanceOf(ShipOverlapException.class);
    }

    @Test
    @DisplayName("Should reject duplicate ship types")
    void shouldRejectDuplicateShipTypes() {
        Fleet fleet = new Fleet();
        fleet.addShip(Ship.fromPlacement(new ShipPlacement(ShipType.DESTROYER, Coordinate.of(0, 0), Orientation.HORIZONTAL)));

        ShipPlacement duplicateType = new ShipPlacement(ShipType.DESTROYER, Coordinate.of(2, 0), Orientation.HORIZONTAL);
        assertThatThrownBy(() -> fleet.addShip(Ship.fromPlacement(duplicateType)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

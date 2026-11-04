package com.fleetstrike.domain.model.ship;

import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.coordinate.Orientation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShipTest {

    @Test
    @DisplayName("Should create ship from valid placement and verify occupancy and hit tracking")
    void shouldTrackHitsAndSink() {
        ShipPlacement placement = new ShipPlacement(ShipType.DESTROYER, Coordinate.of(1, 1), Orientation.HORIZONTAL);
        Ship destroyer = Ship.fromPlacement(placement);

        assertThat(destroyer.getShipType()).isEqualTo(ShipType.DESTROYER);
        assertThat(destroyer.getRemainingHealth()).isEqualTo(2);
        assertThat(destroyer.isSunk()).isFalse();
        assertThat(destroyer.occupies(Coordinate.of(1, 1))).isTrue();
        assertThat(destroyer.occupies(Coordinate.of(1, 2))).isTrue();
        assertThat(destroyer.occupies(Coordinate.of(1, 3))).isFalse();

        // Record first hit
        boolean firstHit = destroyer.recordHit(Coordinate.of(1, 1));
        assertThat(firstHit).isTrue();
        assertThat(destroyer.getRemainingHealth()).isEqualTo(1);
        assertThat(destroyer.isSunk()).isFalse();

        // Record duplicate hit on same coordinate returns false
        boolean duplicateHit = destroyer.recordHit(Coordinate.of(1, 1));
        assertThat(duplicateHit).isFalse();
        assertThat(destroyer.getRemainingHealth()).isEqualTo(1);

        // Record second hit, sinking the ship
        boolean secondHit = destroyer.recordHit(Coordinate.of(1, 2));
        assertThat(secondHit).isTrue();
        assertThat(destroyer.getRemainingHealth()).isEqualTo(0);
        assertThat(destroyer.isSunk()).isTrue();
    }

    @Test
    @DisplayName("Should reject hit coordinate not occupied by the ship")
    void shouldRejectInvalidHitCoordinate() {
        Ship destroyer = new Ship(ShipType.DESTROYER, List.of(Coordinate.of(0, 0), Coordinate.of(0, 1)));
        assertThatThrownBy(() -> destroyer.recordHit(Coordinate.of(5, 5)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should enforce coordinate count matches ship size")
    void shouldEnforceSizeMatchesCoordinates() {
        assertThatThrownBy(() -> new Ship(ShipType.CARRIER, List.of(Coordinate.of(0, 0), Coordinate.of(0, 1))))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

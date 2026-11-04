package com.battleship.presentation.swing;

import com.battleship.domain.model.board.ShotResult;
import com.battleship.domain.model.coordinate.Coordinate;
import com.battleship.domain.model.ship.Ship;
import com.battleship.domain.model.ship.ShipPlacement;
import com.battleship.domain.model.ship.ShipType;
import com.battleship.domain.model.coordinate.Orientation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CombatLogPanelTest {

    @Test
    @DisplayName("Should add entries for miss, hit, and sunk, and clear log")
    void shouldAddAndClearLogEntries() {
        CombatLogPanel logPanel = new CombatLogPanel();
        assertThat(logPanel.getEntryCount()).isEqualTo(0);

        // Add Miss
        logPanel.addEntry(1, "Admiral", Coordinate.of(0, 0), ShotResult.miss(Coordinate.of(0, 0)));
        assertThat(logPanel.getEntryCount()).isEqualTo(1);

        // Add Hit
        logPanel.addEntry(2, "Admiral", Coordinate.of(1, 1), ShotResult.hit(Coordinate.of(1, 1), ShipType.CRUISER));
        assertThat(logPanel.getEntryCount()).isEqualTo(2);

        // Add Sunk
        Ship destroyer = Ship.fromPlacement(new ShipPlacement(ShipType.DESTROYER, Coordinate.of(2, 2), Orientation.HORIZONTAL));
        logPanel.addEntry(3, "Admiral", Coordinate.of(2, 3), ShotResult.sunk(Coordinate.of(2, 3), destroyer, false));
        assertThat(logPanel.getEntryCount()).isEqualTo(3);

        // Clear
        logPanel.clearLog();
        assertThat(logPanel.getEntryCount()).isEqualTo(0);
    }
}

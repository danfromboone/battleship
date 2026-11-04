package com.battleship.presentation.swing;

import com.battleship.domain.model.coordinate.Coordinate;
import com.battleship.domain.model.coordinate.Orientation;
import com.battleship.domain.model.player.Player;
import com.battleship.domain.model.player.PlayerId;
import com.battleship.domain.model.player.PlayerType;
import com.battleship.domain.model.ship.ShipPlacement;
import com.battleship.domain.model.ship.ShipType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class FleetStatusPanelTest {

    @Test
    @DisplayName("Should initialize and update fleet status for own fleet and enemy radar")
    void shouldUpdateFleetStatus() {
        FleetStatusPanel ownPanel = new FleetStatusPanel("Your Fleet", FleetStatusPanel.ViewType.OWN_FLEET);
        FleetStatusPanel enemyPanel = new FleetStatusPanel("Enemy Radar", FleetStatusPanel.ViewType.ENEMY_RADAR);

        Player player = new Player(PlayerId.of("P1"), "Commander Bob", PlayerType.HUMAN);
        player.getOceanBoard().placeShip(new ShipPlacement(ShipType.DESTROYER, Coordinate.of(0, 0), Orientation.HORIZONTAL));

        assertDoesNotThrow(() -> {
            ownPanel.updateStatus(player);
            enemyPanel.updateStatus(player);
        });

        // Hit and sink destroyer
        player.getOceanBoard().receiveShot(Coordinate.of(0, 0));
        player.getOceanBoard().receiveShot(Coordinate.of(0, 1));

        assertDoesNotThrow(() -> {
            ownPanel.updateStatus(player);
            enemyPanel.updateStatus(player);
        });
    }
}

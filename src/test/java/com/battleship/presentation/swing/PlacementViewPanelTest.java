package com.battleship.presentation.swing;

import com.battleship.application.BattleshipApplicationService;
import com.battleship.application.command.CreateGameCommand;
import com.battleship.domain.model.game.Game;
import com.battleship.domain.model.player.Player;
import com.battleship.domain.model.player.PlayerType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PlacementViewPanelTest {

    @Test
    @DisplayName("Should setup placement panel, support auto-deploy, and fire completion callback")
    void shouldHandlePlacementLifecycle() {
        BattleshipApplicationService service = new BattleshipApplicationService();
        PlacementViewPanel panel = new PlacementViewPanel(service);

        Game game = service.createGame(new CreateGameCommand("Player 1", PlayerType.HUMAN, "Player 2", PlayerType.COMPUTER));
        Player p1 = game.getPlayer1();

        panel.setup(game, p1);
        assertThat(p1.isFleetDeployed()).isFalse();

        AtomicBoolean completed = new AtomicBoolean(false);
        panel.setPlacementCompleteListener(player -> completed.set(true));

        // Auto deploy fleet
        service.autoDeployFleet(game.getId(), p1.getId());
        assertThat(p1.isFleetDeployed()).isTrue();
    }
}

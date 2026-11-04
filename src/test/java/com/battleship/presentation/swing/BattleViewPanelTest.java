package com.battleship.presentation.swing;

import com.battleship.application.BattleshipApplicationService;
import com.battleship.application.command.CreateGameCommand;
import com.battleship.domain.model.game.Game;
import com.battleship.domain.model.player.PlayerType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BattleViewPanelTest {

    @Test
    @DisplayName("Should initialize battle panel and update UI state without errors")
    void shouldInitializeBattleView() {
        BattleshipApplicationService service = new BattleshipApplicationService();
        BattleViewPanel battleView = new BattleViewPanel(service);

        Game game = service.createGame(new CreateGameCommand("P1", PlayerType.HUMAN, "P2", PlayerType.COMPUTER));
        service.autoDeployFleet(game.getId(), game.getPlayer1().getId());
        service.autoDeployFleet(game.getId(), game.getPlayer2().getId());
        service.startBattle(game.getId());

        assertDoesNotThrow(() -> {
            battleView.startBattle(game, false, false);
            battleView.updateUiState();
        });
    }
}

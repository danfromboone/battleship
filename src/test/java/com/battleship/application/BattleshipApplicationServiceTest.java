package com.battleship.application;

import com.battleship.application.command.CreateGameCommand;
import com.battleship.application.command.FireShotCommand;
import com.battleship.application.command.PlaceShipCommand;
import com.battleship.domain.model.board.ShotResult;
import com.battleship.domain.model.coordinate.Coordinate;
import com.battleship.domain.model.coordinate.Orientation;
import com.battleship.domain.model.game.Game;
import com.battleship.domain.model.game.GameState;
import com.battleship.domain.model.player.PlayerType;
import com.battleship.domain.model.ship.ShipType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BattleshipApplicationServiceTest {

    private BattleshipApplicationService service;

    @BeforeEach
    void setUp() {
        service = new BattleshipApplicationService();
    }

    @Test
    @DisplayName("Should create game, deploy fleets, and execute player and AI turns")
    void shouldOrchestrateGameLifecycle() {
        Game game = service.createGame(new CreateGameCommand(
                "Human Commander", PlayerType.HUMAN,
                "AI Admiral", PlayerType.COMPUTER
        ));

        assertThat(game.getState()).isEqualTo(GameState.PLACING_SHIPS);

        // Deploy Human ships manually
        service.placeShip(new PlaceShipCommand(game.getId(), game.getPlayer1().getId(),
                ShipType.CARRIER, Coordinate.of(0, 0), Orientation.HORIZONTAL));
        service.placeShip(new PlaceShipCommand(game.getId(), game.getPlayer1().getId(),
                ShipType.BATTLESHIP, Coordinate.of(1, 0), Orientation.HORIZONTAL));
        service.placeShip(new PlaceShipCommand(game.getId(), game.getPlayer1().getId(),
                ShipType.CRUISER, Coordinate.of(2, 0), Orientation.HORIZONTAL));
        service.placeShip(new PlaceShipCommand(game.getId(), game.getPlayer1().getId(),
                ShipType.SUBMARINE, Coordinate.of(3, 0), Orientation.HORIZONTAL));
        service.placeShip(new PlaceShipCommand(game.getId(), game.getPlayer1().getId(),
                ShipType.DESTROYER, Coordinate.of(4, 0), Orientation.HORIZONTAL));

        assertThat(game.getPlayer1().isFleetDeployed()).isTrue();

        // Auto deploy AI ships
        service.autoDeployFleet(game.getId(), game.getPlayer2().getId());
        assertThat(game.getPlayer2().isFleetDeployed()).isTrue();

        // Start battle
        service.startBattle(game.getId());
        assertThat(game.getState()).isEqualTo(GameState.IN_PROGRESS);

        // Human fires shot
        ShotResult humanShot = service.fireShot(new FireShotCommand(game.getId(), game.getPlayer1().getId(), Coordinate.of(0, 0)));
        assertThat(humanShot).isNotNull();
        assertThat(game.getActivePlayerId()).isEqualTo(game.getPlayer2().getId());

        // AI fires shot
        ShotResult aiShot = service.executeAiTurn(game.getId(), game.getPlayer2().getId());
        assertThat(aiShot).isNotNull();
        assertThat(game.getActivePlayerId()).isEqualTo(game.getPlayer1().getId());
    }
}

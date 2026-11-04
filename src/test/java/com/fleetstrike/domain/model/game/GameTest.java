package com.fleetstrike.domain.model.game;

import com.fleetstrike.domain.event.DomainEvent;
import com.fleetstrike.domain.event.DomainEventPublisher;
import com.fleetstrike.domain.event.GameOverEvent;
import com.fleetstrike.domain.exception.InvalidGameStateException;
import com.fleetstrike.domain.model.board.ShotResult;
import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.coordinate.Orientation;
import com.fleetstrike.domain.model.player.Player;
import com.fleetstrike.domain.model.player.PlayerId;
import com.fleetstrike.domain.model.player.PlayerType;
import com.fleetstrike.domain.model.ship.ShipPlacement;
import com.fleetstrike.domain.model.ship.ShipType;
import com.fleetstrike.domain.policy.FleetPlacementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTest {

    private Player player1;
    private Player player2;
    private Game game;
    private List<DomainEvent> capturedEvents;

    @BeforeEach
    void setUp() {
        capturedEvents = new ArrayList<>();
        DomainEventPublisher.instance().clearListeners();
        DomainEventPublisher.instance().subscribe(capturedEvents::add);

        player1 = new Player(PlayerId.of("P1"), "Alice", PlayerType.HUMAN);
        player2 = new Player(PlayerId.of("P2"), "Bob", PlayerType.HUMAN);
        game = new Game(GameId.random(), player1, player2);
    }

    @Test
    @DisplayName("Should enforce state transitions from setup to battle")
    void shouldEnforceStateTransitions() {
        assertThat(game.getState()).isEqualTo(GameState.PLACING_SHIPS);

        // Cannot start battle before fleets are placed
        assertThatThrownBy(() -> game.startBattle())
                .isInstanceOf(InvalidGameStateException.class);

        FleetPlacementService placementService = new FleetPlacementService();
        game.autoDeployFleet(player1.getId(), placementService);
        game.autoDeployFleet(player2.getId(), placementService);

        game.startBattle();
        assertThat(game.getState()).isEqualTo(GameState.IN_PROGRESS);
        assertThat(game.getActivePlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Should alternate turns and reject out-of-turn firing")
    void shouldAlternateTurns() {
        FleetPlacementService placementService = new FleetPlacementService();
        game.autoDeployFleet(player1.getId(), placementService);
        game.autoDeployFleet(player2.getId(), placementService);
        game.startBattle();

        // Player 2 attempts to fire out of turn
        assertThatThrownBy(() -> game.fireShot(player2.getId(), Coordinate.of(0, 0)))
                .isInstanceOf(InvalidGameStateException.class);

        // Player 1 fires valid shot
        ShotResult result1 = game.fireShot(player1.getId(), Coordinate.of(0, 0));
        assertThat(result1).isNotNull();
        // Turn switched to Player 2
        assertThat(game.getActivePlayerId()).isEqualTo(player2.getId());

        // Player 2 fires
        ShotResult result2 = game.fireShot(player2.getId(), Coordinate.of(0, 0));
        assertThat(result2).isNotNull();
        // Turn switched back to Player 1
        assertThat(game.getActivePlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Should detect victory when all enemy ships are sunk")
    void shouldDetectVictory() {
        // Deploy player1 fleet manually
        deployStandardFleet(player1, 0);

        // Deploy player2 fleet manually so we know exact coordinates
        deployStandardFleet(player2, 0);

        game.startBattle();

        // Player 1 systematically sinks player 2's entire fleet
        // CARRIER: row 0, cols 0..4
        // BATTLESHIP: row 1, cols 0..3
        // CRUISER: row 2, cols 0..2
        // SUBMARINE: row 3, cols 0..2
        // DESTROYER: row 4, cols 0..1

        Coordinate[][] fleetCoords = {
                {Coordinate.of(0, 0), Coordinate.of(0, 1), Coordinate.of(0, 2), Coordinate.of(0, 3), Coordinate.of(0, 4)},
                {Coordinate.of(1, 0), Coordinate.of(1, 1), Coordinate.of(1, 2), Coordinate.of(1, 3)},
                {Coordinate.of(2, 0), Coordinate.of(2, 1), Coordinate.of(2, 2)},
                {Coordinate.of(3, 0), Coordinate.of(3, 1), Coordinate.of(3, 2)},
                {Coordinate.of(4, 0), Coordinate.of(4, 1)}
        };

        int p2ShotCount = 0;

        for (int shipIdx = 0; shipIdx < fleetCoords.length; shipIdx++) {
            Coordinate[] ship = fleetCoords[shipIdx];
            for (int cellIdx = 0; cellIdx < ship.length; cellIdx++) {
                Coordinate target = ship[cellIdx];
                ShotResult res = game.fireShot(player1.getId(), target);

                if (game.getState() == GameState.FINISHED) {
                    assertThat(res.isFleetDestroyed()).isTrue();
                    assertThat(game.getWinnerId()).contains(player1.getId());
                    assertThat(capturedEvents).anyMatch(e -> e instanceof GameOverEvent);
                    return;
                }

                // Player 2 takes turn firing misses in bottom rows (8 and 9)
                int r2 = 8 + (p2ShotCount / 10);
                int c2 = p2ShotCount % 10;
                p2ShotCount++;
                game.fireShot(player2.getId(), Coordinate.of(r2, c2));
            }
        }

        assertThat(game.getState()).isEqualTo(GameState.FINISHED);
    }

    @Test
    @DisplayName("Should handle surrender properly")
    void shouldHandleSurrender() {
        FleetPlacementService placementService = new FleetPlacementService();
        game.autoDeployFleet(player1.getId(), placementService);
        game.autoDeployFleet(player2.getId(), placementService);
        game.startBattle();

        game.surrender(player1.getId());
        assertThat(game.getState()).isEqualTo(GameState.FINISHED);
        assertThat(game.getWinnerId()).contains(player2.getId());
        assertThat(capturedEvents).anyMatch(e -> e instanceof GameOverEvent);
    }

    private void deployStandardFleet(Player player, int startRow) {
        player.getOceanBoard().placeShip(new ShipPlacement(ShipType.CARRIER, Coordinate.of(startRow, 0), Orientation.HORIZONTAL));
        player.getOceanBoard().placeShip(new ShipPlacement(ShipType.BATTLESHIP, Coordinate.of(startRow + 1, 0), Orientation.HORIZONTAL));
        player.getOceanBoard().placeShip(new ShipPlacement(ShipType.CRUISER, Coordinate.of(startRow + 2, 0), Orientation.HORIZONTAL));
        player.getOceanBoard().placeShip(new ShipPlacement(ShipType.SUBMARINE, Coordinate.of(startRow + 3, 0), Orientation.HORIZONTAL));
        player.getOceanBoard().placeShip(new ShipPlacement(ShipType.DESTROYER, Coordinate.of(startRow + 4, 0), Orientation.HORIZONTAL));
    }
}

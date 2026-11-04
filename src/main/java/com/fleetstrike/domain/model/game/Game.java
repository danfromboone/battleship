package com.fleetstrike.domain.model.game;

import com.fleetstrike.domain.event.*;
import com.fleetstrike.domain.exception.InvalidGameStateException;
import com.fleetstrike.domain.model.board.ShotResult;
import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.player.Player;
import com.fleetstrike.domain.model.player.PlayerId;
import com.fleetstrike.domain.model.ship.Ship;
import com.fleetstrike.domain.model.ship.ShipPlacement;
import com.fleetstrike.domain.policy.FleetPlacementService;

import java.util.*;

public class Game {
    private final GameId id;
    private final Player player1;
    private final Player player2;
    private GameState state;
    private PlayerId activePlayerId;
    private PlayerId winnerId;
    private final List<TurnRecord> history = new ArrayList<>();
    private final DomainEventPublisher eventPublisher;

    public Game(GameId id, Player player1, Player player2) {
        this(id, player1, player2, DomainEventPublisher.instance());
    }

    public Game(GameId id, Player player1, Player player2, DomainEventPublisher eventPublisher) {
        this.id = Objects.requireNonNull(id, "GameId cannot be null");
        this.player1 = Objects.requireNonNull(player1, "Player 1 cannot be null");
        this.player2 = Objects.requireNonNull(player2, "Player 2 cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "DomainEventPublisher cannot be null");
        this.state = GameState.PLACING_SHIPS;
        this.activePlayerId = player1.getId();
    }

    public GameId getId() {
        return id;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public GameState getState() {
        return state;
    }

    public PlayerId getActivePlayerId() {
        return activePlayerId;
    }

    public Optional<PlayerId> getWinnerId() {
        return Optional.ofNullable(winnerId);
    }

    public List<TurnRecord> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public Player getPlayer(PlayerId playerId) {
        if (player1.getId().equals(playerId)) return player1;
        if (player2.getId().equals(playerId)) return player2;
        throw new IllegalArgumentException("Unknown player id: " + playerId);
    }

    public Player getOpponent(PlayerId playerId) {
        if (player1.getId().equals(playerId)) return player2;
        if (player2.getId().equals(playerId)) return player1;
        throw new IllegalArgumentException("Unknown player id: " + playerId);
    }

    public Player getActivePlayer() {
        return getPlayer(activePlayerId);
    }

    public synchronized void placeShip(PlayerId playerId, ShipPlacement placement) {
        if (state != GameState.PLACING_SHIPS) {
            throw new InvalidGameStateException("Cannot place ships while game state is " + state);
        }
        Player player = getPlayer(playerId);
        player.getOceanBoard().placeShip(placement);
        Ship placed = player.getOceanBoard().getFleet().getShip(placement.getShipType())
                .orElseThrow(() -> new IllegalStateException("Ship was not placed"));
        eventPublisher.publish(new ShipPlacedEvent(playerId, placed));
    }

    public synchronized void autoDeployFleet(PlayerId playerId, FleetPlacementService placementService) {
        if (state != GameState.PLACING_SHIPS) {
            throw new InvalidGameStateException("Cannot place ships while game state is " + state);
        }
        Player player = getPlayer(playerId);
        placementService.deployRandomFleet(player.getOceanBoard());
        for (Ship ship : player.getOceanBoard().getFleet().getShips()) {
            eventPublisher.publish(new ShipPlacedEvent(playerId, ship));
        }
    }

    public synchronized void startBattle() {
        if (state != GameState.PLACING_SHIPS) {
            throw new InvalidGameStateException("Cannot start battle when game state is " + state);
        }
        if (!player1.isFleetDeployed()) {
            throw new InvalidGameStateException(player1.getName() + " has not finished deploying their fleet");
        }
        if (!player2.isFleetDeployed()) {
            throw new InvalidGameStateException(player2.getName() + " has not finished deploying their fleet");
        }
        this.state = GameState.IN_PROGRESS;
        this.activePlayerId = player1.getId();
    }

    public synchronized ShotResult fireShot(PlayerId shooterId, Coordinate coordinate) {
        if (state != GameState.IN_PROGRESS) {
            throw new InvalidGameStateException("Game is not in progress (current state: " + state + ")");
        }
        if (!activePlayerId.equals(shooterId)) {
            throw new InvalidGameStateException("It is not player " + shooterId + "'s turn");
        }

        Player shooter = getPlayer(shooterId);
        Player defender = getOpponent(shooterId);

        // Notify shot fired
        eventPublisher.publish(new ShotFiredEvent(shooterId, defender.getId(), coordinate));

        // Process shot on defender board
        ShotResult result = defender.getOceanBoard().receiveShot(coordinate);

        // Update attacker tracking board
        shooter.getTargetBoard().recordShot(result);

        // Publish hit / sunk events
        if (result.isHit()) {
            result.getHitShipType().ifPresent(st ->
                    eventPublisher.publish(new ShipHitEvent(defender.getId(), coordinate, st))
            );
        }
        result.getSunkShip().ifPresent(sunk ->
                eventPublisher.publish(new ShipSunkEvent(defender.getId(), sunk))
        );

        // Record history
        TurnRecord turnRecord = new TurnRecord(history.size() + 1, shooterId, defender.getId(), coordinate, result);
        history.add(turnRecord);

        // Check victory condition
        if (result.isFleetDestroyed()) {
            this.state = GameState.FINISHED;
            this.winnerId = shooterId;
            eventPublisher.publish(new GameOverEvent(shooterId, defender.getId(), history.size()));
        } else {
            // Alternate turn
            this.activePlayerId = defender.getId();
        }

        return result;
    }

    public synchronized void surrender(PlayerId playerId) {
        if (state != GameState.IN_PROGRESS) {
            throw new InvalidGameStateException("Cannot surrender when game is not in progress");
        }
        Player surrendering = getPlayer(playerId);
        Player opponent = getOpponent(playerId);
        this.state = GameState.FINISHED;
        this.winnerId = opponent.getId();
        eventPublisher.publish(new GameOverEvent(winnerId, surrendering.getId(), history.size()));
    }
}

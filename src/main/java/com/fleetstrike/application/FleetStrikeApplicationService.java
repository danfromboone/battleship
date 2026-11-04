package com.fleetstrike.application;

import com.fleetstrike.application.command.CreateGameCommand;
import com.fleetstrike.application.command.FireShotCommand;
import com.fleetstrike.application.command.PlaceShipCommand;
import com.fleetstrike.domain.model.board.ShotResult;
import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.game.Game;
import com.fleetstrike.domain.model.game.GameId;
import com.fleetstrike.domain.model.player.Player;
import com.fleetstrike.domain.model.player.PlayerId;
import com.fleetstrike.domain.model.ship.ShipPlacement;
import com.fleetstrike.domain.policy.FleetPlacementService;
import com.fleetstrike.infrastructure.ai.AiStrategy;
import com.fleetstrike.infrastructure.ai.HuntAndTargetAiStrategy;
import com.fleetstrike.infrastructure.persistence.GameRepository;
import com.fleetstrike.infrastructure.persistence.InMemoryGameRepository;

import java.util.Objects;

public class FleetStrikeApplicationService {
    private final GameRepository gameRepository;
    private final FleetPlacementService fleetPlacementService;
    private final AiStrategy aiStrategy;

    public FleetStrikeApplicationService() {
        this(new InMemoryGameRepository(), new FleetPlacementService(), new HuntAndTargetAiStrategy());
    }

    public FleetStrikeApplicationService(GameRepository gameRepository,
                                       FleetPlacementService fleetPlacementService,
                                       AiStrategy aiStrategy) {
        this.gameRepository = Objects.requireNonNull(gameRepository, "GameRepository cannot be null");
        this.fleetPlacementService = Objects.requireNonNull(fleetPlacementService, "FleetPlacementService cannot be null");
        this.aiStrategy = Objects.requireNonNull(aiStrategy, "AiStrategy cannot be null");
    }

    public Game createGame(CreateGameCommand command) {
        Player p1 = new Player(PlayerId.of("P1"), command.player1Name(), command.player1Type());
        Player p2 = new Player(PlayerId.of("P2"), command.player2Name(), command.player2Type());
        Game game = new Game(GameId.random(), p1, p2);
        gameRepository.save(game);
        return game;
    }

    public void placeShip(PlaceShipCommand command) {
        Game game = getGame(command.gameId());
        ShipPlacement placement = new ShipPlacement(command.shipType(), command.startCoordinate(), command.orientation());
        game.placeShip(command.playerId(), placement);
        gameRepository.save(game);
    }

    public void autoDeployFleet(GameId gameId, PlayerId playerId) {
        Game game = getGame(gameId);
        game.autoDeployFleet(playerId, fleetPlacementService);
        gameRepository.save(game);
    }

    public void startBattle(GameId gameId) {
        Game game = getGame(gameId);
        game.startBattle();
        gameRepository.save(game);
    }

    public ShotResult fireShot(FireShotCommand command) {
        Game game = getGame(command.gameId());
        ShotResult result = game.fireShot(command.playerId(), command.coordinate());
        gameRepository.save(game);
        return result;
    }

    public ShotResult executeAiTurn(GameId gameId, PlayerId aiPlayerId) {
        Game game = getGame(gameId);
        Player aiPlayer = game.getPlayer(aiPlayerId);
        if (!aiPlayer.isComputer()) {
            throw new IllegalArgumentException("Player " + aiPlayerId + " is not an AI player");
        }

        Coordinate chosenTarget = aiStrategy.chooseTarget(aiPlayer.getTargetBoard());
        ShotResult result = game.fireShot(aiPlayerId, chosenTarget);
        gameRepository.save(game);
        return result;
    }

    public void surrender(GameId gameId, PlayerId playerId) {
        Game game = getGame(gameId);
        game.surrender(playerId);
        gameRepository.save(game);
    }

    public Game getGame(GameId gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
    }
}

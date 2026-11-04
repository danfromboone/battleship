package com.fleetstrike.infrastructure.persistence;

import com.fleetstrike.domain.model.game.Game;
import com.fleetstrike.domain.model.game.GameId;

import java.util.Optional;

public interface GameRepository {
    void save(Game game);
    Optional<Game> findById(GameId gameId);
    void delete(GameId gameId);
}

package com.battleship.infrastructure.persistence;

import com.battleship.domain.model.game.Game;
import com.battleship.domain.model.game.GameId;

import java.util.Optional;

public interface GameRepository {
    void save(Game game);
    Optional<Game> findById(GameId gameId);
    void delete(GameId gameId);
}

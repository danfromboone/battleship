package com.fleetstrike.infrastructure.persistence;

import com.fleetstrike.domain.model.game.Game;
import com.fleetstrike.domain.model.game.GameId;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryGameRepository implements GameRepository {
    private final Map<GameId, Game> store = new ConcurrentHashMap<>();

    @Override
    public void save(Game game) {
        store.put(game.getId(), game);
    }

    @Override
    public Optional<Game> findById(GameId gameId) {
        return Optional.ofNullable(store.get(gameId));
    }

    @Override
    public void delete(GameId gameId) {
        store.remove(gameId);
    }
}

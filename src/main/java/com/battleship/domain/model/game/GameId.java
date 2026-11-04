package com.battleship.domain.model.game;

import java.util.Objects;
import java.util.UUID;

public final class GameId {
    private final String value;

    public GameId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("GameId cannot be empty");
        }
        this.value = value.trim();
    }

    public static GameId random() {
        return new GameId(UUID.randomUUID().toString());
    }

    public static GameId of(String value) {
        return new GameId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameId gameId = (GameId) o;
        return Objects.equals(value, gameId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

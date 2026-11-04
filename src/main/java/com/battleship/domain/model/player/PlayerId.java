package com.battleship.domain.model.player;

import java.util.Objects;
import java.util.UUID;

public final class PlayerId {
    private final String value;

    public PlayerId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("PlayerId cannot be empty");
        }
        this.value = value.trim();
    }

    public static PlayerId random() {
        return new PlayerId(UUID.randomUUID().toString());
    }

    public static PlayerId of(String value) {
        return new PlayerId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerId playerId = (PlayerId) o;
        return Objects.equals(value, playerId.value);
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

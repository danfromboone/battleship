package com.battleship.domain.event;

import com.battleship.domain.model.coordinate.Coordinate;
import com.battleship.domain.model.player.PlayerId;

import java.time.Instant;

public record ShotFiredEvent(
        PlayerId attackerId,
        PlayerId defenderId,
        Coordinate coordinate,
        Instant occurredOn
) implements DomainEvent {
    public ShotFiredEvent(PlayerId attackerId, PlayerId defenderId, Coordinate coordinate) {
        this(attackerId, defenderId, coordinate, Instant.now());
    }
}

package com.battleship.domain.event;

import com.battleship.domain.model.player.PlayerId;

import java.time.Instant;

public record GameOverEvent(
        PlayerId winnerId,
        PlayerId loserId,
        int totalTurns,
        Instant occurredOn
) implements DomainEvent {
    public GameOverEvent(PlayerId winnerId, PlayerId loserId, int totalTurns) {
        this(winnerId, loserId, totalTurns, Instant.now());
    }
}

package com.battleship.domain.event;

import com.battleship.domain.model.player.PlayerId;
import com.battleship.domain.model.ship.Ship;

import java.time.Instant;

public record ShipPlacedEvent(
        PlayerId playerId,
        Ship ship,
        Instant occurredOn
) implements DomainEvent {
    public ShipPlacedEvent(PlayerId playerId, Ship ship) {
        this(playerId, ship, Instant.now());
    }
}

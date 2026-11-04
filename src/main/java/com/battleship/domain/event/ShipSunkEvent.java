package com.battleship.domain.event;

import com.battleship.domain.model.player.PlayerId;
import com.battleship.domain.model.ship.Ship;

import java.time.Instant;

public record ShipSunkEvent(
        PlayerId defenderId,
        Ship ship,
        Instant occurredOn
) implements DomainEvent {
    public ShipSunkEvent(PlayerId defenderId, Ship ship) {
        this(defenderId, ship, Instant.now());
    }
}

package com.fleetstrike.domain.event;

import com.fleetstrike.domain.model.player.PlayerId;
import com.fleetstrike.domain.model.ship.Ship;

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

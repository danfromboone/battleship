package com.fleetstrike.domain.event;

import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.player.PlayerId;
import com.fleetstrike.domain.model.ship.ShipType;

import java.time.Instant;

public record ShipHitEvent(
        PlayerId defenderId,
        Coordinate coordinate,
        ShipType shipType,
        Instant occurredOn
) implements DomainEvent {
    public ShipHitEvent(PlayerId defenderId, Coordinate coordinate, ShipType shipType) {
        this(defenderId, coordinate, shipType, Instant.now());
    }
}

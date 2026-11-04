package com.battleship.domain.event;

import com.battleship.domain.model.coordinate.Coordinate;
import com.battleship.domain.model.player.PlayerId;
import com.battleship.domain.model.ship.ShipType;

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

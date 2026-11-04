package com.battleship.domain.model.game;

import com.battleship.domain.model.board.ShotResult;
import com.battleship.domain.model.coordinate.Coordinate;
import com.battleship.domain.model.player.PlayerId;

import java.time.Instant;
import java.util.Objects;

public record TurnRecord(
        int turnNumber,
        PlayerId shooterId,
        PlayerId defenderId,
        Coordinate coordinate,
        ShotResult result,
        Instant timestamp
) {
    public TurnRecord(int turnNumber, PlayerId shooterId, PlayerId defenderId, Coordinate coordinate, ShotResult result) {
        this(turnNumber, shooterId, defenderId, coordinate, result, Instant.now());
    }

    public TurnRecord {
        Objects.requireNonNull(shooterId, "shooterId cannot be null");
        Objects.requireNonNull(defenderId, "defenderId cannot be null");
        Objects.requireNonNull(coordinate, "coordinate cannot be null");
        Objects.requireNonNull(result, "result cannot be null");
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
    }
}

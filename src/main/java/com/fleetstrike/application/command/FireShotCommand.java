package com.fleetstrike.application.command;

import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.game.GameId;
import com.fleetstrike.domain.model.player.PlayerId;

public record FireShotCommand(
        GameId gameId,
        PlayerId playerId,
        Coordinate coordinate
) {}

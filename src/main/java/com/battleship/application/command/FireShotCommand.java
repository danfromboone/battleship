package com.battleship.application.command;

import com.battleship.domain.model.coordinate.Coordinate;
import com.battleship.domain.model.game.GameId;
import com.battleship.domain.model.player.PlayerId;

public record FireShotCommand(
        GameId gameId,
        PlayerId playerId,
        Coordinate coordinate
) {}

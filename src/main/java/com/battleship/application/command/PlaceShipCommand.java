package com.battleship.application.command;

import com.battleship.domain.model.coordinate.Coordinate;
import com.battleship.domain.model.coordinate.Orientation;
import com.battleship.domain.model.game.GameId;
import com.battleship.domain.model.player.PlayerId;
import com.battleship.domain.model.ship.ShipType;

public record PlaceShipCommand(
        GameId gameId,
        PlayerId playerId,
        ShipType shipType,
        Coordinate startCoordinate,
        Orientation orientation
) {}

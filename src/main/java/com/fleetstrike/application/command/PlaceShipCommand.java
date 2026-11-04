package com.fleetstrike.application.command;

import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.coordinate.Orientation;
import com.fleetstrike.domain.model.game.GameId;
import com.fleetstrike.domain.model.player.PlayerId;
import com.fleetstrike.domain.model.ship.ShipType;

public record PlaceShipCommand(
        GameId gameId,
        PlayerId playerId,
        ShipType shipType,
        Coordinate startCoordinate,
        Orientation orientation
) {}

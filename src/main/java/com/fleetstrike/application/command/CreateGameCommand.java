package com.fleetstrike.application.command;

import com.fleetstrike.domain.model.player.PlayerType;

public record CreateGameCommand(
        String player1Name,
        PlayerType player1Type,
        String player2Name,
        PlayerType player2Type
) {}

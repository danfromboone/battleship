package com.battleship.application.command;

import com.battleship.domain.model.player.PlayerType;

public record CreateGameCommand(
        String player1Name,
        PlayerType player1Type,
        String player2Name,
        PlayerType player2Type
) {}

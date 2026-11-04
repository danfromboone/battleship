package com.battleship.domain.exception;

public class InvalidGameStateException extends BattleshipException {
    public InvalidGameStateException(String message) {
        super(message);
    }
}

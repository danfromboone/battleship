package com.battleship.domain.exception;

public class BattleshipException extends RuntimeException {
    public BattleshipException(String message) {
        super(message);
    }

    public BattleshipException(String message, Throwable cause) {
        super(message, cause);
    }
}

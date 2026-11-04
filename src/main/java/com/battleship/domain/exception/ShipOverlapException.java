package com.battleship.domain.exception;

public class ShipOverlapException extends BattleshipException {
    public ShipOverlapException(String message) {
        super(message);
    }
}

package com.battleship.domain.exception;

public class CellAlreadyAttackedException extends BattleshipException {
    public CellAlreadyAttackedException(String message) {
        super(message);
    }
}

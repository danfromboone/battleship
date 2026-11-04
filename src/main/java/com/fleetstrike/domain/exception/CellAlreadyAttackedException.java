package com.fleetstrike.domain.exception;

public class CellAlreadyAttackedException extends FleetStrikeException {
    public CellAlreadyAttackedException(String message) {
        super(message);
    }
}

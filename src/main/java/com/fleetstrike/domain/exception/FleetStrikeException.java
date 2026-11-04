package com.fleetstrike.domain.exception;

public class FleetStrikeException extends RuntimeException {
    public FleetStrikeException(String message) {
        super(message);
    }

    public FleetStrikeException(String message, Throwable cause) {
        super(message, cause);
    }
}

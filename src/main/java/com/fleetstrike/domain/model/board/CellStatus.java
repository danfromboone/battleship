package com.fleetstrike.domain.model.board;

public enum CellStatus {
    WATER("~", "Water"),
    SHIP("S", "Ship"),
    MISS("O", "Miss"),
    HIT("X", "Hit"),
    SUNK("#", "Sunk");

    private final String symbol;
    private final String description;

    CellStatus(String symbol, String description) {
        this.symbol = symbol;
        this.description = description;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDescription() {
        return description;
    }
}

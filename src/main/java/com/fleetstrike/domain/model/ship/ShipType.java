package com.fleetstrike.domain.model.ship;

public enum ShipType {
    CARRIER("Carrier", 5, 'C'),
    BATTLESHIP("Battleship", 4, 'B'),
    CRUISER("Cruiser", 3, 'R'),
    SUBMARINE("Submarine", 3, 'S'),
    DESTROYER("Destroyer", 2, 'D');

    private final String displayName;
    private final int size;
    private final char symbol;

    ShipType(String displayName, int size, char symbol) {
        this.displayName = displayName;
        this.size = size;
        this.symbol = symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSize() {
        return size;
    }

    public char getSymbol() {
        return symbol;
    }

    public static ShipType fromDisplayName(String name) {
        if (name == null) return null;
        for (ShipType type : values()) {
            if (type.displayName.equalsIgnoreCase(name.trim()) || type.name().equalsIgnoreCase(name.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ship type: " + name);
    }
}

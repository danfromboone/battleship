package com.battleship.domain.model.coordinate;

public enum Orientation {
    HORIZONTAL(0, 1),
    VERTICAL(1, 0);

    private final int deltaRow;
    private final int deltaCol;

    Orientation(int deltaRow, int deltaCol) {
        this.deltaRow = deltaRow;
        this.deltaCol = deltaCol;
    }

    public int getDeltaRow() {
        return deltaRow;
    }

    public int getDeltaCol() {
        return deltaCol;
    }

    public static Orientation fromString(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Orientation cannot be null");
        }
        String trimmed = input.trim().toUpperCase();
        if (trimmed.equals("H") || trimmed.equals("HORIZONTAL")) {
            return HORIZONTAL;
        }
        if (trimmed.equals("V") || trimmed.equals("VERTICAL")) {
            return VERTICAL;
        }
        throw new IllegalArgumentException("Invalid orientation: '" + input + "'. Expected H or V.");
    }
}

package com.fleetstrike.domain.model.coordinate;

import java.util.Objects;

public final class GridDimension {
    public static final GridDimension STANDARD = new GridDimension(10, 10);

    private final int rows;
    private final int columns;

    public GridDimension(int rows, int columns) {
        if (rows <= 0 || columns <= 0) {
            throw new IllegalArgumentException("Grid dimensions must be positive");
        }
        this.rows = rows;
        this.columns = columns;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public boolean contains(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < columns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GridDimension that = (GridDimension) o;
        return rows == that.rows && columns == that.columns;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rows, columns);
    }

    @Override
    public String toString() {
        return rows + "x" + columns;
    }
}

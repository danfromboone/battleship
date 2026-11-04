package com.battleship.domain.model.coordinate;

import com.battleship.domain.exception.InvalidCoordinateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Coordinate implements Comparable<Coordinate> {
    private static final Pattern LETTER_FIRST = Pattern.compile("^([A-Za-z])(\\d+)$");
    private static final Pattern DIGIT_FIRST = Pattern.compile("^(\\d+)([A-Za-z])$");

    private final int row;
    private final int col;

    public Coordinate(int row, int col) {
        if (row < 0 || col < 0) {
            throw new InvalidCoordinateException("Coordinate row and col must be non-negative: (" + row + ", " + col + ")");
        }
        this.row = row;
        this.col = col;
    }

    public static Coordinate of(int row, int col) {
        return new Coordinate(row, col);
    }

    public static Coordinate parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new InvalidCoordinateException("Coordinate input cannot be empty");
        }
        String clean = input.trim().toUpperCase();

        Matcher mLetter = LETTER_FIRST.matcher(clean);
        if (mLetter.matches()) {
            char colChar = mLetter.group(1).charAt(0);
            int rowNum = Integer.parseInt(mLetter.group(2));
            if (colChar < 'A' || colChar > 'J' || rowNum < 1 || rowNum > 10) {
                throw new InvalidCoordinateException("Coordinate must be between A1 and J10: " + input);
            }
            int col = colChar - 'A';
            int row = rowNum - 1;
            return new Coordinate(row, col);
        }

        Matcher mDigit = DIGIT_FIRST.matcher(clean);
        if (mDigit.matches()) {
            int rowNum = Integer.parseInt(mDigit.group(1));
            char colChar = mDigit.group(2).charAt(0);
            if (colChar < 'A' || colChar > 'J' || rowNum < 1 || rowNum > 10) {
                throw new InvalidCoordinateException("Coordinate must be between A1 and J10: " + input);
            }
            int col = colChar - 'A';
            int row = rowNum - 1;
            return new Coordinate(row, col);
        }

        throw new InvalidCoordinateException("Invalid coordinate format: '" + input + "'. Expected format like 'B5' or 'E10'.");
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public char getColLetter() {
        return (char) ('A' + col);
    }

    public int getRowNumber() {
        return row + 1;
    }

    public String toDisplayString() {
        return "" + getColLetter() + getRowNumber();
    }

    public List<Coordinate> getNeighbors(GridDimension dimension) {
        List<Coordinate> neighbors = new ArrayList<>(4);
        int[][] deltas = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : deltas) {
            int nr = row + d[0];
            int nc = col + d[1];
            if (dimension.contains(nr, nc)) {
                neighbors.add(new Coordinate(nr, nc));
            }
        }
        return Collections.unmodifiableList(neighbors);
    }

    public List<Coordinate> getSequence(Orientation orientation, int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        List<Coordinate> coords = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            coords.add(new Coordinate(
                this.row + (i * orientation.getDeltaRow()),
                this.col + (i * orientation.getDeltaCol())
            ));
        }
        return Collections.unmodifiableList(coords);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public int compareTo(Coordinate o) {
        if (this.row != o.row) {
            return Integer.compare(this.row, o.row);
        }
        return Integer.compare(this.col, o.col);
    }

    @Override
    public String toString() {
        return toDisplayString();
    }
}

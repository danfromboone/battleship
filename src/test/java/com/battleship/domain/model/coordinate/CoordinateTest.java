package com.battleship.domain.model.coordinate;

import com.battleship.domain.exception.InvalidCoordinateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoordinateTest {

    @Test
    @DisplayName("Should create coordinate and return correct 1-based and letter displays")
    void shouldCreateCoordinate() {
        Coordinate coord = Coordinate.of(0, 0);
        assertThat(coord.getRow()).isEqualTo(0);
        assertThat(coord.getCol()).isEqualTo(0);
        assertThat(coord.getRowNumber()).isEqualTo(1);
        assertThat(coord.getColLetter()).isEqualTo('A');
        assertThat(coord.toDisplayString()).isEqualTo("A1");
    }

    @ParameterizedTest
    @CsvSource({
            "A1, 0, 0",
            "a1, 0, 0",
            "B4, 3, 1",
            "J10, 9, 9",
            "j10, 9, 9",
            "1A, 0, 0",
            "10J, 9, 9"
    })
    @DisplayName("Should parse valid coordinate strings correctly")
    void shouldParseValidCoordinates(String input, int expectedRow, int expectedCol) {
        Coordinate coord = Coordinate.parse(input);
        assertThat(coord.getRow()).isEqualTo(expectedRow);
        assertThat(coord.getCol()).isEqualTo(expectedCol);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "ZZ", "123", "K1", "A0", "A-1", "XYZ"})
    @DisplayName("Should reject invalid coordinate string formats")
    void shouldRejectInvalidCoordinates(String input) {
        assertThatThrownBy(() -> Coordinate.parse(input))
                .isInstanceOf(InvalidCoordinateException.class);
    }

    @Test
    @DisplayName("Should compute orthogonal neighbors within bounds")
    void shouldComputeNeighbors() {
        GridDimension dim = GridDimension.STANDARD; // 10x10

        // Corner (0,0) has 2 neighbors: (1,0) and (0,1)
        Coordinate corner = Coordinate.of(0, 0);
        List<Coordinate> cornerNeighbors = corner.getNeighbors(dim);
        assertThat(cornerNeighbors).containsExactlyInAnyOrder(
                Coordinate.of(1, 0),
                Coordinate.of(0, 1)
        );

        // Center (5,5) has 4 neighbors
        Coordinate center = Coordinate.of(5, 5);
        List<Coordinate> centerNeighbors = center.getNeighbors(dim);
        assertThat(centerNeighbors).containsExactlyInAnyOrder(
                Coordinate.of(4, 5),
                Coordinate.of(6, 5),
                Coordinate.of(5, 4),
                Coordinate.of(5, 6)
        );
    }

    @Test
    @DisplayName("Should compute sequence for horizontal and vertical orientations")
    void shouldComputeSequence() {
        Coordinate start = Coordinate.of(2, 2); // C3

        List<Coordinate> horizontalSeq = start.getSequence(Orientation.HORIZONTAL, 3);
        assertThat(horizontalSeq).containsExactly(
                Coordinate.of(2, 2),
                Coordinate.of(2, 3),
                Coordinate.of(2, 4)
        );

        List<Coordinate> verticalSeq = start.getSequence(Orientation.VERTICAL, 3);
        assertThat(verticalSeq).containsExactly(
                Coordinate.of(2, 2),
                Coordinate.of(3, 2),
                Coordinate.of(4, 2)
        );
    }

    @Test
    @DisplayName("Coordinate value object should obey equals and hashcode")
    void shouldObeyValueObjectEquality() {
        Coordinate c1 = Coordinate.of(4, 7);
        Coordinate c2 = Coordinate.parse("H5");
        assertThat(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }
}

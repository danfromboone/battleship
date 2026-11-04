package com.fleetstrike.presentation.swing;

import com.fleetstrike.domain.model.board.OceanBoard;
import com.fleetstrike.domain.model.board.TargetBoard;
import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.coordinate.Orientation;
import com.fleetstrike.domain.model.ship.ShipPlacement;
import com.fleetstrike.domain.model.ship.ShipType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class BoardGridPanelTest {

    private BoardGridPanel gridPanel;

    @BeforeEach
    void setUp() {
        gridPanel = new BoardGridPanel();
    }

    @Test
    @DisplayName("Should correctly map pixel coordinates to board Coordinates")
    void shouldMapPixelCoordinatesToBoardCoordinate() {
        // Header offset is 28, cell size is 34
        // (x=28, y=28) is top-left cell: row 0, col 0 (A1)
        Coordinate c00 = gridPanel.pointToCoordinate(30, 30);
        assertThat(c00).isNotNull();
        assertThat(c00.getRow()).isEqualTo(0);
        assertThat(c00.getCol()).isEqualTo(0);

        // (x=28 + 9*34 + 5, y=28 + 9*34 + 5) is bottom-right cell: row 9, col 9 (J10)
        Coordinate c99 = gridPanel.pointToCoordinate(28 + (9 * 34) + 10, 28 + (9 * 34) + 10);
        assertThat(c99).isNotNull();
        assertThat(c99.getRow()).isEqualTo(9);
        assertThat(c99.getCol()).isEqualTo(9);

        // Out of bounds (e.g. inside header region or outside grid)
        assertThat(gridPanel.pointToCoordinate(10, 10)).isNull();
        assertThat(gridPanel.pointToCoordinate(-5, 50)).isNull();
        assertThat(gridPanel.pointToCoordinate(600, 600)).isNull();
    }

    @Test
    @DisplayName("Should correctly calculate top-left point for a coordinate")
    void shouldCalculateTopLeftPoint() {
        Point p00 = gridPanel.coordinateToTopLeft(Coordinate.of(0, 0));
        assertThat(p00.x).isEqualTo(28);
        assertThat(p00.y).isEqualTo(28);

        Point p12 = gridPanel.coordinateToTopLeft(Coordinate.of(1, 2));
        assertThat(p12.x).isEqualTo(28 + (2 * 34));
        assertThat(p12.y).isEqualTo(28 + (1 * 34));
    }

    @Test
    @DisplayName("Should support setting ocean board and target board")
    void shouldSetBoardsAndModes() {
        OceanBoard oceanBoard = new OceanBoard();
        oceanBoard.placeShip(new ShipPlacement(ShipType.DESTROYER, Coordinate.of(0, 0), Orientation.HORIZONTAL));

        gridPanel.setOceanBoard(oceanBoard);
        gridPanel.setMode(BoardGridPanel.GridMode.INTERACTIVE_PLACEMENT);
        assertThat(gridPanel.getMode()).isEqualTo(BoardGridPanel.GridMode.INTERACTIVE_PLACEMENT);

        TargetBoard targetBoard = new TargetBoard();
        gridPanel.setTargetBoard(targetBoard);
        gridPanel.setMode(BoardGridPanel.GridMode.INTERACTIVE_TARGET);
        assertThat(gridPanel.getMode()).isEqualTo(BoardGridPanel.GridMode.INTERACTIVE_TARGET);
    }

    @Test
    @DisplayName("Should invoke cell click listener when clicked")
    void shouldDispatchCellClick() {
        AtomicReference<Coordinate> clickedCoord = new AtomicReference<>();
        gridPanel.setCellClickListener((coord, event) -> clickedCoord.set(coord));

        // Synthesize click at cell (2, 3) -> x = 28 + 3*34 + 5 = 135, y = 28 + 2*34 + 5 = 101
        int clickX = 28 + (3 * 34) + 5;
        int clickY = 28 + (2 * 34) + 5;
        MouseEvent me = new MouseEvent(gridPanel, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                0, clickX, clickY, 1, false, MouseEvent.BUTTON1);

        for (var listener : gridPanel.getMouseListeners()) {
            listener.mouseClicked(me);
        }

        assertThat(clickedCoord.get()).isEqualTo(Coordinate.of(2, 3));
    }
}

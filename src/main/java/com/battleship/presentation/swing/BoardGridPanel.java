package com.battleship.presentation.swing;

import com.battleship.domain.model.board.CellStatus;
import com.battleship.domain.model.board.OceanBoard;
import com.battleship.domain.model.board.TargetBoard;
import com.battleship.domain.model.coordinate.Coordinate;
import com.battleship.domain.model.coordinate.Orientation;
import com.battleship.domain.model.ship.ShipPlacement;
import com.battleship.domain.model.ship.ShipType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * High-definition Swing canvas component for rendering Battleship 10x10 boards
 * with interactive hover previews, radar reticles, and custom graphics.
 */
public class BoardGridPanel extends JPanel {

    public enum GridMode {
        DISPLAY_ONLY,
        INTERACTIVE_TARGET,
        INTERACTIVE_PLACEMENT
    }

    public interface CellClickListener {
        void onCellClicked(Coordinate coordinate, MouseEvent event);
    }

    public interface CellHoverListener {
        void onCellHovered(Coordinate coordinate);
    }

    private static final int GRID_SIZE = 10;
    private static final int HEADER_OFFSET = 28;
    private static final int CELL_SIZE = 34;

    private GridMode mode = GridMode.DISPLAY_ONLY;
    private OceanBoard oceanBoard;
    private TargetBoard targetBoard;

    // Placement preview state
    private ShipType previewShipType;
    private Orientation previewOrientation = Orientation.HORIZONTAL;
    private BiPredicate<Coordinate, Orientation> placementValidator;

    // Hover state
    private Coordinate hoveredCoord = null;

    // Listeners
    private CellClickListener cellClickListener;
    private CellHoverListener cellHoverListener;

    public BoardGridPanel() {
        int totalWidth = HEADER_OFFSET + (GRID_SIZE * CELL_SIZE) + 4;
        int totalHeight = HEADER_OFFSET + (GRID_SIZE * CELL_SIZE) + 4;
        Dimension dim = new Dimension(totalWidth, totalHeight);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setBackground(SwingTheme.BG_PANEL);

        setupMouseListeners();
    }

    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Coordinate coord = pointToCoordinate(e.getX(), e.getY());
                if (!Objects.equals(coord, hoveredCoord)) {
                    hoveredCoord = coord;
                    if (cellHoverListener != null) {
                        cellHoverListener.onCellHovered(coord);
                    }
                    if (coord != null) {
                        setToolTipText(coord.toString());
                    } else {
                        setToolTipText(null);
                    }
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredCoord = null;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                Coordinate coord = pointToCoordinate(e.getX(), e.getY());
                if (coord != null && cellClickListener != null) {
                    cellClickListener.onCellClicked(coord, e);
                }
            }
        });
    }

    public Coordinate pointToCoordinate(int x, int y) {
        if (x < HEADER_OFFSET || y < HEADER_OFFSET) {
            return null;
        }
        int col = (x - HEADER_OFFSET) / CELL_SIZE;
        int row = (y - HEADER_OFFSET) / CELL_SIZE;
        if (row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE) {
            return Coordinate.of(row, col);
        }
        return null;
    }

    public Point coordinateToTopLeft(Coordinate coord) {
        int x = HEADER_OFFSET + (coord.getCol() * CELL_SIZE);
        int y = HEADER_OFFSET + (coord.getRow() * CELL_SIZE);
        return new Point(x, y);
    }

    public void setOceanBoard(OceanBoard oceanBoard) {
        this.oceanBoard = oceanBoard;
        this.targetBoard = null;
        repaint();
    }

    public void setTargetBoard(TargetBoard targetBoard) {
        this.targetBoard = targetBoard;
        this.oceanBoard = null;
        repaint();
    }

    public void setMode(GridMode mode) {
        this.mode = mode;
        repaint();
    }

    public GridMode getMode() {
        return mode;
    }

    public void setPlacementPreview(ShipType shipType, Orientation orientation,
                                   BiPredicate<Coordinate, Orientation> placementValidator) {
        this.previewShipType = shipType;
        this.previewOrientation = orientation;
        this.placementValidator = placementValidator;
        repaint();
    }

    public void clearPlacementPreview() {
        this.previewShipType = null;
        repaint();
    }

    public void setCellClickListener(CellClickListener listener) {
        this.cellClickListener = listener;
    }

    public void setCellHoverListener(CellHoverListener listener) {
        this.cellHoverListener = listener;
    }

    public Coordinate getHoveredCoord() {
        return hoveredCoord;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawHeaders(g2);
        drawGridCells(g2);

        if (mode == GridMode.INTERACTIVE_PLACEMENT && previewShipType != null && hoveredCoord != null) {
            drawPlacementPreview(g2);
        } else if (mode == GridMode.INTERACTIVE_TARGET && hoveredCoord != null) {
            drawTargetReticle(g2);
        }

        g2.dispose();
    }

    private void drawHeaders(Graphics2D g2) {
        g2.setFont(SwingTheme.FONT_GRID);
        g2.setColor(SwingTheme.TEXT_MUTED);

        FontMetrics fm = g2.getFontMetrics();

        // Column headers: A to J
        for (int col = 0; col < GRID_SIZE; col++) {
            String colLabel = String.valueOf((char) ('A' + col));
            int x = HEADER_OFFSET + (col * CELL_SIZE) + (CELL_SIZE - fm.stringWidth(colLabel)) / 2;
            int y = (HEADER_OFFSET + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(colLabel, x, y);
        }

        // Row headers: 1 to 10
        for (int row = 0; row < GRID_SIZE; row++) {
            String rowLabel = String.valueOf(row + 1);
            int x = (HEADER_OFFSET - fm.stringWidth(rowLabel)) / 2;
            int y = HEADER_OFFSET + (row * CELL_SIZE) + (CELL_SIZE + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(rowLabel, x, y);
        }
    }

    private void drawGridCells(Graphics2D g2) {
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                Coordinate coord = Coordinate.of(r, c);
                int x = HEADER_OFFSET + (c * CELL_SIZE);
                int y = HEADER_OFFSET + (r * CELL_SIZE);

                CellStatus status = getCellStatus(coord);
                paintCell(g2, coord, status, x, y, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    private CellStatus getCellStatus(Coordinate coord) {
        if (oceanBoard != null) {
            return oceanBoard.getCellStatus(coord);
        } else if (targetBoard != null) {
            return targetBoard.getCellStatus(coord);
        }
        return CellStatus.WATER;
    }

    private void paintCell(Graphics2D g2, Coordinate coord, CellStatus status,
                           int x, int y, int width, int height) {
        // Base cell background
        switch (status) {
            case WATER -> {
                g2.setColor(SwingTheme.CELL_WATER);
                g2.fillRect(x, y, width - 1, height - 1);
                // Subtle radar water texture
                g2.setColor(new Color(255, 255, 255, 8));
                g2.fillOval(x + width / 4, y + height / 4, width / 2, height / 2);
            }
            case SHIP -> {
                g2.setColor(SwingTheme.CELL_SHIP);
                g2.fillRoundRect(x + 1, y + 1, width - 3, height - 3, 6, 6);
                g2.setColor(SwingTheme.CELL_SHIP_BORDER);
                g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 6, 6);
                // Inner rivet
                g2.setColor(new Color(255, 255, 255, 60));
                g2.fillOval(x + width / 2 - 2, y + height / 2 - 2, 4, 4);
            }
            case HIT -> {
                g2.setColor(SwingTheme.CELL_HIT);
                g2.fillRoundRect(x + 1, y + 1, width - 3, height - 3, 4, 4);
                // Fiery cross marker
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawLine(x + 7, y + 7, x + width - 8, y + height - 8);
                g2.drawLine(x + width - 8, y + 7, x + 7, y + height - 8);
            }
            case MISS -> {
                g2.setColor(SwingTheme.CELL_WATER);
                g2.fillRect(x, y, width - 1, height - 1);
                // Water splash ring
                g2.setColor(SwingTheme.CELL_MISS);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawOval(x + width / 4, y + height / 4, width / 2, height / 2);
                g2.setColor(new Color(255, 255, 255, 140));
                g2.fillOval(x + width / 2 - 2, y + height / 2 - 2, 4, 4);
            }
            case SUNK -> {
                g2.setColor(SwingTheme.CELL_SUNK);
                g2.fillRoundRect(x + 1, y + 1, width - 3, height - 3, 4, 4);
                g2.setColor(SwingTheme.COLOR_GOLD);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawLine(x + 6, y + 6, x + width - 7, y + height - 7);
                g2.drawLine(x + width - 7, y + 6, x + 6, y + height - 7);
                g2.drawRect(x + 4, y + 4, width - 9, height - 9);
            }
        }

        // Cell border
        g2.setColor(SwingTheme.CELL_BORDER);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRect(x, y, width - 1, height - 1);
    }

    private void drawPlacementPreview(Graphics2D g2) {
        if (hoveredCoord == null || previewShipType == null) return;

        int size = previewShipType.getSize();
        boolean isValid = placementValidator != null && placementValidator.test(hoveredCoord, previewOrientation);
        Color previewColor = isValid ? SwingTheme.PREVIEW_VALID : SwingTheme.PREVIEW_INVALID;

        List<Coordinate> coords = new ArrayList<>();
        int deltaRow = previewOrientation.getDeltaRow();
        int deltaCol = previewOrientation.getDeltaCol();

        for (int i = 0; i < size; i++) {
            int r = hoveredCoord.getRow() + (i * deltaRow);
            int c = hoveredCoord.getCol() + (i * deltaCol);
            if (r >= 0 && r < GRID_SIZE && c >= 0 && c < GRID_SIZE) {
                coords.add(Coordinate.of(r, c));
            }
        }

        g2.setColor(previewColor);
        for (Coordinate c : coords) {
            int x = HEADER_OFFSET + (c.getCol() * CELL_SIZE);
            int y = HEADER_OFFSET + (c.getRow() * CELL_SIZE);
            g2.fillRoundRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2, 6, 6);
        }
    }

    private void drawTargetReticle(Graphics2D g2) {
        if (hoveredCoord == null) return;

        // If cell has already been attacked, show subtle invalid marker
        boolean alreadyAttacked = false;
        if (targetBoard != null && targetBoard.hasBeenAttacked(hoveredCoord)) {
            alreadyAttacked = true;
        }

        int x = HEADER_OFFSET + (hoveredCoord.getCol() * CELL_SIZE);
        int y = HEADER_OFFSET + (hoveredCoord.getRow() * CELL_SIZE);

        if (alreadyAttacked) {
            g2.setColor(new Color(239, 68, 68, 80));
            g2.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
            return;
        }

        // Glowing radar reticle
        g2.setColor(SwingTheme.RADAR_RETICLE);
        g2.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);

        g2.setColor(SwingTheme.TEXT_ACCENT);
        g2.setStroke(new BasicStroke(1.5f));
        int cx = x + CELL_SIZE / 2;
        int cy = y + CELL_SIZE / 2;
        int r = (CELL_SIZE / 2) - 4;
        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        g2.drawLine(cx, y + 2, cx, y + CELL_SIZE - 3);
        g2.drawLine(x + 2, cy, x + CELL_SIZE - 3, cy);
    }
}

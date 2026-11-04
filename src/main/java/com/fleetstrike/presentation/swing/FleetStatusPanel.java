package com.fleetstrike.presentation.swing;

import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.player.Player;
import com.fleetstrike.domain.model.ship.Fleet;
import com.fleetstrike.domain.model.ship.Ship;
import com.fleetstrike.domain.model.ship.ShipType;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;
import java.util.Optional;

/**
 * Visual fleet dashboard showing each ship's health, damaged segments, and operational status.
 */
public class FleetStatusPanel extends JPanel {

    public enum ViewType {
        OWN_FLEET,      // Full visibility into ship damage segments
        ENEMY_RADAR     // Visibility only into sunk status vs afloat
    }

    private final ViewType viewType;
    private final JLabel titleLabel;
    private final JLabel subtitleLabel;
    private final JPanel shipsContainer;

    private static final List<ShipType> ORDERED_SHIPS = List.of(
            ShipType.CARRIER,
            ShipType.BATTLESHIP,
            ShipType.CRUISER,
            ShipType.SUBMARINE,
            ShipType.DESTROYER
    );

    public FleetStatusPanel(String title, ViewType viewType) {
        this.viewType = viewType;
        setLayout(new BorderLayout(0, 8));
        setBackground(SwingTheme.BG_PANEL);
        setBorder(new CompoundBorder(
                new LineBorder(SwingTheme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        headerPanel.setOpaque(false);

        titleLabel = new JLabel(title);
        titleLabel.setFont(SwingTheme.FONT_SUBHEADER);
        titleLabel.setForeground(SwingTheme.TEXT_PRIMARY);

        subtitleLabel = new JLabel("Fleet Status: Ready");
        subtitleLabel.setFont(SwingTheme.FONT_SMALL);
        subtitleLabel.setForeground(SwingTheme.TEXT_MUTED);

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Container for ship health rows
        shipsContainer = new JPanel();
        shipsContainer.setLayout(new BoxLayout(shipsContainer, BoxLayout.Y_AXIS));
        shipsContainer.setOpaque(false);
        add(shipsContainer, BorderLayout.CENTER);

        renderEmptyFleet();
    }

    private void renderEmptyFleet() {
        shipsContainer.removeAll();
        for (ShipType type : ORDERED_SHIPS) {
            shipsContainer.add(createShipRow(type, false, 0));
            shipsContainer.add(Box.createVerticalStrut(4));
        }
        shipsContainer.revalidate();
        shipsContainer.repaint();
    }

    /**
     * Updates the fleet status based on a Player model.
     */
    public void updateStatus(Player player) {
        if (player == null) return;
        Fleet fleet = player.getFleet();
        int afloat = fleet.getAfloatCount();
        int total = fleet.getShips().size();

        titleLabel.setText(player.getName() + (viewType == ViewType.OWN_FLEET ? " (Your Fleet)" : " (Enemy Radar)"));
        subtitleLabel.setText(afloat + " / " + (total > 0 ? total : 5) + " ships operational");

        shipsContainer.removeAll();
        for (ShipType type : ORDERED_SHIPS) {
            Optional<Ship> shipOpt = fleet.getShip(type);
            boolean isSunk = shipOpt.map(Ship::isSunk).orElse(false);
            int hits = 0;
            if (shipOpt.isPresent()) {
                hits = shipOpt.get().getHitCoordinates().size();
            }

            if (viewType == ViewType.ENEMY_RADAR) {
                // If view is enemy radar, hits are only shown as intact until sunk, or if sunk all hit
                shipsContainer.add(createShipRow(type, isSunk, isSunk ? type.getSize() : 0));
            } else {
                shipsContainer.add(createShipRow(type, isSunk, hits));
            }
            shipsContainer.add(Box.createVerticalStrut(4));
        }

        shipsContainer.revalidate();
        shipsContainer.repaint();
    }

    private JPanel createShipRow(ShipType type, boolean isSunk, int hits) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        // Ship Name
        JLabel nameLabel = new JLabel(type.getDisplayName() + " (" + type.getSize() + ")");
        nameLabel.setFont(SwingTheme.FONT_BODY);
        nameLabel.setForeground(isSunk ? SwingTheme.TEXT_MUTED : SwingTheme.TEXT_PRIMARY);
        nameLabel.setPreferredSize(new Dimension(105, 18));
        row.add(nameLabel, BorderLayout.WEST);

        // Mini health segments
        JPanel segmentsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        segmentsPanel.setOpaque(false);

        for (int i = 0; i < type.getSize(); i++) {
            boolean segmentHit = (i < hits);
            JPanel segment = new JPanel();
            segment.setPreferredSize(new Dimension(13, 13));
            if (isSunk) {
                segment.setBackground(SwingTheme.CELL_SUNK);
                segment.setBorder(new LineBorder(SwingTheme.COLOR_GOLD, 1));
            } else if (segmentHit) {
                segment.setBackground(SwingTheme.CELL_HIT);
                segment.setBorder(new LineBorder(Color.WHITE, 1));
            } else {
                segment.setBackground(SwingTheme.CELL_SHIP);
                segment.setBorder(new LineBorder(SwingTheme.CELL_SHIP_BORDER, 1));
            }
            segmentsPanel.add(segment);
        }
        row.add(segmentsPanel, BorderLayout.CENTER);

        // Status badge
        JLabel badge = new JLabel(isSunk ? "SUNK" : "AFLOAT");
        badge.setFont(SwingTheme.FONT_GRID);
        badge.setForeground(isSunk ? SwingTheme.TEXT_DANGER : SwingTheme.TEXT_SUCCESS);
        badge.setHorizontalAlignment(SwingConstants.RIGHT);
        badge.setPreferredSize(new Dimension(55, 18));
        row.add(badge, BorderLayout.EAST);

        return row;
    }
}

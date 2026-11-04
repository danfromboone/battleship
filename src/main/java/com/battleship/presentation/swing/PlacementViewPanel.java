package com.battleship.presentation.swing;

import com.battleship.application.BattleshipApplicationService;
import com.battleship.application.command.PlaceShipCommand;
import com.battleship.domain.exception.BattleshipException;
import com.battleship.domain.model.coordinate.Coordinate;
import com.battleship.domain.model.coordinate.GridDimension;
import com.battleship.domain.model.coordinate.Orientation;
import com.battleship.domain.model.game.Game;
import com.battleship.domain.model.player.Player;
import com.battleship.domain.model.ship.Fleet;
import com.battleship.domain.model.ship.ShipPlacement;
import com.battleship.domain.model.ship.ShipType;
import com.battleship.domain.policy.FleetPlacementService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Interactive fleet deployment panel with mouse hover placement preview,
 * rotation controls (Right-click or 'R' key), auto-deployment, and validation.
 */
public class PlacementViewPanel extends JPanel {

    public interface PlacementCompleteListener {
        void onPlacementComplete(Player player);
    }

    public interface BackToMenuListener {
        void onBackToMenu();
    }

    private final BattleshipApplicationService service;
    private final FleetPlacementService fleetPlacementService = new FleetPlacementService();

    private Game game;
    private Player player;

    private final JLabel titleLabel = new JLabel("Commander Fleet Deployment");
    private final JLabel statusLabel = new JLabel("Select a ship and click the grid to deploy.");
    private final BoardGridPanel gridPanel = new BoardGridPanel();

    private final JButton rotateBtn = SwingTheme.createButton("Orientation: Horizontal [H]", SwingTheme.BG_ELEVATED, SwingTheme.TEXT_PRIMARY);
    private final JButton autoDeployBtn = SwingTheme.createButton("Auto Deploy (Random)", SwingTheme.BG_ELEVATED, SwingTheme.TEXT_ACCENT);
    private final JButton resetBtn = SwingTheme.createButton("Reset Fleet", SwingTheme.BG_ELEVATED, SwingTheme.TEXT_DANGER);
    private final JButton confirmBtn = SwingTheme.createButton("CONFIRM & READY", SwingTheme.BTN_SUCCESS, Color.WHITE);
    private final JButton backBtn = SwingTheme.createButton("Back to Menu", SwingTheme.BG_ELEVATED, SwingTheme.TEXT_MUTED);

    private final Map<ShipType, JButton> shipButtons = new EnumMap<>(ShipType.class);

    private Orientation currentOrientation = Orientation.HORIZONTAL;
    private ShipType selectedShip = ShipType.CARRIER;

    private PlacementCompleteListener completeListener;
    private BackToMenuListener backListener;

    public PlacementViewPanel(BattleshipApplicationService service) {
        this.service = service;

        setLayout(new BorderLayout(16, 16));
        setBackground(SwingTheme.BG_DARK);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setOpaque(false);

        titleLabel.setFont(SwingTheme.FONT_TITLE);
        titleLabel.setForeground(SwingTheme.TEXT_PRIMARY);

        statusLabel.setFont(SwingTheme.FONT_SUBHEADER);
        statusLabel.setForeground(SwingTheme.TEXT_ACCENT);

        headerPanel.add(titleLabel);
        headerPanel.add(statusLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Center: Grid & Control sidebar
        JPanel centerPanel = new JPanel(new BorderLayout(24, 0));
        centerPanel.setOpaque(false);

        // Grid container
        JPanel gridWrapper = SwingTheme.createCardPanel();
        gridWrapper.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        gridWrapper.add(gridPanel);
        centerPanel.add(gridWrapper, BorderLayout.CENTER);

        // Right control sidebar
        JPanel sidebar = createSidebar();
        centerPanel.add(sidebar, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.add(backBtn, BorderLayout.WEST);
        bottomBar.add(confirmBtn, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);

        setupInteractions();
    }

    private JPanel createSidebar() {
        JPanel sidebar = SwingTheme.createCardPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(280, 400));

        JLabel shipsHeader = new JLabel("AVAILABLE FLEET");
        shipsHeader.setFont(SwingTheme.FONT_HEADER);
        shipsHeader.setForeground(SwingTheme.TEXT_PRIMARY);
        sidebar.add(shipsHeader);
        sidebar.add(Box.createVerticalStrut(10));

        for (ShipType type : Fleet.REQUIRED_SHIP_TYPES) {
            JButton btn = SwingTheme.createButton(
                    type.getDisplayName() + " (Size " + type.getSize() + ")",
                    SwingTheme.BG_PANEL_ALT,
                    SwingTheme.TEXT_PRIMARY
            );
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(260, 32));
            btn.addActionListener(e -> selectShip(type));
            shipButtons.put(type, btn);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(6));
        }

        sidebar.add(Box.createVerticalStrut(12));
        JSeparator sep = new JSeparator();
        sep.setForeground(SwingTheme.BORDER_COLOR);
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(12));

        // Controls
        rotateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        rotateBtn.setMaximumSize(new Dimension(260, 34));
        rotateBtn.addActionListener(e -> toggleOrientation());
        sidebar.add(rotateBtn);
        sidebar.add(Box.createVerticalStrut(8));

        autoDeployBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        autoDeployBtn.setMaximumSize(new Dimension(260, 34));
        autoDeployBtn.addActionListener(e -> autoDeploy());
        sidebar.add(autoDeployBtn);
        sidebar.add(Box.createVerticalStrut(8));

        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetBtn.setMaximumSize(new Dimension(260, 34));
        resetBtn.addActionListener(e -> resetFleet());
        sidebar.add(resetBtn);

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private void setupInteractions() {
        gridPanel.setMode(BoardGridPanel.GridMode.INTERACTIVE_PLACEMENT);

        gridPanel.setCellClickListener((coord, event) -> {
            if (SwingUtilities.isRightMouseButton(event)) {
                toggleOrientation();
            } else if (SwingUtilities.isLeftMouseButton(event)) {
                attemptPlaceShip(coord);
            }
        });

        // Key binding 'R' for rotate
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_R || e.getKeyCode() == KeyEvent.VK_SPACE) {
                    toggleOrientation();
                }
            }
        });

        confirmBtn.setEnabled(false);
        confirmBtn.addActionListener(e -> {
            if (player != null && player.isFleetDeployed() && completeListener != null) {
                completeListener.onPlacementComplete(player);
            }
        });

        backBtn.addActionListener(e -> {
            if (backListener != null) {
                backListener.onBackToMenu();
            }
        });
    }

    public void setup(Game game, Player player) {
        this.game = game;
        this.player = player;
        this.currentOrientation = Orientation.HORIZONTAL;

        titleLabel.setText("Fleet Deployment - " + player.getName());
        statusLabel.setText("Place your ships or use Auto Deploy. Press 'R' to rotate.");
        gridPanel.setOceanBoard(player.getOceanBoard());

        updateShipButtons();
        selectFirstAvailableShip();
        updatePreview();
        requestFocusInWindow();
    }

    private void toggleOrientation() {
        currentOrientation = (currentOrientation == Orientation.HORIZONTAL)
                ? Orientation.VERTICAL
                : Orientation.HORIZONTAL;
        rotateBtn.setText("Orientation: " + currentOrientation.name().substring(0, 1).toUpperCase()
                + currentOrientation.name().substring(1).toLowerCase() + " [" + currentOrientation.name().charAt(0) + "]");
        updatePreview();
    }

    private void selectShip(ShipType type) {
        this.selectedShip = type;
        updatePreview();
        updateShipButtons();
    }

    private void selectFirstAvailableShip() {
        if (player == null) return;
        Fleet fleet = player.getFleet();
        for (ShipType type : Fleet.REQUIRED_SHIP_TYPES) {
            if (!fleet.hasShip(type)) {
                selectShip(type);
                return;
            }
        }
        selectedShip = null;
        updatePreview();
    }

    private void updatePreview() {
        if (selectedShip == null || (player != null && player.getFleet().hasShip(selectedShip))) {
            gridPanel.clearPlacementPreview();
        } else {
            gridPanel.setPlacementPreview(selectedShip, currentOrientation, (coord, orient) -> {
                if (player == null) return false;
                try {
                    ShipPlacement testPlacement = new ShipPlacement(selectedShip, coord, orient);
                    return fleetPlacementService.canPlace(player.getOceanBoard(), testPlacement);
                } catch (Exception e) {
                    return false;
                }
            });
        }
    }

    private void attemptPlaceShip(Coordinate startCoord) {
        if (selectedShip == null || player == null || game == null) return;

        if (player.getFleet().hasShip(selectedShip)) {
            statusLabel.setText(selectedShip.getDisplayName() + " is already placed.");
            return;
        }

        try {
            ShipPlacement placement = new ShipPlacement(selectedShip, startCoord, currentOrientation);
            if (!fleetPlacementService.canPlace(player.getOceanBoard(), placement)) {
                statusLabel.setText("Cannot place " + selectedShip.getDisplayName() + " here (overlap or out of bounds).");
                return;
            }

            service.placeShip(new PlaceShipCommand(game.getId(), player.getId(), selectedShip, startCoord, currentOrientation));
            statusLabel.setText(selectedShip.getDisplayName() + " placed successfully!");

            updateShipButtons();
            selectFirstAvailableShip();
            checkFleetComplete();
            gridPanel.repaint();

        } catch (BattleshipException | IllegalArgumentException e) {
            statusLabel.setText(e.getMessage());
        }
    }

    private void autoDeploy() {
        if (game == null || player == null) return;

        // If fleet is partially placed, reset first then auto-deploy all
        if (!player.getFleet().getShips().isEmpty()) {
            resetFleet();
        }

        service.autoDeployFleet(game.getId(), player.getId());
        statusLabel.setText("Fleet deployed automatically!");
        updateShipButtons();
        selectedShip = null;
        updatePreview();
        checkFleetComplete();
        gridPanel.repaint();
    }

    private void resetFleet() {
        if (player == null) return;
        player.getOceanBoard().clear();
        statusLabel.setText("Fleet cleared. Deploy your ships.");
        updateShipButtons();
        selectFirstAvailableShip();
        checkFleetComplete();
        gridPanel.repaint();
    }

    private void updateShipButtons() {
        if (player == null) return;
        Fleet fleet = player.getFleet();

        for (Map.Entry<ShipType, JButton> entry : shipButtons.entrySet()) {
            ShipType type = entry.getKey();
            JButton btn = entry.getValue();
            boolean placed = fleet.hasShip(type);

            if (placed) {
                btn.setText("✓ " + type.getDisplayName() + " (Deployed)");
                btn.setBackground(SwingTheme.BG_ELEVATED);
                btn.setForeground(SwingTheme.TEXT_MUTED);
            } else if (type == selectedShip) {
                btn.setText("► " + type.getDisplayName() + " (Size " + type.getSize() + ")");
                btn.setBackground(SwingTheme.BTN_PRIMARY);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setText(type.getDisplayName() + " (Size " + type.getSize() + ")");
                btn.setBackground(SwingTheme.BG_PANEL_ALT);
                btn.setForeground(SwingTheme.TEXT_PRIMARY);
            }
        }
    }

    private void checkFleetComplete() {
        boolean complete = player != null && player.isFleetDeployed();
        confirmBtn.setEnabled(complete);
        if (complete) {
            statusLabel.setText("All 5 warships deployed! Click 'CONFIRM & READY' to proceed.");
        }
    }

    public void setPlacementCompleteListener(PlacementCompleteListener listener) {
        this.completeListener = listener;
    }

    public void setBackToMenuListener(BackToMenuListener listener) {
        this.backListener = listener;
    }
}

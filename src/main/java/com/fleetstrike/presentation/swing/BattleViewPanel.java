package com.fleetstrike.presentation.swing;

import com.fleetstrike.application.FleetStrikeApplicationService;
import com.fleetstrike.application.command.FireShotCommand;
import com.fleetstrike.domain.exception.FleetStrikeException;
import com.fleetstrike.domain.model.board.ShotResult;
import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.game.Game;
import com.fleetstrike.domain.model.game.GameState;
import com.fleetstrike.domain.model.player.Player;
import com.fleetstrike.domain.model.player.PlayerId;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Objects;

/**
 * Main battle panel hosting dual interactive boards (Radar Offense vs Ocean Defense),
 * real-time fleet health bars, combat log, AI turn timers, and simulation controls.
 */
public class BattleViewPanel extends JPanel {

    public interface BattleListener {
        void onGameOver(Game game);
        void onSurrender(Player player);
        void onNewGameRequested();
    }

    private final FleetStrikeApplicationService service;

    private Game game;
    private boolean isSimulationMode = false;
    private boolean isHotseatMode = false;

    // Header Components
    private final JLabel turnBanner = new JLabel("BATTLE IN PROGRESS");
    private final JLabel statusMessage = new JLabel("Select coordinates to fire.");
    private final JLabel lastResultBadge = new JLabel(" ");

    // Grids
    private final BoardGridPanel targetGrid = new BoardGridPanel();
    private final BoardGridPanel oceanGrid = new BoardGridPanel();

    // Dashboards
    private final FleetStatusPanel ownFleetStatus = new FleetStatusPanel("Your Fleet", FleetStatusPanel.ViewType.OWN_FLEET);
    private final FleetStatusPanel enemyFleetStatus = new FleetStatusPanel("Enemy Radar", FleetStatusPanel.ViewType.ENEMY_RADAR);
    private final CombatLogPanel combatLog = new CombatLogPanel();

    // Simulation Controls
    private final JPanel simControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));
    private final JButton simPlayPauseBtn = SwingTheme.createButton("Play Simulation", SwingTheme.BTN_PRIMARY, Color.WHITE);
    private final JButton simStepBtn = SwingTheme.createButton("Step (1 Turn)", SwingTheme.BG_ELEVATED, SwingTheme.TEXT_PRIMARY);
    private final JSlider simSpeedSlider = new JSlider(100, 1500, 600);
    private Timer simTimer;

    // Standard Buttons
    private final JButton surrenderBtn = SwingTheme.createButton("Surrender", SwingTheme.BTN_DANGER, Color.WHITE);
    private final JButton newGameBtn = SwingTheme.createButton("New Game", SwingTheme.BG_ELEVATED, SwingTheme.TEXT_MUTED);

    private BattleListener battleListener;
    private boolean waitingForAi = false;

    public BattleViewPanel(FleetStrikeApplicationService service) {
        this.service = Objects.requireNonNull(service, "Service cannot be null");

        setLayout(new BorderLayout(12, 12));
        setBackground(SwingTheme.BG_DARK);
        setBorder(new EmptyBorder(12, 16, 12, 16));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);

        setupInteractions();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);

        turnBanner.setFont(SwingTheme.FONT_TITLE);
        turnBanner.setForeground(SwingTheme.TEXT_PRIMARY);

        statusMessage.setFont(SwingTheme.FONT_SUBHEADER);
        statusMessage.setForeground(SwingTheme.TEXT_ACCENT);

        titles.add(turnBanner);
        titles.add(statusMessage);
        header.add(titles, BorderLayout.WEST);

        lastResultBadge.setFont(SwingTheme.FONT_HEADER);
        lastResultBadge.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(lastResultBadge, BorderLayout.EAST);

        return header;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 8, 0, 8);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left: Target Radar Grid Card
        JPanel targetCard = SwingTheme.createCardPanel();
        targetCard.setLayout(new BorderLayout(0, 8));
        JLabel targetTitle = new JLabel("TARGET RADAR (Offense)", SwingConstants.CENTER);
        targetTitle.setFont(SwingTheme.FONT_HEADER);
        targetTitle.setForeground(SwingTheme.TEXT_ACCENT);
        targetCard.add(targetTitle, BorderLayout.NORTH);
        targetCard.add(targetGrid, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.weightx = 0.35;
        content.add(targetCard, gbc);

        // Center: Fleet Statuses & Combat History
        JPanel centerCard = new JPanel(new BorderLayout(0, 8));
        centerCard.setOpaque(false);

        JPanel fleetsPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        fleetsPanel.setOpaque(false);
        fleetsPanel.add(enemyFleetStatus);
        fleetsPanel.add(ownFleetStatus);
        centerCard.add(fleetsPanel, BorderLayout.NORTH);

        centerCard.add(combatLog, BorderLayout.CENTER);

        gbc.gridx = 1;
        gbc.weightx = 0.30;
        content.add(centerCard, gbc);

        // Right: Ocean Fleet Grid Card
        JPanel oceanCard = SwingTheme.createCardPanel();
        oceanCard.setLayout(new BorderLayout(0, 8));
        JLabel oceanTitle = new JLabel("YOUR FLEET (Defense)", SwingConstants.CENTER);
        oceanTitle.setFont(SwingTheme.FONT_HEADER);
        oceanTitle.setForeground(SwingTheme.TEXT_PRIMARY);
        oceanCard.add(oceanTitle, BorderLayout.NORTH);
        oceanCard.add(oceanGrid, BorderLayout.CENTER);

        gbc.gridx = 2;
        gbc.weightx = 0.35;
        content.add(oceanCard, gbc);

        return content;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        // Simulation controls
        simControlPanel.setOpaque(false);
        simSpeedSlider.setBackground(SwingTheme.BG_DARK);
        simSpeedSlider.setForeground(SwingTheme.TEXT_MUTED);
        simSpeedSlider.setToolTipText("Simulation Speed (ms per turn)");
        simControlPanel.add(simPlayPauseBtn);
        simControlPanel.add(simStepBtn);
        simControlPanel.add(new JLabel("Speed:"));
        simControlPanel.add(simSpeedSlider);
        simControlPanel.setVisible(false);

        footer.add(simControlPanel, BorderLayout.CENTER);

        // Action buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(newGameBtn);
        actions.add(surrenderBtn);
        footer.add(actions, BorderLayout.EAST);

        return footer;
    }

    private void setupInteractions() {
        targetGrid.setCellClickListener((coord, event) -> {
            if (waitingForAi || game == null || game.getState() != GameState.IN_PROGRESS) {
                return;
            }
            if (isSimulationMode) {
                return;
            }
            handleHumanShot(coord);
        });

        surrenderBtn.addActionListener(e -> {
            if (game != null && game.getState() == GameState.IN_PROGRESS) {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Commander, are you sure you want to surrender the battle?",
                        "Confirm Surrender",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    Player active = game.getActivePlayer();
                    service.surrender(game.getId(), active.getId());
                    if (battleListener != null) {
                        battleListener.onSurrender(active);
                    }
                    updateUiState();
                }
            }
        });

        newGameBtn.addActionListener(e -> {
            if (battleListener != null) {
                stopSimulation();
                battleListener.onNewGameRequested();
            }
        });

        simPlayPauseBtn.addActionListener(e -> toggleSimulation());
        simStepBtn.addActionListener(e -> executeSingleAiTurn());
    }

    public void startBattle(Game game, boolean isSimulation, boolean isHotseat) {
        this.game = game;
        this.isSimulationMode = isSimulation;
        this.isHotseatMode = isHotseat;
        this.waitingForAi = false;

        combatLog.clearLog();
        simControlPanel.setVisible(isSimulation);
        surrenderBtn.setVisible(!isSimulation);

        if (isSimulation) {
            setupSimulation();
        } else {
            updateUiState();
        }
    }

    private void setupSimulation() {
        targetGrid.setMode(BoardGridPanel.GridMode.DISPLAY_ONLY);
        oceanGrid.setMode(BoardGridPanel.GridMode.DISPLAY_ONLY);
        updateUiState();
    }

    private void toggleSimulation() {
        if (simTimer != null && simTimer.isRunning()) {
            stopSimulation();
        } else {
            startSimulation();
        }
    }

    private void startSimulation() {
        if (game.getState() != GameState.IN_PROGRESS) return;
        simPlayPauseBtn.setText("Pause Simulation");
        simPlayPauseBtn.setBackground(SwingTheme.BTN_DANGER);
        simStepBtn.setEnabled(false);

        int delay = simSpeedSlider.getValue();
        simTimer = new Timer(delay, e -> {
            if (game.getState() == GameState.IN_PROGRESS) {
                executeSingleAiTurn();
            } else {
                stopSimulation();
            }
        });
        simTimer.start();
    }

    private void stopSimulation() {
        if (simTimer != null) {
            simTimer.stop();
            simTimer = null;
        }
        simPlayPauseBtn.setText("Play Simulation");
        simPlayPauseBtn.setBackground(SwingTheme.BTN_PRIMARY);
        simStepBtn.setEnabled(true);
    }

    private void executeSingleAiTurn() {
        if (game == null || game.getState() != GameState.IN_PROGRESS) return;

        Player active = game.getActivePlayer();
        int turnNum = game.getHistory().size() + 1;

        try {
            ShotResult result = service.executeAiTurn(game.getId(), active.getId());
            combatLog.addEntry(turnNum, active.getName(), result.getCoordinate(), result);
            showResultBadge(result);
            updateUiState();

            if (result.isFleetDestroyed() || game.getState() == GameState.FINISHED) {
                stopSimulation();
                if (battleListener != null) {
                    battleListener.onGameOver(game);
                }
            }
        } catch (Exception ex) {
            statusMessage.setText("Simulation Error: " + ex.getMessage());
            stopSimulation();
        }
    }

    private void handleHumanShot(Coordinate targetCoord) {
        Player active = game.getActivePlayer();
        if (active.getTargetBoard().hasBeenAttacked(targetCoord)) {
            statusMessage.setText("Coordinate " + targetCoord + " has already been attacked! Choose another.");
            return;
        }

        int turnNum = game.getHistory().size() + 1;
        try {
            ShotResult result = service.fireShot(new FireShotCommand(game.getId(), active.getId(), targetCoord));
            combatLog.addEntry(turnNum, active.getName(), targetCoord, result);
            showResultBadge(result);
            updateUiState();

            if (result.isFleetDestroyed() || game.getState() == GameState.FINISHED) {
                if (battleListener != null) {
                    battleListener.onGameOver(game);
                }
                return;
            }

            // Handle next turn
            Player nextPlayer = game.getActivePlayer();
            if (nextPlayer.isComputer()) {
                scheduleComputerTurn(nextPlayer);
            } else if (isHotseatMode) {
                handleHotseatTurnTransition(nextPlayer);
            }

        } catch (FleetStrikeException | IllegalArgumentException e) {
            statusMessage.setText(e.getMessage());
        }
    }

    private void scheduleComputerTurn(Player aiPlayer) {
        waitingForAi = true;
        targetGrid.setMode(BoardGridPanel.GridMode.DISPLAY_ONLY);
        statusMessage.setText(aiPlayer.getName() + " is calculating firing solution...");

        Timer aiTimer = new Timer(700, e -> {
            ((Timer) e.getSource()).stop();
            if (game.getState() != GameState.IN_PROGRESS) {
                waitingForAi = false;
                return;
            }

            int turnNum = game.getHistory().size() + 1;
            ShotResult aiResult = service.executeAiTurn(game.getId(), aiPlayer.getId());
            combatLog.addEntry(turnNum, aiPlayer.getName(), aiResult.getCoordinate(), aiResult);
            showResultBadge(aiResult);
            waitingForAi = false;

            updateUiState();

            if (aiResult.isFleetDestroyed() || game.getState() == GameState.FINISHED) {
                if (battleListener != null) {
                    battleListener.onGameOver(game);
                }
            }
        });
        aiTimer.setRepeats(false);
        aiTimer.start();
    }

    private void handleHotseatTurnTransition(Player nextPlayer) {
        // Show pass controls dialog
        HotseatPassDialog.showPassDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                nextPlayer.getName(),
                () -> updateUiState()
        );
    }

    public void updateUiState() {
        if (game == null) return;

        Player active = game.getActivePlayer();
        Player opponent = game.getOpponent(active.getId());
        int turnNum = game.getHistory().size() + 1;

        turnBanner.setText("TURN " + turnNum + " - " + active.getName().toUpperCase());

        if (waitingForAi) {
            statusMessage.setText(active.getName() + " is aiming...");
            targetGrid.setMode(BoardGridPanel.GridMode.DISPLAY_ONLY);
        } else if (isSimulationMode) {
            statusMessage.setText("AI Simulation active. Click 'Play' or 'Step'.");
            targetGrid.setMode(BoardGridPanel.GridMode.DISPLAY_ONLY);
        } else {
            statusMessage.setText("Commander " + active.getName() + ", select coordinates on the radar grid to fire!");
            targetGrid.setMode(BoardGridPanel.GridMode.INTERACTIVE_TARGET);
        }

        // Grids
        targetGrid.setTargetBoard(active.getTargetBoard());
        oceanGrid.setOceanBoard(active.getOceanBoard());

        // Statuses
        ownFleetStatus.updateStatus(active);
        enemyFleetStatus.updateStatus(opponent);

        repaint();
    }

    private void showResultBadge(ShotResult result) {
        switch (result.getStatus()) {
            case MISS -> {
                lastResultBadge.setText("MISS at " + result.getCoordinate() + "!");
                lastResultBadge.setForeground(SwingTheme.TEXT_MUTED);
            }
            case HIT -> {
                lastResultBadge.setText("DIRECT HIT at " + result.getCoordinate() + "!");
                lastResultBadge.setForeground(SwingTheme.TEXT_DANGER);
            }
            case SUNK -> {
                String shipName = result.getSunkShip().map(s -> s.getShipType().getDisplayName()).orElse("Ship");
                lastResultBadge.setText("SUNK " + shipName.toUpperCase() + "!");
                lastResultBadge.setForeground(SwingTheme.COLOR_GOLD);
            }
        }
    }

    public void setBattleListener(BattleListener listener) {
        this.battleListener = listener;
    }
}

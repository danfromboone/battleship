package com.battleship.presentation.swing;

import com.battleship.application.BattleshipApplicationService;
import com.battleship.application.command.CreateGameCommand;
import com.battleship.domain.model.game.Game;
import com.battleship.domain.model.player.Player;
import com.battleship.domain.model.player.PlayerType;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Main application frame and lifecycle manager for the Battleship Swing GUI presentation layer.
 */
public class BattleshipSwingApp extends JFrame {

    private static final String CARD_MENU = "CARD_MENU";
    private static final String CARD_PLACEMENT = "CARD_PLACEMENT";
    private static final String CARD_BATTLE = "CARD_BATTLE";

    private final BattleshipApplicationService service;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel rootCardPanel = new JPanel(cardLayout);

    private final MainMenuPanel menuPanel = new MainMenuPanel();
    private final PlacementViewPanel placementPanel;
    private final BattleViewPanel battlePanel;

    private Game currentGame;
    private MainMenuPanel.GameMode currentMode = MainMenuPanel.GameMode.SINGLE_PLAYER;
    private String lastP1Name = "Admiral";
    private String lastP2Name = "DeepNavy (AI)";

    public BattleshipSwingApp() {
        this(new BattleshipApplicationService());
    }

    public BattleshipSwingApp(BattleshipApplicationService service) {
        super("Battleship - Classic Naval Warfare");
        this.service = Objects.requireNonNull(service, "Service cannot be null");

        this.placementPanel = new PlacementViewPanel(service);
        this.battlePanel = new BattleViewPanel(service);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1020, 680));
        setPreferredSize(new Dimension(1080, 720));

        SwingTheme.applyGlobalTheme();
        initMenuBar();
        initCardPanels();
        initNavigationCallbacks();

        setContentPane(rootCardPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(SwingTheme.BG_PANEL);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, SwingTheme.BORDER_COLOR));

        // Game Menu
        JMenu gameMenu = new JMenu("Game");
        gameMenu.setForeground(SwingTheme.TEXT_PRIMARY);

        JMenuItem newGameItem = new JMenuItem("New Game / Return to Menu");
        newGameItem.addActionListener(e -> showMainMenu());
        gameMenu.add(newGameItem);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        gameMenu.add(exitItem);

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setForeground(SwingTheme.TEXT_PRIMARY);

        JMenuItem aboutItem = new JMenuItem("About Battleship");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(gameMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void initCardPanels() {
        rootCardPanel.add(menuPanel, CARD_MENU);
        rootCardPanel.add(placementPanel, CARD_PLACEMENT);
        rootCardPanel.add(battlePanel, CARD_BATTLE);
        cardLayout.show(rootCardPanel, CARD_MENU);
    }

    private void initNavigationCallbacks() {
        menuPanel.setGameStartListener(this::handleStartGame);

        placementPanel.setBackToMenuListener(this::showMainMenu);

        battlePanel.setBattleListener(new BattleViewPanel.BattleListener() {
            @Override
            public void onGameOver(Game game) {
                GameOverDialog.showGameOver(BattleshipSwingApp.this, game, new GameOverDialog.GameOverCallback() {
                    @Override
                    public void onPlayAgain() {
                        handleStartGame(currentMode, lastP1Name, lastP2Name);
                    }

                    @Override
                    public void onMainMenu() {
                        showMainMenu();
                    }
                });
            }

            @Override
            public void onSurrender(Player player) {
                // Battle over triggered via game state check
                if (currentGame != null) {
                    GameOverDialog.showGameOver(BattleshipSwingApp.this, currentGame, new GameOverDialog.GameOverCallback() {
                        @Override
                        public void onPlayAgain() {
                            handleStartGame(currentMode, lastP1Name, lastP2Name);
                        }

                        @Override
                        public void onMainMenu() {
                            showMainMenu();
                        }
                    });
                }
            }

            @Override
            public void onNewGameRequested() {
                showMainMenu();
            }
        });
    }

    public void showMainMenu() {
        cardLayout.show(rootCardPanel, CARD_MENU);
    }

    private void handleStartGame(MainMenuPanel.GameMode mode, String p1Name, String p2Name) {
        this.currentMode = mode;
        this.lastP1Name = p1Name;
        this.lastP2Name = p2Name;

        switch (mode) {
            case SINGLE_PLAYER -> {
                currentGame = service.createGame(new CreateGameCommand(
                        p1Name, PlayerType.HUMAN,
                        p2Name, PlayerType.COMPUTER
                ));
                // Auto deploy AI fleet
                service.autoDeployFleet(currentGame.getId(), currentGame.getPlayer2().getId());

                // Setup human placement
                placementPanel.setup(currentGame, currentGame.getPlayer1());
                placementPanel.setPlacementCompleteListener(player -> {
                    service.startBattle(currentGame.getId());
                    battlePanel.startBattle(currentGame, false, false);
                    cardLayout.show(rootCardPanel, CARD_BATTLE);
                });

                cardLayout.show(rootCardPanel, CARD_PLACEMENT);
            }

            case TWO_PLAYER_HOTSEAT -> {
                currentGame = service.createGame(new CreateGameCommand(
                        p1Name, PlayerType.HUMAN,
                        p2Name, PlayerType.HUMAN
                ));

                // Step 1: Player 1 Placement
                placementPanel.setup(currentGame, currentGame.getPlayer1());
                placementPanel.setPlacementCompleteListener(p1 -> {
                    // Transition to Player 2
                    HotseatPassDialog.showPassDialog(this, currentGame.getPlayer2().getName(), () -> {
                        // Step 2: Player 2 Placement
                        placementPanel.setup(currentGame, currentGame.getPlayer2());
                        placementPanel.setPlacementCompleteListener(p2 -> {
                            service.startBattle(currentGame.getId());
                            // Transition back to Player 1 for Turn 1
                            HotseatPassDialog.showPassDialog(this, currentGame.getPlayer1().getName(), () -> {
                                battlePanel.startBattle(currentGame, false, true);
                                cardLayout.show(rootCardPanel, CARD_BATTLE);
                            });
                        });
                    });
                });

                cardLayout.show(rootCardPanel, CARD_PLACEMENT);
            }

            case AI_VS_AI -> {
                currentGame = service.createGame(new CreateGameCommand(
                        p1Name, PlayerType.COMPUTER,
                        p2Name, PlayerType.COMPUTER
                ));
                service.autoDeployFleet(currentGame.getId(), currentGame.getPlayer1().getId());
                service.autoDeployFleet(currentGame.getId(), currentGame.getPlayer2().getId());
                service.startBattle(currentGame.getId());

                battlePanel.startBattle(currentGame, true, false);
                cardLayout.show(rootCardPanel, CARD_BATTLE);
            }
        }
    }

    private void showAboutDialog() {
        String msg = """
                BATTLESHIP - Classic Naval Warfare
                
                Swing GUI Presentation Layer
                Built with Java 21 & Domain-Driven Design (DDD).
                
                Features:
                - Interactive Drag & Click Fleet Deployment
                - Tactical Radar Target Tracking
                - Smart AI Hunt-and-Target Strategy
                - Pass-and-Play Hotseat Security Shielding
                - Real-Time Combat Analytics & Battle History
                """;
        JOptionPane.showMessageDialog(this, msg, "About Battleship", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BattleshipSwingApp app = new BattleshipSwingApp();
            app.setVisible(true);
        });
    }
}

package com.fleetstrike.presentation.swing;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Main menu panel allowing player to select game mode, configure player names,
 * and view game rules before launching into placement or battle.
 */
public class MainMenuPanel extends JPanel {

    public enum GameMode {
        SINGLE_PLAYER("Single Player (vs Smart AI)"),
        TWO_PLAYER_HOTSEAT("Two Players (Pass & Play Hotseat)"),
        AI_VS_AI("AI Simulation (Computer vs Computer)");

        private final String label;

        GameMode(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public interface GameStartListener {
        void onStartGame(GameMode mode, String player1Name, String player2Name);
    }

    private GameMode selectedMode = GameMode.SINGLE_PLAYER;
    private final JTextField p1Field = new JTextField("Admiral", 16);
    private final JTextField p2Field = new JTextField("DeepNavy (AI)", 16);
    private final JLabel p1Label = new JLabel("Commander Name:");
    private final JLabel p2Label = new JLabel("Opponent Name:");
    private GameStartListener startListener;

    public MainMenuPanel() {
        setLayout(new GridBagLayout());
        setBackground(SwingTheme.BG_DARK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Card Container
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(SwingTheme.BG_PANEL);
        card.setBorder(new CompoundBorder(
                new LineBorder(SwingTheme.BORDER_COLOR, 1, true),
                new EmptyBorder(24, 32, 24, 32)
        ));

        // Banner Title
        JLabel title = new JLabel("FLEET STRIKE");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        title.setForeground(SwingTheme.TEXT_ACCENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);

        JLabel subtitle = new JLabel("Tactical Naval Combat Simulator");
        subtitle.setFont(SwingTheme.FONT_SUBHEADER);
        subtitle.setForeground(SwingTheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(20));

        // Mode Selection
        JLabel modeHeader = new JLabel("SELECT MISSION TYPE:");
        modeHeader.setFont(SwingTheme.FONT_HEADER);
        modeHeader.setForeground(SwingTheme.TEXT_PRIMARY);
        modeHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(modeHeader);
        card.add(Box.createVerticalStrut(10));

        ButtonGroup modeGroup = new ButtonGroup();
        JPanel modesPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        modesPanel.setOpaque(false);

        JRadioButton spRadio = createRadio(GameMode.SINGLE_PLAYER, true);
        JRadioButton mpRadio = createRadio(GameMode.TWO_PLAYER_HOTSEAT, false);
        JRadioButton aiRadio = createRadio(GameMode.AI_VS_AI, false);

        modeGroup.add(spRadio);
        modeGroup.add(mpRadio);
        modeGroup.add(aiRadio);

        modesPanel.add(spRadio);
        modesPanel.add(mpRadio);
        modesPanel.add(aiRadio);
        modesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(modesPanel);
        card.add(Box.createVerticalStrut(18));

        // Player Name Inputs
        JPanel namesPanel = new JPanel(new GridLayout(2, 2, 10, 8));
        namesPanel.setOpaque(false);

        p1Label.setFont(SwingTheme.FONT_BOLD);
        p1Label.setForeground(SwingTheme.TEXT_PRIMARY);
        p2Label.setFont(SwingTheme.FONT_BOLD);
        p2Label.setForeground(SwingTheme.TEXT_PRIMARY);

        styleTextField(p1Field);
        styleTextField(p2Field);
        p2Field.setEnabled(false); // Default single player AI name fixed or editable

        namesPanel.add(p1Label);
        namesPanel.add(p1Field);
        namesPanel.add(p2Label);
        namesPanel.add(p2Field);
        namesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(namesPanel);
        card.add(Box.createVerticalStrut(22));

        // Start Button
        JButton startBtn = SwingTheme.createButton("LAUNCH ENGAGEMENT", SwingTheme.BTN_PRIMARY, Color.WHITE);
        startBtn.setFont(SwingTheme.FONT_HEADER);
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.setMaximumSize(new Dimension(280, 42));
        startBtn.addActionListener(e -> {
            if (startListener != null) {
                String p1 = p1Field.getText().trim();
                String p2 = p2Field.getText().trim();
                if (p1.isEmpty()) p1 = "Player 1";
                if (p2.isEmpty()) p2 = "Player 2";
                startListener.onStartGame(selectedMode, p1, p2);
            }
        });
        card.add(startBtn);
        card.add(Box.createVerticalStrut(10));

        // Rules Button
        JButton rulesBtn = SwingTheme.createButton("Briefing / Mission Rules", SwingTheme.BG_ELEVATED, SwingTheme.TEXT_PRIMARY);
        rulesBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        rulesBtn.setMaximumSize(new Dimension(280, 34));
        rulesBtn.addActionListener(e -> showRulesDialog());
        card.add(rulesBtn);

        add(card, gbc);
    }

    private JRadioButton createRadio(GameMode mode, boolean selected) {
        JRadioButton radio = new JRadioButton(mode.getLabel(), selected);
        radio.setFont(SwingTheme.FONT_BODY);
        radio.setForeground(SwingTheme.TEXT_PRIMARY);
        radio.setOpaque(false);
        radio.setFocusPainted(false);
        radio.addActionListener(e -> updateMode(mode));
        return radio;
    }

    private void updateMode(GameMode mode) {
        this.selectedMode = mode;
        switch (mode) {
            case SINGLE_PLAYER -> {
                p1Label.setText("Commander Name:");
                p1Field.setText("Admiral");
                p1Field.setEnabled(true);
                p2Label.setText("Computer AI:");
                p2Field.setText("DeepNavy (AI)");
                p2Field.setEnabled(false);
            }
            case TWO_PLAYER_HOTSEAT -> {
                p1Label.setText("Player 1 Name:");
                p1Field.setText("Admiral 1");
                p1Field.setEnabled(true);
                p2Label.setText("Player 2 Name:");
                p2Field.setText("Admiral 2");
                p2Field.setEnabled(true);
            }
            case AI_VS_AI -> {
                p1Label.setText("AI Commander 1:");
                p1Field.setText("DeepNavy Alpha");
                p1Field.setEnabled(false);
                p2Label.setText("AI Commander 2:");
                p2Field.setText("DeepNavy Bravo");
                p2Field.setEnabled(false);
            }
        }
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(SwingTheme.FONT_BODY);
        tf.setBackground(SwingTheme.BG_PANEL_ALT);
        tf.setForeground(SwingTheme.TEXT_PRIMARY);
        tf.setCaretColor(SwingTheme.TEXT_ACCENT);
        tf.setBorder(new CompoundBorder(
                new LineBorder(SwingTheme.BORDER_COLOR, 1),
                new EmptyBorder(6, 8, 6, 8)
        ));
    }

    private void showRulesDialog() {
        String rules = """
                FLEET STRIKE - RULES OF ENGAGEMENT
                
                1. FLEET COMPOSITION:
                   - Carrier (Size 5)
                   - Battleship (Size 4)
                   - Cruiser (Size 3)
                   - Submarine (Size 3)
                   - Destroyer (Size 2)
                   
                2. FLEET DEPLOYMENT:
                   - Place all 5 ships horizontally or vertically on the 10x10 Ocean grid.
                   - Ships may not overlap or extend beyond the grid perimeter.
                   - Press 'R' or right-click to rotate between Horizontal & Vertical.
                   - Or click 'Auto Deploy' for rapid tactical deployment!
                   
                3. COMBAT PHASE:
                   - Alternating turns firing shots into enemy waters via the Radar grid.
                   - HIT: You strike an enemy vessel! Marked in Red (X).
                   - MISS: Your shot falls into open water. Marked in Gray (O).
                   - SUNK: All segments of a vessel have been destroyed!
                   
                4. VICTORY CONDITION:
                   - The first commander to sink all 5 enemy vessels wins the war!
                """;

        JTextArea ta = new JTextArea(rules);
        ta.setFont(SwingTheme.FONT_MONO);
        ta.setEditable(false);
        ta.setBackground(SwingTheme.BG_PANEL);
        ta.setForeground(SwingTheme.TEXT_PRIMARY);
        ta.setBorder(new EmptyBorder(10, 10, 10, 10));

        JOptionPane.showMessageDialog(this, ta, "Tactical Mission Briefing", JOptionPane.INFORMATION_MESSAGE);
    }

    public void setGameStartListener(GameStartListener listener) {
        this.startListener = listener;
    }
}

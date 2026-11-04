package com.fleetstrike.presentation.swing;

import com.fleetstrike.domain.model.game.Game;
import com.fleetstrike.domain.model.player.Player;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Victory and game-over announcement dialog presenting detailed battle statistics,
 * hit accuracy, turn summaries, and post-game choices.
 */
public class GameOverDialog extends JDialog {

    public interface GameOverCallback {
        void onPlayAgain();
        void onMainMenu();
    }

    public static void showGameOver(Window owner, Game game, GameOverCallback callback) {
        GameOverDialog dialog = new GameOverDialog(owner, game, callback);
        dialog.setVisible(true);
    }

    public GameOverDialog(Window owner, Game game, GameOverCallback callback) {
        super(owner, "Engagement Concluded", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);

        Player winner = game.getWinnerId().map(game::getPlayer).orElse(null);
        String winnerName = winner != null ? winner.getName() : "Unknown Commander";

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(SwingTheme.BG_PANEL);
        content.setBorder(new CompoundBorder(
                new LineBorder(SwingTheme.COLOR_GOLD, 2, true),
                new EmptyBorder(24, 36, 24, 36)
        ));

        // Trophy / Star
        JLabel icon = new JLabel("★ NAVAL VICTORY ★");
        icon.setFont(SwingTheme.FONT_TITLE);
        icon.setForeground(SwingTheme.COLOR_GOLD);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(icon);
        content.add(Box.createVerticalStrut(10));

        JLabel winnerLabel = new JLabel("Commander " + winnerName + " Has Emerged Victorious!");
        winnerLabel.setFont(SwingTheme.FONT_HEADER);
        winnerLabel.setForeground(SwingTheme.TEXT_PRIMARY);
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(winnerLabel);
        content.add(Box.createVerticalStrut(16));

        // Statistics Panel
        JPanel statsPanel = new JPanel(new GridLayout(4, 1, 0, 6));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(new CompoundBorder(
                new LineBorder(SwingTheme.BORDER_COLOR, 1),
                new EmptyBorder(10, 14, 10, 14)
        ));

        int totalTurns = game.getHistory().size();
        statsPanel.add(createStatRow("Total Turns Fired:", String.valueOf(totalTurns)));

        Player p1 = game.getPlayer1();
        long p1Shots = game.getHistory().stream().filter(t -> t.shooterId().equals(p1.getId())).count();
        long p1Hits = game.getHistory().stream().filter(t -> t.shooterId().equals(p1.getId()) && t.result().isHit()).count();
        double p1Acc = p1Shots > 0 ? ((double) p1Hits / p1Shots) * 100.0 : 0.0;
        statsPanel.add(createStatRow(p1.getName() + " Accuracy:", String.format("%d/%d (%.1f%%)", p1Hits, p1Shots, p1Acc)));

        Player p2 = game.getPlayer2();
        long p2Shots = game.getHistory().stream().filter(t -> t.shooterId().equals(p2.getId())).count();
        long p2Hits = game.getHistory().stream().filter(t -> t.shooterId().equals(p2.getId()) && t.result().isHit()).count();
        double p2Acc = p2Shots > 0 ? ((double) p2Hits / p2Shots) * 100.0 : 0.0;
        statsPanel.add(createStatRow(p2.getName() + " Accuracy:", String.format("%d/%d (%.1f%%)", p2Hits, p2Shots, p2Acc)));

        statsPanel.add(createStatRow("Fleet Afloat:",
                p1.getName() + ": " + p1.getFleet().getAfloatCount() + " | " +
                p2.getName() + ": " + p2.getFleet().getAfloatCount()));

        content.add(statsPanel);
        content.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btnPanel.setOpaque(false);

        JButton playAgainBtn = SwingTheme.createButton("Play Again", SwingTheme.BTN_SUCCESS, Color.WHITE);
        playAgainBtn.addActionListener(e -> {
            dispose();
            if (callback != null) {
                callback.onPlayAgain();
            }
        });

        JButton menuBtn = SwingTheme.createButton("Return to Menu", SwingTheme.BG_ELEVATED, SwingTheme.TEXT_PRIMARY);
        menuBtn.addActionListener(e -> {
            dispose();
            if (callback != null) {
                callback.onMainMenu();
            }
        });

        btnPanel.add(playAgainBtn);
        btnPanel.add(menuBtn);
        content.add(btnPanel);

        setContentPane(content);
        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel createStatRow(String labelText, String valueText) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(SwingTheme.FONT_BODY);
        label.setForeground(SwingTheme.TEXT_MUTED);

        JLabel value = new JLabel(valueText);
        value.setFont(SwingTheme.FONT_MONO);
        value.setForeground(SwingTheme.TEXT_PRIMARY);
        value.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }
}

package com.battleship.presentation.swing;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modal dialog for 2-Player Pass-and-Play Hotseat mode to obscure secret fleet positions
 * and turn state while handing over control between players.
 */
public class HotseatPassDialog extends JDialog {

    public interface PassConfirmedCallback {
        void onConfirmed();
    }

    public static void showPassDialog(Window owner, String nextPlayerName, PassConfirmedCallback callback) {
        HotseatPassDialog dialog = new HotseatPassDialog(owner, nextPlayerName, callback);
        dialog.setVisible(true);
    }

    public HotseatPassDialog(Window owner, String nextPlayerName, PassConfirmedCallback callback) {
        super(owner, "Pass Controls", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(SwingTheme.BG_PANEL);
        content.setBorder(new CompoundBorder(
                new LineBorder(SwingTheme.TEXT_ACCENT, 2, true),
                new EmptyBorder(30, 40, 30, 40)
        ));

        JLabel iconLabel = new JLabel("⚓");
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 40));
        iconLabel.setForeground(SwingTheme.TEXT_ACCENT);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(iconLabel);
        content.add(Box.createVerticalStrut(12));

        JLabel title = new JLabel("PASS CONTROLS TO COMMANDER");
        title.setFont(SwingTheme.FONT_SUBHEADER);
        title.setForeground(SwingTheme.TEXT_MUTED);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(title);

        JLabel nameLabel = new JLabel(nextPlayerName.toUpperCase());
        nameLabel.setFont(SwingTheme.FONT_TITLE);
        nameLabel.setForeground(SwingTheme.COLOR_GOLD);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(nameLabel);
        content.add(Box.createVerticalStrut(12));

        JLabel info = new JLabel("Screen shielded to protect fleet confidentiality.");
        info.setFont(SwingTheme.FONT_BODY);
        info.setForeground(SwingTheme.TEXT_MUTED);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(info);
        content.add(Box.createVerticalStrut(24));

        JButton readyBtn = SwingTheme.createButton("I AM READY, REVEAL RADAR", SwingTheme.BTN_PRIMARY, Color.WHITE);
        readyBtn.setFont(SwingTheme.FONT_HEADER);
        readyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        readyBtn.addActionListener(e -> {
            dispose();
            if (callback != null) {
                callback.onConfirmed();
            }
        });
        content.add(readyBtn);

        setContentPane(content);
        pack();
        setLocationRelativeTo(owner);
    }
}

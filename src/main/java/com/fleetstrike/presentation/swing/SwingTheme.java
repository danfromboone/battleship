package com.fleetstrike.presentation.swing;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Aesthetic constants, colors, fonts, and UI element factories for the Battleship Swing presentation layer.
 */
public final class SwingTheme {

    // Backgrounds & Surface Colors
    public static final Color BG_DARK = new Color(15, 23, 42);          // Deep Slate #0F172A
    public static final Color BG_PANEL = new Color(30, 41, 59);         // Card Panel #1E293B
    public static final Color BG_PANEL_ALT = new Color(23, 33, 49);     // Slightly darker panel
    public static final Color BG_ELEVATED = new Color(51, 65, 85);       // Elevated element #334155
    public static final Color BORDER_COLOR = new Color(71, 85, 105);    // Slate border #475569

    // Board & Cell Colors
    public static final Color CELL_WATER = new Color(16, 42, 73);        // Deep Ocean
    public static final Color CELL_WATER_HOVER = new Color(24, 58, 98);  // Water hover
    public static final Color CELL_BORDER = new Color(30, 64, 102);      // Subtle grid line
    public static final Color CELL_SHIP = new Color(59, 130, 246);       // Steel Blue #3B82F6
    public static final Color CELL_SHIP_BORDER = new Color(96, 165, 250);// Highlight blue
    public static final Color CELL_HIT = new Color(220, 38, 38);         // Crimson Hit #DC2626
    public static final Color CELL_MISS = new Color(100, 116, 139);      // Slate Splash #64748B
    public static final Color CELL_SUNK = new Color(127, 29, 29);        // Dark Crimson Sunk #7F1D1D

    // Overlays & Highlights
    public static final Color PREVIEW_VALID = new Color(34, 197, 94, 160);    // Translucent Emerald
    public static final Color PREVIEW_INVALID = new Color(239, 68, 68, 160);  // Translucent Crimson
    public static final Color RADAR_RETICLE = new Color(56, 189, 248, 130);   // Translucent Cyan

    // Typography Colors
    public static final Color TEXT_PRIMARY = new Color(248, 250, 252);  // Bright White #F8FAFC
    public static final Color TEXT_MUTED = new Color(148, 163, 184);    // Cool Gray #94A3B8
    public static final Color TEXT_ACCENT = new Color(56, 189, 248);    // Cyan #38BDF8
    public static final Color TEXT_SUCCESS = new Color(74, 222, 128);   // Mint Green #4ADE80
    public static final Color TEXT_DANGER = new Color(248, 113, 113);   // Salmon Red #F87171
    public static final Color COLOR_GOLD = new Color(251, 191, 36);     // Victory Gold #FBBF24

    // Button Colors
    public static final Color BTN_PRIMARY = new Color(37, 99, 235);     // Blue #2563EB
    public static final Color BTN_SUCCESS = new Color(22, 163, 74);     // Green #16A34A
    public static final Color BTN_DANGER = new Color(220, 38, 38);      // Red #DC2626
    public static final Color BTN_SECONDARY = new Color(51, 65, 85);    // Slate #334155
    public static final Color BTN_ACCENT = new Color(14, 165, 233);     // Sky Blue #0EA5E9

    // Fonts
    public static final Font FONT_TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font(Font.SANS_SERIF, Font.BOLD, 15);
    public static final Font FONT_SUBHEADER = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    public static final Font FONT_BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    public static final Font FONT_BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    public static final Font FONT_SMALL = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font(Font.MONOSPACED, Font.BOLD, 12);
    public static final Font FONT_GRID = new Font(Font.MONOSPACED, Font.BOLD, 11);

    private SwingTheme() {}

    /**
     * Applies standard dark theme defaults to Swing's UIManager where appropriate.
     */
    public static void applyGlobalTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        UIManager.put("Panel.background", BG_DARK);
        UIManager.put("OptionPane.background", BG_PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
    }

    /**
     * Creates a styled card container panel.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_PANEL);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));
        return panel;
    }

    /**
     * Creates a styled button with hover feedback and rounded appearance.
     */
    public static JButton createButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(bgColor.brighter(), 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        Color normalBg = bgColor;
        Color hoverBg = bgColor.brighter();
        Color pressedBg = bgColor.darker();

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverBg);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(normalBg);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(pressedBg);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(button.getBounds().contains(e.getPoint()) ? hoverBg : normalBg);
                }
            }
        });

        return button;
    }

    /**
     * Creates a styled section header label.
     */
    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_HEADER);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    /**
     * Creates a styled subtitle or description label.
     */
    public static JLabel createMutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SMALL);
        label.setForeground(TEXT_MUTED);
        return label;
    }
}

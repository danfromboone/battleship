package com.fleetstrike.presentation.swing;

import com.fleetstrike.domain.model.board.ShotResult;
import com.fleetstrike.domain.model.board.ShotStatus;
import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.ship.ShipType;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Real-time event log tracking all shots, hits, misses, and sinkings during battle.
 */
public class CombatLogPanel extends JPanel {

    public record LogEntry(int turnNumber, String shooterName, Coordinate coordinate, ShotResult result) {}

    private final DefaultListModel<LogEntry> listModel = new DefaultListModel<>();
    private final JList<LogEntry> logList = new JList<>(listModel);
    private final JScrollPane scrollPane;

    public CombatLogPanel() {
        setLayout(new BorderLayout(0, 6));
        setBackground(SwingTheme.BG_PANEL);
        setBorder(new CompoundBorder(
                new LineBorder(SwingTheme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel title = new JLabel("Combat History");
        title.setFont(SwingTheme.FONT_SUBHEADER);
        title.setForeground(SwingTheme.TEXT_PRIMARY);
        add(title, BorderLayout.NORTH);

        logList.setBackground(SwingTheme.BG_PANEL_ALT);
        logList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logList.setCellRenderer(new LogEntryRenderer());

        scrollPane = new JScrollPane(logList);
        scrollPane.setBorder(new LineBorder(SwingTheme.BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(SwingTheme.BG_PANEL_ALT);
        scrollPane.setPreferredSize(new Dimension(280, 160));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addEntry(int turnNumber, String shooterName, Coordinate coordinate, ShotResult result) {
        LogEntry entry = new LogEntry(turnNumber, shooterName, coordinate, result);
        listModel.addElement(entry);
        SwingUtilities.invokeLater(() -> {
            int lastIndex = listModel.getSize() - 1;
            if (lastIndex >= 0) {
                logList.ensureIndexIsVisible(lastIndex);
            }
        });
    }

    public void clearLog() {
        listModel.clear();
    }

    public int getEntryCount() {
        return listModel.getSize();
    }

    private static class LogEntryRenderer extends JPanel implements ListCellRenderer<LogEntry> {
        private final JLabel turnLabel = new JLabel();
        private final JLabel contentLabel = new JLabel();

        public LogEntryRenderer() {
            setLayout(new BorderLayout(6, 0));
            setBorder(new EmptyBorder(3, 6, 3, 6));
            setOpaque(true);

            turnLabel.setFont(SwingTheme.FONT_GRID);
            contentLabel.setFont(SwingTheme.FONT_BODY);

            add(turnLabel, BorderLayout.WEST);
            add(contentLabel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends LogEntry> list,
                                                      LogEntry entry,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            setBackground(index % 2 == 0 ? SwingTheme.BG_PANEL_ALT : SwingTheme.BG_PANEL);

            turnLabel.setText(String.format("T%02d", entry.turnNumber()));
            turnLabel.setForeground(SwingTheme.TEXT_MUTED);

            ShotResult res = entry.result();
            String shooter = entry.shooterName();
            Coordinate coord = entry.coordinate();

            if (res.getStatus() == ShotStatus.SUNK) {
                String shipName = res.getSunkShip().map(s -> s.getShipType().getDisplayName()).orElse("Ship");
                contentLabel.setText(shooter + " -> " + coord + ": SUNK " + shipName + "!");
                contentLabel.setForeground(SwingTheme.COLOR_GOLD);
            } else if (res.getStatus() == ShotStatus.HIT) {
                String shipName = res.getHitShipType().map(ShipType::getDisplayName).orElse("Ship");
                contentLabel.setText(shooter + " -> " + coord + ": HIT on " + shipName + "!");
                contentLabel.setForeground(SwingTheme.TEXT_DANGER);
            } else {
                contentLabel.setText(shooter + " -> " + coord + ": MISS (Water)");
                contentLabel.setForeground(SwingTheme.TEXT_MUTED);
            }

            return this;
        }
    }
}

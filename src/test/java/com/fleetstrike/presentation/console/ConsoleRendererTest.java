package com.fleetstrike.presentation.console;

import com.fleetstrike.domain.model.board.ShotResult;
import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.coordinate.Orientation;
import com.fleetstrike.domain.model.player.Player;
import com.fleetstrike.domain.model.player.PlayerId;
import com.fleetstrike.domain.model.player.PlayerType;
import com.fleetstrike.domain.model.ship.ShipPlacement;
import com.fleetstrike.domain.model.ship.ShipType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleRendererTest {

    @Test
    @DisplayName("ConsoleRenderer should format boards, health bars, and legend without errors")
    void shouldRenderGameViewProperly() {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(outBytes);
        ConsoleRenderer renderer = new ConsoleRenderer(ps);

        Player p1 = new Player(PlayerId.of("P1"), "Alice", PlayerType.HUMAN);
        Player p2 = new Player(PlayerId.of("P2"), "Bob", PlayerType.COMPUTER);

        p1.getOceanBoard().placeShip(new ShipPlacement(ShipType.DESTROYER, Coordinate.of(0, 0), Orientation.HORIZONTAL));
        p1.getOceanBoard().receiveShot(Coordinate.of(0, 0)); // Hit on destroyer
        p1.getOceanBoard().receiveShot(Coordinate.of(5, 5)); // Miss

        p1.getTargetBoard().recordShot(ShotResult.hit(Coordinate.of(3, 3), ShipType.CRUISER));
        p1.getTargetBoard().recordShot(ShotResult.miss(Coordinate.of(1, 1)));

        renderer.renderGameView(p1, p2, 1);

        String output = outBytes.toString();
        assertThat(output).contains("YOUR FLEET DEFENSE");
        assertThat(output).contains("RADAR TARGETING");
        assertThat(output).contains("FLEET STATUS");
        assertThat(output).contains("LEGEND");
    }

    @Test
    @DisplayName("Column headers must be precisely aligned with cells on both DEFENSE and RADAR tables")
    void shouldAlignColumnHeadersWithTableCells() {
        boolean prevAnsi = AnsiColor.isEnabled();
        try {
            // Test with ANSI disabled so we can check exact string indices directly
            AnsiColor.setEnabled(false);

            ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(outBytes);
            ConsoleRenderer renderer = new ConsoleRenderer(ps);

            Player p1 = new Player(PlayerId.of("P1"), "Alice", PlayerType.HUMAN);
            Player p2 = new Player(PlayerId.of("P2"), "Bob", PlayerType.COMPUTER);
            renderer.renderGameView(p1, p2, 1);

            String[] lines = outBytes.toString().split("\\R");

            String headerLine = null;
            String row1Line = null;
            for (String line : lines) {
                if (line.contains("A B C D E F G H I J") && headerLine == null) {
                    headerLine = line;
                }
                if (line.trim().startsWith("1 |") && row1Line == null) {
                    row1Line = line;
                }
            }

            assertThat(headerLine).isNotNull();
            assertThat(row1Line).isNotNull();

            // Find first 'A' and second 'A' (radar board header)
            int firstA = headerLine.indexOf('A');
            int secondA = headerLine.indexOf('A', firstA + 1);

            // In row1, find first cell and radar cell
            // First row format: " 1 | ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ |      1 | · · · · · · · · · · |"
            // The first cell is at position 5 ('~' right after " 1 | ")
            // The radar first cell is right after "      1 | "
            int firstBar = row1Line.indexOf('|');
            int defenseCell0 = firstBar + 2; // character after "| "

            int secondBar = row1Line.indexOf('|', defenseCell0 + 1); // end of defense board
            int thirdBar = row1Line.indexOf('|', secondBar + 1); // start of radar board
            int radarCell0 = thirdBar + 2; // character after "| " of radar board

            // Verify DEFENSE board 'A' is aligned with defense cell 0
            assertThat(firstA).isEqualTo(defenseCell0);

            // Verify RADAR board 'A' is aligned with radar cell 0
            assertThat(secondA).isEqualTo(radarCell0);

        } finally {
            AnsiColor.setEnabled(prevAnsi);
        }
    }
}

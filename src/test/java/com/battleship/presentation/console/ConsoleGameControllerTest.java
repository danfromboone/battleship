package com.battleship.presentation.console;

import com.battleship.application.BattleshipApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleGameControllerTest {

    @Test
    @DisplayName("Should run AI vs AI simulation from console controller")
    void shouldRunAiSimulation() {
        // Menu choice: '3' (AI vs AI simulation), then '5' (Exit)
        String simulatedInputs = "3\n5\n";
        ByteArrayInputStream in = new ByteArrayInputStream(simulatedInputs.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        BattleshipApplicationService service = new BattleshipApplicationService();
        ConsoleGameController controller = new ConsoleGameController(service, in, out);
        controller.start();

        String output = outBytes.toString();
        assertThat(output).contains("CLASSIC NAVAL WARFARE");
        assertThat(output).contains("AI SIMULATION BATTLE");
        assertThat(output).contains("VICTORY!");
        assertThat(output).contains("BATTLE STATISTICS");
        assertThat(output).contains("Thank you for playing Battleship");
    }

    @Test
    @DisplayName("Should handle human single player game with surrender")
    void shouldHandleSinglePlayerSurrender() {
        // Choice 1: Single Player
        // Name: "Tester"
        // Deployment: 1 (Auto-deploy)
        // Shot prompt: "surrender"
        // Menu choice: 5 (Exit)
        String simulatedInputs = "1\nTester\n1\nsurrender\n5\n";
        ByteArrayInputStream in = new ByteArrayInputStream(simulatedInputs.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        BattleshipApplicationService service = new BattleshipApplicationService();
        ConsoleGameController controller = new ConsoleGameController(service, in, out);
        controller.start();

        String output = outBytes.toString();
        assertThat(output).contains("Fleet successfully deployed automatically!");
        assertThat(output).contains("Tester has surrendered!");
        assertThat(output).contains("GAME OVER");
    }
}

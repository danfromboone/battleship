package com.fleetstrike.presentation.console;

import com.fleetstrike.application.FleetStrikeApplicationService;
import com.fleetstrike.application.command.CreateGameCommand;
import com.fleetstrike.application.command.FireShotCommand;
import com.fleetstrike.application.command.PlaceShipCommand;
import com.fleetstrike.domain.exception.CellAlreadyAttackedException;
import com.fleetstrike.domain.exception.InvalidCoordinateException;
import com.fleetstrike.domain.exception.OutOfBoundsException;
import com.fleetstrike.domain.exception.ShipOverlapException;
import com.fleetstrike.domain.model.board.ShotResult;
import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.coordinate.Orientation;
import com.fleetstrike.domain.model.game.Game;
import com.fleetstrike.domain.model.game.GameState;
import com.fleetstrike.domain.model.player.Player;
import com.fleetstrike.domain.model.player.PlayerType;
import com.fleetstrike.domain.model.ship.ShipType;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public class ConsoleGameController {
    private final FleetStrikeApplicationService service;
    private final ConsoleRenderer renderer;
    private final Scanner scanner;
    private final PrintStream out;

    public ConsoleGameController() {
        this(new FleetStrikeApplicationService(), System.in, System.out);
    }

    public ConsoleGameController(FleetStrikeApplicationService service, InputStream in, PrintStream out) {
        this.service = service;
        this.renderer = new ConsoleRenderer(out);
        this.scanner = new Scanner(in);
        this.out = out;
    }

    public void start() {
        renderer.renderBanner();

        boolean running = true;
        while (running) {
            out.println(AnsiColor.bold("MAIN MENU:"));
            out.println("  1. Single Player (Human vs Smart Computer)");
            out.println("  2. Two Players (Local Hotseat / Pass-and-Play)");
            out.println("  3. AI Simulation (Computer vs Computer)");
            out.println("  4. Toggle ANSI Colors (Currently: " + (AnsiColor.isEnabled() ? "ON" : "OFF") + ")");
            out.println("  5. Exit");
            out.print(AnsiColor.bold("\nSelect an option [1-5]: "));

            String choice = readLine().trim();
            switch (choice) {
                case "1" -> playSinglePlayerGame();
                case "2" -> playTwoPlayerGame();
                case "3" -> playAiVsAiGame();
                case "4" -> {
                    AnsiColor.setEnabled(!AnsiColor.isEnabled());
                    renderer.printSuccess("ANSI color styling is now " + (AnsiColor.isEnabled() ? "ENABLED" : "DISABLED"));
                }
                case "5", "exit", "quit", "q" -> {
                    renderer.printMessage("Thank you for playing Fleet Strike. Dismissed, Commander!");
                    running = false;
                }
                default -> renderer.printError("Invalid choice. Please enter a number between 1 and 5.");
            }
        }
    }

    private void playSinglePlayerGame() {
        out.print("\nEnter your commander name [Admiral]: ");
        String name = readLine().trim();
        if (name.isEmpty()) name = "Admiral";

        Game game = service.createGame(new CreateGameCommand(
                name, PlayerType.HUMAN,
                "Computer (DeepNavy)", PlayerType.COMPUTER
        ));

        setupPlayerFleet(game, game.getPlayer1());
        service.autoDeployFleet(game.getId(), game.getPlayer2().getId());

        service.startBattle(game.getId());
        runBattleLoop(game);
    }

    private void playTwoPlayerGame() {
        out.print("\nEnter Player 1 name [Player 1]: ");
        String p1Name = readLine().trim();
        if (p1Name.isEmpty()) p1Name = "Player 1";

        out.print("Enter Player 2 name [Player 2]: ");
        String p2Name = readLine().trim();
        if (p2Name.isEmpty()) p2Name = "Player 2";

        Game game = service.createGame(new CreateGameCommand(
                p1Name, PlayerType.HUMAN,
                p2Name, PlayerType.HUMAN
        ));

        renderer.renderHeader(game.getPlayer1().getName() + " - FLEET DEPLOYMENT");
        setupPlayerFleet(game, game.getPlayer1());
        clearScreen();

        promptForPass(game.getPlayer2().getName());
        renderer.renderHeader(game.getPlayer2().getName() + " - FLEET DEPLOYMENT");
        setupPlayerFleet(game, game.getPlayer2());
        clearScreen();

        service.startBattle(game.getId());
        runBattleLoop(game);
    }

    private void playAiVsAiGame() {
        Game game = service.createGame(new CreateGameCommand(
                "AI Alpha", PlayerType.COMPUTER,
                "AI Bravo", PlayerType.COMPUTER
        ));

        service.autoDeployFleet(game.getId(), game.getPlayer1().getId());
        service.autoDeployFleet(game.getId(), game.getPlayer2().getId());
        service.startBattle(game.getId());

        renderer.renderHeader("AI SIMULATION BATTLE");
        out.println("Starting automated simulation between AI Alpha and AI Bravo...\n");

        while (game.getState() == GameState.IN_PROGRESS) {
            Player active = game.getActivePlayer();
            Player opponent = game.getOpponent(active.getId());
            int turn = game.getHistory().size() + 1;

            out.println(AnsiColor.bold(AnsiColor.yellow("Turn " + turn + ": " + active.getName() + " is firing...")));
            ShotResult result = service.executeAiTurn(game.getId(), active.getId());

            out.println(" -> Fired at " + AnsiColor.bold(result.getCoordinate().toString()) + ": " + formatResult(result));

            if (result.isFleetDestroyed()) {
                break;
            }
        }

        renderGameOver(game);
    }

    private void setupPlayerFleet(Game game, Player player) {
        out.println("\nFleet Deployment for " + AnsiColor.bold(player.getName()) + ":");
        out.println("  1. Automatic Random Placement (Quick Start)");
        out.println("  2. Manual Placement");
        out.print("Choose placement method [1-2] (default 1): ");

        String choice = readLine().trim();
        if ("2".equals(choice)) {
            deployFleetManually(game, player);
        } else {
            service.autoDeployFleet(game.getId(), player.getId());
            renderer.renderOceanOnly(player.getOceanBoard(), player.getName() + "'s Deployed Fleet");
            renderer.printSuccess("Fleet successfully deployed automatically!");
        }
    }

    private void deployFleetManually(Game game, Player player) {
        List<ShipType> ships = List.of(
                ShipType.CARRIER,
                ShipType.BATTLESHIP,
                ShipType.CRUISER,
                ShipType.SUBMARINE,
                ShipType.DESTROYER
        );

        for (ShipType shipType : ships) {
            boolean placed = false;
            while (!placed) {
                renderer.renderOceanOnly(player.getOceanBoard(), player.getName() + "'s Fleet Deployment");
                out.println("Placing " + AnsiColor.bold(shipType.getDisplayName()) + 
                        " (Length: " + shipType.getSize() + ")");
                out.print("Enter start coordinate and orientation [H/V], or 'auto' (e.g. 'B3 H'): ");
                String input = readLine().trim();

                if (input.equalsIgnoreCase("auto")) {
                    service.autoDeployFleet(game.getId(), player.getId());
                    renderer.renderOceanOnly(player.getOceanBoard(), player.getName() + "'s Deployed Fleet");
                    renderer.printSuccess("Remaining ships deployed automatically!");
                    return;
                }

                String[] parts = input.split("\\s+");
                if (parts.length < 2) {
                    renderer.printError("Please provide both coordinate and orientation (e.g. 'A1 H' or 'C5 V').");
                    continue;
                }

                try {
                    Coordinate coord = Coordinate.parse(parts[0]);
                    Orientation orientation = Orientation.fromString(parts[1]);
                    service.placeShip(new PlaceShipCommand(game.getId(), player.getId(), shipType, coord, orientation));
                    placed = true;
                    renderer.printSuccess(shipType.getDisplayName() + " successfully placed at " + coord + " (" + orientation + ")!");
                } catch (InvalidCoordinateException | OutOfBoundsException | ShipOverlapException e) {
                    renderer.printError(e.getMessage());
                } catch (IllegalArgumentException e) {
                    renderer.printError("Invalid input: " + e.getMessage());
                }
            }
        }
    }

    private void runBattleLoop(Game game) {
        while (game.getState() == GameState.IN_PROGRESS) {
            Player active = game.getActivePlayer();
            Player opponent = game.getOpponent(active.getId());
            int turnNumber = game.getHistory().size() + 1;

            if (active.isComputer()) {
                out.println("\n" + AnsiColor.bold(AnsiColor.yellow(">>> " + active.getName() + "'s Turn <<<")));
                out.println("Enemy calculating trajectory...");
                pause(800);
                ShotResult result = service.executeAiTurn(game.getId(), active.getId());
                out.println(active.getName() + " fired at " + AnsiColor.bold(result.getCoordinate().toString()) + " -> " + formatResult(result));
                pause(600);
            } else {
                if (game.getPlayer1().getType() == PlayerType.HUMAN && game.getPlayer2().getType() == PlayerType.HUMAN) {
                    promptForPass(active.getName());
                }

                renderer.renderGameView(active, opponent, turnNumber);
                boolean validShot = false;

                while (!validShot) {
                    out.print(AnsiColor.bold("Commander " + active.getName() + ", enter coordinates to fire (e.g. B4) or 'surrender': "));
                    String input = readLine().trim();

                    if (input.equalsIgnoreCase("surrender")) {
                        service.surrender(game.getId(), active.getId());
                        renderer.printWarning(active.getName() + " has surrendered!");
                        validShot = true;
                        break;
                    }

                    if (input.equalsIgnoreCase("help")) {
                        out.println("Commands: Enter a grid coordinate like 'A1' or 'J10' to fire, or 'surrender' to concede.");
                        continue;
                    }

                    try {
                        Coordinate target = Coordinate.parse(input);
                        if (active.getTargetBoard().hasBeenAttacked(target)) {
                            renderer.printWarning("You have already fired at " + target + "! Choose a different coordinate.");
                            continue;
                        }

                        ShotResult result = service.fireShot(new FireShotCommand(game.getId(), active.getId(), target));
                        validShot = true;

                        out.println();
                        out.println(AnsiColor.bold(">>> " + formatResult(result) + " <<<"));
                        out.println();
                        pause(600);

                    } catch (InvalidCoordinateException | OutOfBoundsException | CellAlreadyAttackedException e) {
                        renderer.printError(e.getMessage());
                    } catch (Exception e) {
                        renderer.printError("Unexpected error: " + e.getMessage());
                    }
                }
            }
        }

        renderGameOver(game);
    }

    private void renderGameOver(Game game) {
        renderer.renderHeader("GAME OVER");
        Player winner = game.getPlayer(game.getWinnerId().orElseThrow());
        Player loser = game.getOpponent(winner.getId());

        out.println(AnsiColor.bold(AnsiColor.green("VICTORY! Commander " + winner.getName() + " has emerged victorious!")));
        out.println(loser.getName() + "'s fleet has been entirely destroyed or surrendered.\n");

        int totalTurns = game.getHistory().size();
        long p1Hits = game.getHistory().stream()
                .filter(t -> t.shooterId().equals(game.getPlayer1().getId()) && t.result().isHit())
                .count();
        long p1Shots = game.getHistory().stream()
                .filter(t -> t.shooterId().equals(game.getPlayer1().getId()))
                .count();

        long p2Hits = game.getHistory().stream()
                .filter(t -> t.shooterId().equals(game.getPlayer2().getId()) && t.result().isHit())
                .count();
        long p2Shots = game.getHistory().stream()
                .filter(t -> t.shooterId().equals(game.getPlayer2().getId()))
                .count();

        out.println(AnsiColor.bold("BATTLE STATISTICS:"));
        out.println("  Total Turns Fired: " + totalTurns);
        if (p1Shots > 0) {
            double acc1 = ((double) p1Hits / p1Shots) * 100.0;
            out.printf("  %s: %d hits / %d shots (%.1f%% accuracy)%n", game.getPlayer1().getName(), p1Hits, p1Shots, acc1);
        }
        if (p2Shots > 0) {
            double acc2 = ((double) p2Hits / p2Shots) * 100.0;
            out.printf("  %s: %d hits / %d shots (%.1f%% accuracy)%n", game.getPlayer2().getName(), p2Hits, p2Shots, acc2);
        }
        out.println();
    }

    private String formatResult(ShotResult result) {
        return switch (result.getStatus()) {
            case MISS -> AnsiColor.dim("MISS - Water splash!");
            case HIT -> AnsiColor.bold(AnsiColor.brightRed("DIRECT HIT on " + 
                    result.getHitShipType().map(ShipType::getDisplayName).orElse("enemy ship") + "!"));
            case SUNK -> AnsiColor.bold(AnsiColor.red("DIRECT HIT & SUNK! " +
                    result.getSunkShip().map(s -> s.getShipType().getDisplayName()).orElse("Ship") + " has been sunk!"));
        };
    }

    private void promptForPass(String nextPlayer) {
        out.println("\n" + AnsiColor.bold(">>> Pass controls to " + nextPlayer + ". Press ENTER when ready... <<<"));
        readLine();
        clearScreen();
    }

    private void clearScreen() {
        for (int i = 0; i < 40; i++) {
            out.println();
        }
    }

    private String readLine() {
        if (!scanner.hasNextLine()) {
            return "exit";
        }
        return scanner.nextLine();
    }

    private void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}

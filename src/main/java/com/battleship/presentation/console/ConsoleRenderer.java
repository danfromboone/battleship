package com.battleship.presentation.console;

import com.battleship.domain.model.board.CellStatus;
import com.battleship.domain.model.board.OceanBoard;
import com.battleship.domain.model.board.TargetBoard;
import com.battleship.domain.model.coordinate.Coordinate;
import com.battleship.domain.model.coordinate.GridDimension;
import com.battleship.domain.model.player.Player;
import com.battleship.domain.model.ship.Ship;
import com.battleship.domain.model.ship.ShipType;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

public class ConsoleRenderer {
    private final PrintStream out;

    public ConsoleRenderer() {
        this(System.out);
    }

    public ConsoleRenderer(PrintStream out) {
        this.out = out;
    }

    public void renderHeader(String title) {
        out.println();
        out.println(AnsiColor.bold(AnsiColor.cyan("================================================================================")));
        out.println(AnsiColor.bold(AnsiColor.yellow("  " + title.toUpperCase())));
        out.println(AnsiColor.bold(AnsiColor.cyan("================================================================================")));
        out.println();
    }

    public void renderBanner() {
        String banner = """
                 ____        _   _   _           _     _       
                | __ )  __ _| |_| |_| | ___  ___| |__ (_)_ __  
                |  _ \\ / _` | __| __| |/ _ \\/ __| '_ \\| | '_ \\ 
                | |_) | (_| | |_| |_| |  __/\\__ \\ | | | | |_) |
                |____/ \\__,_|\\__|\\__|_|\\___||___/_| |_|_| .__/ 
                                                        |_|    
                 --- CLASSIC NAVAL WARFARE --- DOMAIN DRIVEN DESIGN ---
                """;
        out.println(AnsiColor.bold(AnsiColor.cyan(banner)));
    }

    public void renderGameView(Player player, Player opponent, int turnNumber) {
        OceanBoard ocean = player.getOceanBoard();
        TargetBoard target = player.getTargetBoard();
        GridDimension dim = ocean.getDimension();

        out.println();
        out.println(AnsiColor.bold(AnsiColor.yellow(">>> TURN " + turnNumber + " | COMMANDER: " + player.getName() + " <<<")));
        out.println();

        String separator = "      ";
        String leftTitle = padRight("  [ YOUR FLEET DEFENSE ]", 26);
        String rightTitle = padRight("  [ RADAR TARGETING ]", 26);
        out.println(AnsiColor.bold(AnsiColor.green(leftTitle)) + separator + AnsiColor.bold(AnsiColor.brightRed(rightTitle)));

        // Column headers (A-J)
        StringBuilder colHeader = new StringBuilder("     ");
        for (int c = 0; c < dim.getColumns(); c++) {
            colHeader.append((char) ('A' + c)).append(" ");
        }
        colHeader.append(" ");
        String headerStr = colHeader.toString();
        out.println(AnsiColor.bold(headerStr) + separator + AnsiColor.bold(headerStr));

        String borderStr = "   +---------------------+";
        out.println(AnsiColor.dim(borderStr) + separator + AnsiColor.dim(borderStr));

        // Rows side by side
        for (int r = 0; r < dim.getRows(); r++) {
            StringBuilder leftRow = new StringBuilder();
            StringBuilder rightRow = new StringBuilder();

            leftRow.append(String.format("%2d | ", r + 1));
            rightRow.append(String.format("%2d | ", r + 1));

            for (int c = 0; c < dim.getColumns(); c++) {
                Coordinate coord = new Coordinate(r, c);
                // Ocean board cell
                CellStatus oceanCell = ocean.getCellStatus(coord);
                leftRow.append(formatOceanCell(oceanCell)).append(" ");

                // Target board cell
                CellStatus targetCell = target.getCellStatus(coord);
                rightRow.append(formatTargetCell(targetCell)).append(" ");
            }

            leftRow.append("|");
            rightRow.append("|");

            out.println(leftRow.toString() + separator + rightRow.toString());
        }

        out.println(AnsiColor.dim(borderStr) + separator + AnsiColor.dim(borderStr));

        out.println();
        renderFleetStatusSideBySide(player, opponent);
        renderLegend();
    }

    public void renderOceanOnly(OceanBoard ocean, String title) {
        GridDimension dim = ocean.getDimension();
        out.println(AnsiColor.bold(AnsiColor.green("  " + title)));

        StringBuilder colHeader = new StringBuilder("     ");
        for (int c = 0; c < dim.getColumns(); c++) {
            colHeader.append((char) ('A' + c)).append(" ");
        }
        colHeader.append(" ");
        out.println(AnsiColor.bold(colHeader.toString()));

        String borderStr = "   +---------------------+";
        out.println(AnsiColor.dim(borderStr));

        for (int r = 0; r < dim.getRows(); r++) {
            StringBuilder rowStr = new StringBuilder();
            rowStr.append(String.format("%2d | ", r + 1));
            for (int c = 0; c < dim.getColumns(); c++) {
                Coordinate coord = new Coordinate(r, c);
                CellStatus cell = ocean.getCellStatus(coord);
                rowStr.append(formatOceanCell(cell)).append(" ");
            }
            rowStr.append("|");
            out.println(rowStr.toString());
        }
        out.println(AnsiColor.dim(borderStr));
        out.println();
    }

    private String padRight(String s, int n) {
        if (s.length() >= n) return s;
        return s + " ".repeat(n - s.length());
    }

    private void renderFleetStatusSideBySide(Player player, Player opponent) {
        out.println(AnsiColor.bold("FLEET STATUS:"));
        List<ShipType> types = List.of(
                ShipType.CARRIER,
                ShipType.BATTLESHIP,
                ShipType.CRUISER,
                ShipType.SUBMARINE,
                ShipType.DESTROYER
        );

        int opponentAfloat = opponent.getFleet().getAfloatCount();
        int opponentSunk = opponent.getFleet().getSunkCount();

        for (int i = 0; i < types.size(); i++) {
            ShipType type = types.get(i);
            Optional<Ship> shipOpt = player.getFleet().getShip(type);
            String ownStatus;
            if (shipOpt.isPresent()) {
                Ship s = shipOpt.get();
                ownStatus = formatShipHealth(s);
            } else {
                ownStatus = AnsiColor.dim("Not Deployed");
            }

            String oppInfo = "";
            if (i == 0) {
                oppInfo = "Enemy Ships Afloat: " + AnsiColor.bold(AnsiColor.green(String.valueOf(opponentAfloat)));
            } else if (i == 1) {
                oppInfo = "Enemy Ships Sunk:   " + AnsiColor.bold(AnsiColor.red(String.valueOf(opponentSunk)));
            } else if (i == 3) {
                oppInfo = "Total Ships:        5";
            }

            out.printf("  %-11s %-32s   |  %s%n", type.getDisplayName() + ":", ownStatus, oppInfo);
        }
        out.println();
    }

    private String formatShipHealth(Ship ship) {
        if (ship.isSunk()) {
            return AnsiColor.bold(AnsiColor.red("[SUNK]"));
        }
        StringBuilder bar = new StringBuilder("[");
        int total = ship.getShipType().getSize();
        int remaining = ship.getRemainingHealth();
        for (int i = 0; i < total; i++) {
            if (i < remaining) {
                bar.append(AnsiColor.green("■"));
            } else {
                bar.append(AnsiColor.red("x"));
            }
        }
        bar.append("] (").append(remaining).append("/").append(total).append(")");
        return bar.toString();
    }

    private void renderLegend() {
        out.println(AnsiColor.dim("LEGEND: ") +
                formatOceanCell(CellStatus.WATER) + " Water  " +
                formatOceanCell(CellStatus.SHIP) + " Own Ship  " +
                formatOceanCell(CellStatus.HIT) + " Hit  " +
                formatOceanCell(CellStatus.MISS) + " Miss  " +
                formatOceanCell(CellStatus.SUNK) + " Sunk");
        out.println();
    }

    private String formatOceanCell(CellStatus status) {
        return switch (status) {
            case WATER -> AnsiColor.blue("~");
            case SHIP -> AnsiColor.bold(AnsiColor.cyan("S"));
            case HIT -> AnsiColor.bold(AnsiColor.brightRed("X"));
            case MISS -> AnsiColor.dim("O");
            case SUNK -> AnsiColor.bold(AnsiColor.red("#"));
        };
    }

    private String formatTargetCell(CellStatus status) {
        return switch (status) {
            case WATER -> AnsiColor.dim("·");
            case SHIP -> AnsiColor.dim("·"); // Hidden from player
            case HIT -> AnsiColor.bold(AnsiColor.brightRed("X"));
            case MISS -> AnsiColor.dim("O");
            case SUNK -> AnsiColor.bold(AnsiColor.red("#"));
        };
    }

    public void printMessage(String msg) {
        out.println(msg);
    }

    public void printSuccess(String msg) {
        out.println(AnsiColor.bold(AnsiColor.green(msg)));
    }

    public void printWarning(String msg) {
        out.println(AnsiColor.bold(AnsiColor.yellow(msg)));
    }

    public void printError(String msg) {
        out.println(AnsiColor.bold(AnsiColor.red(msg)));
    }
}

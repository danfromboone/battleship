package com.fleetstrike.infrastructure.ai;

import com.fleetstrike.domain.model.board.CellStatus;
import com.fleetstrike.domain.model.board.TargetBoard;
import com.fleetstrike.domain.model.coordinate.Coordinate;
import com.fleetstrike.domain.model.coordinate.GridDimension;

import java.util.*;

public class HuntAndTargetAiStrategy implements AiStrategy {
    private final Random random;

    public HuntAndTargetAiStrategy() {
        this(new Random());
    }

    public HuntAndTargetAiStrategy(Random random) {
        this.random = Objects.requireNonNull(random, "Random cannot be null");
    }

    @Override
    public Coordinate chooseTarget(TargetBoard targetBoard) {
        GridDimension dim = targetBoard.getDimension();

        // 1. Find all active hits that belong to unsunk ships
        List<Coordinate> unsunkHits = new ArrayList<>();
        for (int r = 0; r < dim.getRows(); r++) {
            for (int c = 0; c < dim.getColumns(); c++) {
                Coordinate coord = new Coordinate(r, c);
                if (targetBoard.getCellStatus(coord) == CellStatus.HIT) {
                    unsunkHits.add(coord);
                }
            }
        }

        // 2. If we have active hits, we are in TARGET MODE
        if (!unsunkHits.isEmpty()) {
            Coordinate target = findTargetModeCoordinate(unsunkHits, targetBoard, dim);
            if (target != null) {
                return target;
            }
        }

        // 3. Otherwise, we are in HUNT MODE (Parity / Checkerboard)
        return findHuntModeCoordinate(targetBoard, dim);
    }

    private Coordinate findTargetModeCoordinate(List<Coordinate> unsunkHits, TargetBoard targetBoard, GridDimension dim) {
        // If we have multiple unsunk hits, check if any pair defines a line
        if (unsunkHits.size() >= 2) {
            // Check for horizontal or vertical alignment
            for (int i = 0; i < unsunkHits.size(); i++) {
                for (int j = i + 1; j < unsunkHits.size(); j++) {
                    Coordinate c1 = unsunkHits.get(i);
                    Coordinate c2 = unsunkHits.get(j);

                    if (c1.getRow() == c2.getRow()) {
                        // Same row: try left and right endpoints
                        int row = c1.getRow();
                        int minCol = Math.min(c1.getCol(), c2.getCol());
                        int maxCol = Math.max(c1.getCol(), c2.getCol());

                        List<Coordinate> candidates = new ArrayList<>();
                        // Check left
                        if (minCol - 1 >= 0) {
                            Coordinate left = new Coordinate(row, minCol - 1);
                            if (!targetBoard.hasBeenAttacked(left)) {
                                candidates.add(left);
                            }
                        }
                        // Check right
                        if (maxCol + 1 < dim.getColumns()) {
                            Coordinate right = new Coordinate(row, maxCol + 1);
                            if (!targetBoard.hasBeenAttacked(right)) {
                                candidates.add(right);
                            }
                        }
                        if (!candidates.isEmpty()) {
                            return candidates.get(random.nextInt(candidates.size()));
                        }
                    } else if (c1.getCol() == c2.getCol()) {
                        // Same col: try up and down endpoints
                        int col = c1.getCol();
                        int minRow = Math.min(c1.getRow(), c2.getRow());
                        int maxRow = Math.max(c1.getRow(), c2.getRow());

                        List<Coordinate> candidates = new ArrayList<>();
                        // Check up
                        if (minRow - 1 >= 0) {
                            Coordinate up = new Coordinate(minRow - 1, col);
                            if (!targetBoard.hasBeenAttacked(up)) {
                                candidates.add(up);
                            }
                        }
                        // Check down
                        if (maxRow + 1 < dim.getRows()) {
                            Coordinate down = new Coordinate(maxRow + 1, col);
                            if (!targetBoard.hasBeenAttacked(down)) {
                                candidates.add(down);
                            }
                        }
                        if (!candidates.isEmpty()) {
                            return candidates.get(random.nextInt(candidates.size()));
                        }
                    }
                }
            }
        }

        // If no collinear extension found, try adjacent neighbors of any unsunk hit
        List<Coordinate> adjacentCandidates = new ArrayList<>();
        for (Coordinate hit : unsunkHits) {
            for (Coordinate neighbor : hit.getNeighbors(dim)) {
                if (!targetBoard.hasBeenAttacked(neighbor) && !adjacentCandidates.contains(neighbor)) {
                    adjacentCandidates.add(neighbor);
                }
            }
        }

        if (!adjacentCandidates.isEmpty()) {
            return adjacentCandidates.get(random.nextInt(adjacentCandidates.size()));
        }

        return null;
    }

    private Coordinate findHuntModeCoordinate(TargetBoard targetBoard, GridDimension dim) {
        List<Coordinate> parityCandidates = new ArrayList<>();
        List<Coordinate> allUnattacked = new ArrayList<>();

        for (int r = 0; r < dim.getRows(); r++) {
            for (int c = 0; c < dim.getColumns(); c++) {
                Coordinate coord = new Coordinate(r, c);
                if (!targetBoard.hasBeenAttacked(coord)) {
                    allUnattacked.add(coord);
                    if ((r + c) % 2 == 0) {
                        // Check if a ship of length 2 could fit horizontally or vertically through this cell
                        if (canFitShipOfSize2(coord, targetBoard, dim)) {
                            parityCandidates.add(coord);
                        }
                    }
                }
            }
        }

        if (!parityCandidates.isEmpty()) {
            return parityCandidates.get(random.nextInt(parityCandidates.size()));
        }

        if (!allUnattacked.isEmpty()) {
            return allUnattacked.get(random.nextInt(allUnattacked.size()));
        }

        throw new IllegalStateException("No unattacked coordinates left on board");
    }

    private boolean canFitShipOfSize2(Coordinate coord, TargetBoard targetBoard, GridDimension dim) {
        int r = coord.getRow();
        int c = coord.getCol();
        // Check horizontal fit
        boolean leftOpen = (c > 0 && targetBoard.getCellStatus(new Coordinate(r, c - 1)) != CellStatus.MISS);
        boolean rightOpen = (c < dim.getColumns() - 1 && targetBoard.getCellStatus(new Coordinate(r, c + 1)) != CellStatus.MISS);
        if (leftOpen || rightOpen) return true;

        // Check vertical fit
        boolean upOpen = (r > 0 && targetBoard.getCellStatus(new Coordinate(r - 1, c)) != CellStatus.MISS);
        boolean downOpen = (r < dim.getRows() - 1 && targetBoard.getCellStatus(new Coordinate(r + 1, c)) != CellStatus.MISS);
        return upOpen || downOpen;
    }
}

package com.fleetstrike.domain.model.player;

import com.fleetstrike.domain.model.board.OceanBoard;
import com.fleetstrike.domain.model.board.TargetBoard;
import com.fleetstrike.domain.model.coordinate.GridDimension;
import com.fleetstrike.domain.model.ship.Fleet;

import java.util.Objects;

public class Player {
    private final PlayerId id;
    private final String name;
    private final PlayerType type;
    private final OceanBoard oceanBoard;
    private final TargetBoard targetBoard;

    public Player(PlayerId id, String name, PlayerType type) {
        this(id, name, type, new OceanBoard(GridDimension.STANDARD, new Fleet()), new TargetBoard(GridDimension.STANDARD));
    }

    public Player(PlayerId id, String name, PlayerType type, OceanBoard oceanBoard, TargetBoard targetBoard) {
        this.id = Objects.requireNonNull(id, "PlayerId cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.type = Objects.requireNonNull(type, "PlayerType cannot be null");
        this.oceanBoard = Objects.requireNonNull(oceanBoard, "OceanBoard cannot be null");
        this.targetBoard = Objects.requireNonNull(targetBoard, "TargetBoard cannot be null");
    }

    public PlayerId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PlayerType getType() {
        return type;
    }

    public boolean isComputer() {
        return type == PlayerType.COMPUTER;
    }

    public OceanBoard getOceanBoard() {
        return oceanBoard;
    }

    public TargetBoard getTargetBoard() {
        return targetBoard;
    }

    public Fleet getFleet() {
        return oceanBoard.getFleet();
    }

    public boolean isFleetDeployed() {
        return oceanBoard.getFleet().isComplete();
    }

    public boolean isDefeated() {
        return oceanBoard.getFleet().isAllSunk();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(id, player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}

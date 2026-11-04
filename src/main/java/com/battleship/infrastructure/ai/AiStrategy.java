package com.battleship.infrastructure.ai;

import com.battleship.domain.model.board.TargetBoard;
import com.battleship.domain.model.coordinate.Coordinate;

public interface AiStrategy {
    Coordinate chooseTarget(TargetBoard targetBoard);
}

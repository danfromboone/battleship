package com.fleetstrike.infrastructure.ai;

import com.fleetstrike.domain.model.board.TargetBoard;
import com.fleetstrike.domain.model.coordinate.Coordinate;

public interface AiStrategy {
    Coordinate chooseTarget(TargetBoard targetBoard);
}

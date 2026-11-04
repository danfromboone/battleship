package com.battleship.domain.event;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();
}

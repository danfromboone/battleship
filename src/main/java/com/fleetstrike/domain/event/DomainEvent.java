package com.fleetstrike.domain.event;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();
}

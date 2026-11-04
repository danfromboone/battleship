package com.battleship.domain.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class DomainEventPublisher {
    private static final DomainEventPublisher INSTANCE = new DomainEventPublisher();
    private final List<Consumer<DomainEvent>> listeners = Collections.synchronizedList(new ArrayList<>());

    private DomainEventPublisher() {}

    public static DomainEventPublisher instance() {
        return INSTANCE;
    }

    public void subscribe(Consumer<DomainEvent> listener) {
        listeners.add(listener);
    }

    public void unsubscribe(Consumer<DomainEvent> listener) {
        listeners.remove(listener);
    }

    public void clearListeners() {
        listeners.clear();
    }

    public void publish(DomainEvent event) {
        for (Consumer<DomainEvent> listener : new ArrayList<>(listeners)) {
            listener.accept(event);
        }
    }
}

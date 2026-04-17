package com.payments.application.port.out;

import com.payments.application.event.OutboxEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface OutboxRepository  {
    Mono<Void> save(OutboxEvent event);

    Flux<OutboxEvent> findPending();

    Mono<Void> markAsPublished(String id);
}

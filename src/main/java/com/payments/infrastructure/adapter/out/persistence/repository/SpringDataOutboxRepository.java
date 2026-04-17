package com.payments.infrastructure.adapter.out.persistence.repository;

import com.payments.infrastructure.adapter.out.persistence.entity.OutboxEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface SpringDataOutboxRepository extends ReactiveCrudRepository<OutboxEntity, String> {

    Flux<OutboxEntity> findByStatus(String status);
}

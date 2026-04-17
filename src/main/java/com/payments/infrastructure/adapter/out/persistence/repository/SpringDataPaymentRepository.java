package com.payments.infrastructure.adapter.out.persistence.repository;

import com.payments.infrastructure.adapter.out.persistence.entity.PaymentEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SpringDataPaymentRepository extends ReactiveCrudRepository<PaymentEntity, String> {
    @Modifying
    @Query("UPDATE payments SET status = :status WHERE id = :id")
    Mono<Integer> updateStatus(String id, String status);

    Mono<PaymentEntity> findByIdempotencyKey(String idempotencyKey);
}

package com.payments.application.port.out;

import com.payments.domain.model.Payment;
import reactor.core.publisher.Mono;


public interface PaymentRepository {
    Mono<Payment> findById(String id);

    Mono<Payment> save(Payment payment);

    Mono<Void> updateStatus(String id, String status);

    Mono<Payment> findByIdempotencyKey(String key);
}

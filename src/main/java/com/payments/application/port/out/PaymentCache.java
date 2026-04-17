package com.payments.application.port.out;

import com.payments.domain.model.Payment;
import reactor.core.publisher.Mono;

public interface PaymentCache {
    Mono<Payment> get(String id);
    Mono<Void> put(String id, Payment payment);
}

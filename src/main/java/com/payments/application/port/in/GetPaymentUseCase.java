package com.payments.application.port.in;

import com.payments.domain.model.Payment;
import reactor.core.publisher.Mono;

public interface GetPaymentUseCase {
    Mono<Payment> findById(String id);
}

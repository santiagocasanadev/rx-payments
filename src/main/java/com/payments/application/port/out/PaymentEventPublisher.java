package com.payments.application.port.out;

import com.payments.domain.model.Payment;
import reactor.core.publisher.Mono;

public interface PaymentEventPublisher {
    Mono<Void> publishPaymentCreated(Payment payment);
}

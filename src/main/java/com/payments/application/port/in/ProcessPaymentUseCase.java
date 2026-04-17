package com.payments.application.port.in;

import reactor.core.publisher.Mono;

public interface ProcessPaymentUseCase {
    Mono<Void> process(String paymentId);
}

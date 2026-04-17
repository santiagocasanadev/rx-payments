package com.payments.application.port.out;

import reactor.core.publisher.Mono;

public interface AccountServiceClient {
    Mono<Boolean> hasSufficientBalance(String paymentId);
}

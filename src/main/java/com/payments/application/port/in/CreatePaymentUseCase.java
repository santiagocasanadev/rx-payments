package com.payments.application.port.in;

import com.payments.application.port.command.CreatePaymentCommand;
import com.payments.domain.model.Payment;
import reactor.core.publisher.Mono;

public interface CreatePaymentUseCase {

    Mono<Payment> create(CreatePaymentCommand command);
}

package com.payments.application.event;

import java.math.BigDecimal;

public record PaymentCreatedEvent(
        String paymentId,
        BigDecimal amount,
        String currency
) {}

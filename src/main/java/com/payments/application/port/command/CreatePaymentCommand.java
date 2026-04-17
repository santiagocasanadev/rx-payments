package com.payments.application.port.command;

import java.math.BigDecimal;

public record CreatePaymentCommand(
    BigDecimal amount,
    String currency,
    String idempotencyKey
) {}

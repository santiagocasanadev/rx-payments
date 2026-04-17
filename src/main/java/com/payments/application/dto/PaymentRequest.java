package com.payments.application.dto;

import java.math.BigDecimal;

public record PaymentRequest(
        BigDecimal amount,
        String currency
) {}

package com.payments.application.dto;

import com.payments.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse (
    String paymentId,
    BigDecimal amount,
    String currency,
    PaymentStatus status){}


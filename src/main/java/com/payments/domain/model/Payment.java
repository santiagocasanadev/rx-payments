package com.payments.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Payment {
    private final String id;
    private final BigDecimal amount;
    private final String currency;
    private PaymentStatus status;
    private final LocalDateTime createdAt;
    private final String idempotency;

    public Payment(String id, BigDecimal amount, String currency, PaymentStatus status, LocalDateTime createdAt, String idempotency) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
        this.idempotency = idempotency;
    }

    public void markCompleted(){
        this.status = PaymentStatus.COMPLETED;
    }

    public void markFailed(){
        this.status = PaymentStatus.FAILED;
    }
}

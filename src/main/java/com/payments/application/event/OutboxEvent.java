package com.payments.application.event;

import java.time.LocalDateTime;

public record OutboxEvent(
        String id,
        String aggregateId,
        String eventType,
        String payload,
        String correlationId,
        String status,
        LocalDateTime createdAt
) {}

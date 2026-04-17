package com.payments.infrastructure.config;

import org.slf4j.MDC;

import java.util.UUID;

public final class CorrelationIdSupport {

    public static final String HEADER = "X-Correlation-Id";

    private CorrelationIdSupport() {
    }

    public static String currentOrNew() {
        String correlationId = MDC.get(HEADER);
        return correlationId == null || correlationId.isBlank()
                ? UUID.randomUUID().toString()
                : correlationId;
    }
}

package com.payments.infrastructure.adapter;

import com.payments.application.event.PaymentCreatedEvent;
import com.payments.application.port.in.ProcessPaymentUseCase;
import com.payments.infrastructure.config.CorrelationIdSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

@Component
public class PaymentCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentCreatedConsumer.class);

    private final ProcessPaymentUseCase processPaymentUseCase;

    public PaymentCreatedConsumer(ProcessPaymentUseCase processPaymentUseCase) {
        this.processPaymentUseCase = processPaymentUseCase;
    }

    @KafkaListener(
            topics = "payment-created",
            groupId = "payments-group"
    )
    public void consume(
            PaymentCreatedEvent event,
            @Header(name = CorrelationIdSupport.HEADER, required = false) String correlationId
    ) {
        String resolvedCorrelationId = (correlationId == null || correlationId.isBlank())
                ? CorrelationIdSupport.currentOrNew()
                : correlationId;
        log.info("Received payment-created event paymentId={}", event.paymentId());
        processPaymentUseCase.process(event.paymentId())
                .contextWrite(context -> context.put(CorrelationIdSupport.HEADER, resolvedCorrelationId))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }
}

package com.payments.application.port.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.application.event.OutboxEvent;
import com.payments.application.event.PaymentCreatedEvent;
import com.payments.application.port.command.CreatePaymentCommand;
import com.payments.application.port.in.CreatePaymentUseCase;
import com.payments.application.port.out.OutboxRepository;
import com.payments.application.port.out.PaymentEventPublisher;
import com.payments.application.port.out.PaymentRepository;
import com.payments.domain.model.Payment;
import com.payments.domain.model.PaymentStatus;
import com.payments.infrastructure.config.CorrelationIdSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreatePaymentService implements CreatePaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreatePaymentService.class);

    private PaymentRepository repository;

    private PaymentEventPublisher eventPublisher;

    private OutboxRepository outboxRepository;

    public CreatePaymentService(PaymentRepository repository, PaymentEventPublisher eventPublisher, OutboxRepository outboxRepository) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.outboxRepository = outboxRepository;
    }

    @Override
    public Mono<Payment> create(CreatePaymentCommand command) {
        return repository.findByIdempotencyKey(command.idempotencyKey())
                .flatMap(existing -> {
                    log.info("Idempotent hit before insert for paymentId={}", existing.getId());
                    return Mono.just(existing);
                })
                .switchIfEmpty(
                        Mono.defer(() ->
                                Mono.just(command)
                                        .filter(cmd -> cmd.amount().compareTo(BigDecimal.ZERO) > 0)
                                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid amount")))
                                        .map(cmd -> new Payment(
                                                UUID.randomUUID().toString(),
                                                cmd.amount(),
                                                cmd.currency(),
                                                PaymentStatus.PENDING,
                                                LocalDateTime.now(),
                                                cmd.idempotencyKey()
                                        ))
                                        .flatMap(repository::save)
                                        .onErrorResume(error -> {
                                            if (isDuplicateKey(error)) {
                                                log.info("Idempotent hit after duplicate key for idempotencyKey={}", command.idempotencyKey());
                                                return repository.findByIdempotencyKey(command.idempotencyKey());
                                            }
                                            return Mono.error(error);
                                        })
                                        .flatMap(saved -> {
                                            log.info("Creating outbox event for paymentId={}", saved.getId());
                                            String payload = toJson(new PaymentCreatedEvent(
                                                    saved.getId(),
                                                    saved.getAmount(),
                                                    saved.getCurrency()
                                            ));

                                            OutboxEvent event = new OutboxEvent(
                                                    UUID.randomUUID().toString(),
                                                    saved.getId(),
                                                    "PAYMENT_CREATED",
                                                    payload,
                                                    CorrelationIdSupport.currentOrNew(),
                                                    "PENDING",
                                                    LocalDateTime.now()
                                            );

                                            return outboxRepository.save(event)
                                                    .thenReturn(saved);
                                        })
                        )
                );
    }

    private boolean isDuplicateKey(Throwable error) {
        return error.getMessage() != null &&
                error.getMessage().contains("duplicate key");
    }

    private String toJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

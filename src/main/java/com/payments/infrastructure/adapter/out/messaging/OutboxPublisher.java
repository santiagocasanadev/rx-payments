package com.payments.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.application.event.PaymentCreatedEvent;
import com.payments.application.port.out.OutboxRepository;
import com.payments.infrastructure.config.CorrelationIdSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Mono;

@Component
@EnableScheduling
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(OutboxRepository outboxRepository,
                           KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate,
                           ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void publish() {
        outboxRepository.findPending()
                .flatMap(event ->
                        Mono.fromCallable(() -> objectMapper.readValue(event.payload(), PaymentCreatedEvent.class))
                                .flatMap(payload -> Mono.fromFuture(kafkaTemplate.send(
                                        MessageBuilder.withPayload(payload)
                                                .setHeader(KafkaHeaders.TOPIC, "payment-created")
                                                .setHeader(KafkaHeaders.KEY, event.id())
                                                .setHeader(CorrelationIdSupport.HEADER, event.correlationId())
                                                .build()
                                )))
                                .doOnSuccess(r -> log.info("Published outbox event id={}", event.id()))
                                .then(outboxRepository.markAsPublished(event.id()))
                                .onErrorResume(error -> {
                                    log.error("Error publishing outbox event id={}", event.id(), error);
                                    return Mono.empty();
                                })
                )
                .subscribe();
    }
}

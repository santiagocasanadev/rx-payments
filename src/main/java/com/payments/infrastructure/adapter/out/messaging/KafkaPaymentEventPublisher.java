package com.payments.infrastructure.adapter.out.messaging;

import com.payments.application.event.PaymentCreatedEvent;
import com.payments.application.port.out.PaymentEventPublisher;
import com.payments.domain.model.Payment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate;

    public KafkaPaymentEventPublisher(KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Void> publishPaymentCreated(Payment payment) {

        String key = payment.getId().toString();

        PaymentCreatedEvent event = new PaymentCreatedEvent(
                key,
                payment.getAmount(),
                payment.getCurrency()
        );
        return Mono.fromFuture(
                        kafkaTemplate.send("payment-created", key, event))
                .doOnSuccess(result -> {
                    // log técnico (offset, partition)
                })
                .doOnError(error -> {
                    // log error
                })
                .then();
    }
}

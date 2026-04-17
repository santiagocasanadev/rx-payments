package com.payments.application.port.usecase;

import com.payments.application.port.command.CreatePaymentCommand;
import com.payments.application.port.out.OutboxRepository;
import com.payments.application.port.out.PaymentEventPublisher;
import com.payments.application.port.out.PaymentRepository;
import com.payments.domain.model.Payment;
import com.payments.domain.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePaymentServiceTest {

    @Mock
    PaymentRepository repository;

    @Mock
    PaymentEventPublisher eventPublisher;

    @Mock
    OutboxRepository outboxRepository;

    @InjectMocks
    CreatePaymentService service;

    @Test
    void shouldCreatePayment() {
        CreatePaymentCommand cmd = new CreatePaymentCommand(BigDecimal.TEN, "USD", "key-1");
        Payment savedPayment = new Payment(
                "payment-1",
                BigDecimal.TEN,
                "USD",
                PaymentStatus.PENDING,
                LocalDateTime.now(),
                "key-1"
        );

        when(repository.findByIdempotencyKey("key-1")).thenReturn(Mono.empty());
        when(repository.save(any())).thenReturn(Mono.just(savedPayment));
        when(outboxRepository.save(any())).thenReturn(Mono.empty());

        StepVerifier.create(service.create(cmd))
                .assertNext(payment -> {
                    assertEquals("payment-1", payment.getId());
                    assertEquals(BigDecimal.TEN, payment.getAmount());
                    assertEquals(PaymentStatus.PENDING, payment.getStatus());
                })
                .verifyComplete();

        verify(repository).findByIdempotencyKey("key-1");
        verify(repository).save(any());
        verify(outboxRepository).save(any());
    }
}

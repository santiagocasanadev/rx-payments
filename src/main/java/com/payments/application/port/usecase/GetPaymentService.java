package com.payments.application.port.usecase;

import com.payments.application.port.in.GetPaymentUseCase;
import com.payments.application.port.out.PaymentCache;
import com.payments.application.port.out.PaymentRepository;
import com.payments.domain.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GetPaymentService implements GetPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetPaymentService.class);

    private final PaymentRepository repository;

    private final PaymentCache paymentCache;

    public GetPaymentService(PaymentRepository repository, PaymentCache paymentCache) {
        this.repository = repository;
        this.paymentCache = paymentCache;
    }

    @Override
    public Mono<Payment> findById(String id) {
        return paymentCache.get(id)
                .doOnNext(cached -> log.info("Cache hit for paymentId={}", id))
                .switchIfEmpty(
                        repository.findById(id)
                                .doOnNext(db -> log.info("Database hit for paymentId={}", id))
                                .flatMap(payment ->
                                        paymentCache.put(id, payment)
                                                .thenReturn(payment)
                                )
                );
    }
}

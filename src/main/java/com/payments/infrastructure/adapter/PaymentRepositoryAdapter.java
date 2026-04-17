package com.payments.infrastructure.adapter;

import com.payments.application.port.out.PaymentRepository;
import com.payments.domain.model.Payment;
import com.payments.infrastructure.adapter.out.persistence.entity.PaymentEntity;
import com.payments.infrastructure.adapter.out.persistence.mapper.PaymentMapper;
import com.payments.infrastructure.adapter.out.persistence.repository.SpringDataPaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class PaymentRepositoryAdapter implements PaymentRepository {

    private static final Logger log = LoggerFactory.getLogger(PaymentRepositoryAdapter.class);

    private final SpringDataPaymentRepository repository;
    private final PaymentMapper paymentMapper;

    public PaymentRepositoryAdapter(SpringDataPaymentRepository repository, PaymentMapper paymentMapper) {
        this.repository = repository;
        this.paymentMapper = paymentMapper;
    }


    @Override
    public Mono<Payment> findById(String id) {
        return repository.findById(id)
                .map(paymentMapper::toDomain);
    }

    @Override
    public Mono<Payment> save(Payment payment) {
        log.info("Persisting payment id={}", payment.getId());

        return Mono.just(payment)
                .map(paymentMapper::toEntity)
                .flatMap(entity -> repository.save(entity)
                        .doOnNext(e -> log.info("Payment persisted id={}", e.getId()))
                        .doOnError(err -> log.error("Error persisting payment id={}", payment.getId(), err))
                )
                .map(paymentMapper::toDomain);
    }

    @Override
    public Mono<Void> updateStatus(String id, String status) {

        return repository.updateStatus(id, status)
                .doOnNext(rows -> log.info("Updated payment status rows={} paymentId={} status={}", rows, id, status))
                .then();
    }

    @Override
    public Mono<Payment> findByIdempotencyKey(String key) {
        Mono<PaymentEntity> result = repository.findByIdempotencyKey(key);
        if (result == null) {
            return Mono.empty();
        }
        return result.map(paymentMapper::toDomain);
    }
}

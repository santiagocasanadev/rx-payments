package com.payments.infrastructure.adapter;

import com.payments.application.event.OutboxEvent;
import com.payments.application.port.out.OutboxRepository;
import com.payments.infrastructure.adapter.out.persistence.mapper.OutboxMapper;
import com.payments.infrastructure.adapter.out.persistence.repository.SpringDataOutboxRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class OutboxRepositoryAdapter implements OutboxRepository {

    private final SpringDataOutboxRepository repository;
    private final OutboxMapper outboxMapper;

    public OutboxRepositoryAdapter(SpringDataOutboxRepository repository, OutboxMapper outboxMapper) {
        this.repository = repository;
        this.outboxMapper = outboxMapper;
    }

    @Override
    public Mono<Void> save(OutboxEvent event) {
        return repository.save(outboxMapper.toEntity(event)).then();
    }

    @Override
    public Flux<OutboxEvent> findPending() {
        return repository.findByStatus("PENDING")
                .map(outboxMapper::toDomain);
    }

    @Override
    public Mono<Void> markAsPublished(String id) {

        return repository.findById(id)
                .flatMap(entity -> {
                    entity.markPersisted();
                    entity.setStatus("PUBLISHED");
                    return repository.save(entity);
                })
                .then();
    }
}

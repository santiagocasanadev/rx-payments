package com.payments.infrastructure.adapter.out.persistence.mapper;

import com.payments.application.event.OutboxEvent;
import com.payments.infrastructure.adapter.out.persistence.entity.OutboxEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OutboxMapper {

    @Mapping(target = "newEntity", ignore = true)
    OutboxEntity toEntity(OutboxEvent event);

    OutboxEvent toDomain(OutboxEntity entity);
}

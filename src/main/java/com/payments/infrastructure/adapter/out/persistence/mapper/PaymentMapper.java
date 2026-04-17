package com.payments.infrastructure.adapter.out.persistence.mapper;

import com.payments.domain.model.Payment;
import com.payments.domain.model.PaymentStatus;
import com.payments.infrastructure.adapter.out.persistence.entity.PaymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "status", target = "status")
    @Mapping(source = "idempotency", target = "idempotencyKey")
    PaymentEntity toEntity(Payment payment);

    @Mapping(source = "status", target = "status")
    @Mapping(source = "idempotencyKey", target = "idempotency")
    Payment toDomain(PaymentEntity entity);

    default String map(PaymentStatus status) {
        return status == null ? null : status.name();
    }

    default PaymentStatus map(String status) {
        if (status == null) {
            return null;
        }
        try {
            return PaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status value: " + status, e);
        }
    }
}

package com.payments.infrastructure.adapter;

import com.payments.application.port.out.PaymentCache;
import com.payments.domain.model.Payment;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class RedisPaymentCache implements PaymentCache {

    private final ReactiveRedisTemplate<String, Payment> redisTemplate;

    public RedisPaymentCache(ReactiveRedisTemplate<String, Payment> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Payment> get(String id) {
        return redisTemplate.opsForValue().get("payment:" + id);
    }

    @Override
    public Mono<Void> put(String id, Payment payment) {
        return redisTemplate.opsForValue()
                .set("payment:" + id, payment, Duration.ofMinutes(5))
                .then();
    }
}

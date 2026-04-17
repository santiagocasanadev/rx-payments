package com.payments.infrastructure.adapter.out.external;

import com.payments.application.port.out.AccountServiceClient;
import com.payments.infrastructure.config.CorrelationIdSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class AccountServiceClientAdapter implements AccountServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceClientAdapter.class);

    private final WebClient webClient;

    public AccountServiceClientAdapter(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://account-service:8081").build();
    }

    @Override
    public Mono<Boolean> hasSufficientBalance(String paymentId) {

        return webClient.get()
                .uri("/accounts/{id}/balance", paymentId)
                .header(CorrelationIdSupport.HEADER, CorrelationIdSupport.currentOrNew())

                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(Boolean.class);
                    } else {
                        return Mono.error(new RuntimeException("HTTP ERROR"));
                    }
                })

                .doOnSubscribe(sub -> log.info("Calling account service for paymentId={}", paymentId))
                .doOnNext(res -> log.info("Account service response paymentId={} hasBalance={}", paymentId, res))
                .doOnError(err -> log.error("Account service error for paymentId={}", paymentId, err))

                .timeout(Duration.ofSeconds(5)); // aumentar
    }
}

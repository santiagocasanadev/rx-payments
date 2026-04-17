package com.payments.application.port.usecase;

import com.payments.application.port.in.ProcessPaymentUseCase;
import com.payments.application.port.out.AccountServiceClient;
import com.payments.application.port.out.PaymentRepository;
import com.payments.domain.model.PaymentStatus;
import io.r2dbc.spi.R2dbcTransientResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.SocketException;
import java.time.Duration;

@Service
public class ProcessPaymentService implements ProcessPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPaymentService.class);

    private final PaymentRepository repository;
    private final AccountServiceClient accountServiceClient;

    public ProcessPaymentService(PaymentRepository repository,
                                 AccountServiceClient accountServiceClient) {
        this.repository = repository;
        this.accountServiceClient = accountServiceClient;
    }

    @Override
    public Mono<Void> process(String paymentId) {


        return repository.findById(paymentId)

                .flatMap(payment ->
                        accountServiceClient.hasSufficientBalance(paymentId)
                                // retry aquí
                                .retryWhen(
                                        Retry.backoff(3, Duration.ofSeconds(2))
                                )
                                .flatMap(hasBalance -> {
                                    log.info("Balance validation result paymentId={} hasBalance={}", paymentId, hasBalance);

                                    String status = hasBalance
                                            ? PaymentStatus.COMPLETED.name()
                                            : PaymentStatus.FAILED.name();
                                    return repository.updateStatus(payment.getId(), status);
                                })
                                .onErrorResume(error -> {
                                    log.error("Error processing payment paymentId={}", paymentId, error);
                                    return repository.updateStatus(
                                            payment.getId(),
                                            PaymentStatus.FAILED.name()
                                    );
                                })
                )
                .then();
    }

    private boolean isTransientDbError(Throwable error) {
        return error instanceof SocketException
                || error instanceof R2dbcTransientResourceException
                || error.getMessage().contains("Connection reset");
    }
}

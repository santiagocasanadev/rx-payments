package com.payments.entrypoint;

import com.payments.application.dto.PaymentRequest;
import com.payments.application.dto.PaymentResponse;
import com.payments.application.port.command.CreatePaymentCommand;
import com.payments.application.port.in.CreatePaymentUseCase;
import com.payments.application.port.in.GetPaymentUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private CreatePaymentUseCase createUseCase;

    private GetPaymentUseCase getUseCase;

    public PaymentController(CreatePaymentUseCase createUseCase, GetPaymentUseCase getUseCase) {
        this.createUseCase = createUseCase;
        this.getUseCase = getUseCase;
    }

    @PostMapping
    public Mono<ResponseEntity<PaymentResponse>> create(
            @RequestHeader ("Idempotency-Key") String idempotencyKey,
            @RequestBody PaymentRequest request){
        validateIdempotencyKey(idempotencyKey);
        return Mono.just(request)
                .map(req -> new CreatePaymentCommand(
                        req.amount(),
                        req.currency(),
                        idempotencyKey
                ))
                .flatMap(createUseCase::create)
                .map(payment -> new PaymentResponse(
                        payment.getId().toString(),
                        payment.getAmount(),
                        payment.getCurrency(),
                        payment.getStatus()
                ))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<PaymentResponse>> getById(@PathVariable String id) {
        return getUseCase.findById(id)
                .map(payment -> new PaymentResponse(
                        id,
                        payment.getAmount(),
                        payment.getCurrency(),
                        payment.getStatus()
                ))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    private void validateIdempotencyKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Missing Idempotency-Key header");
        }
    }
}
package com.payments.infrastructure.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

@Configuration
public class ReactorMdcConfiguration {

    private static final String MDC_HOOK_KEY = "correlation-id-mdc";

    @PostConstruct
    public void registerHook() {
        Hooks.onEachOperator(MDC_HOOK_KEY, Operators.lift((scannable, subscriber) -> new MdcContextLifter<>(subscriber)));
    }

    @PreDestroy
    public void cleanupHook() {
        Hooks.resetOnEachOperator(MDC_HOOK_KEY);
    }

    private static final class MdcContextLifter<T> implements CoreSubscriber<T> {

        private final CoreSubscriber<? super T> delegate;

        private MdcContextLifter(CoreSubscriber<? super T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Context currentContext() {
            return delegate.currentContext();
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            withCorrelationId(() -> delegate.onSubscribe(subscription));
        }

        @Override
        public void onNext(T value) {
            withCorrelationId(() -> delegate.onNext(value));
        }

        @Override
        public void onError(Throwable throwable) {
            withCorrelationId(() -> delegate.onError(throwable));
        }

        @Override
        public void onComplete() {
            withCorrelationId(delegate::onComplete);
        }

        private void withCorrelationId(Runnable action) {
            String correlationId = currentContext().getOrDefault(CorrelationIdSupport.HEADER, null);
            if (correlationId != null) {
                MDC.put(CorrelationIdSupport.HEADER, correlationId);
            }
            try {
                action.run();
            } finally {
                if (correlationId != null) {
                    MDC.remove(CorrelationIdSupport.HEADER);
                }
            }
        }
    }
}

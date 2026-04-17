package com.payments.infrastructure.adapter;

import com.payments.infrastructure.config.CorrelationIdSupport;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst(CorrelationIdSupport.HEADER);

        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        String finalCorrelationId = correlationId;
        exchange.getAttributes().put(CorrelationIdSupport.HEADER, finalCorrelationId);
        exchange.getResponse().getHeaders().set(CorrelationIdSupport.HEADER, finalCorrelationId);

        return chain.filter(exchange)
                .contextWrite(context -> context.put(CorrelationIdSupport.HEADER, finalCorrelationId));
    }
}

package com.example.transaction.component;

import com.example.transaction.enumType.OutboxStatus;
import com.example.transaction.repository.TransactionOutboxRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetricsConfig {

    private final MeterRegistry registry;
    private final TransactionOutboxRepository outboxRepository;

    @PostConstruct
    public void init() {
        Gauge.builder("outbox.pending.count", outboxRepository, repo ->
                        repo.countByStatus(OutboxStatus.PENDING))
                .tag("service", "transaction-guard")
                .description("Pending messages waiting for Kafka delivery")
                .register(registry);
    }
}

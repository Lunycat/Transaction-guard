package com.example.transaction.kafka;

import com.example.transaction.dto.TransactionEvent;
import com.example.transaction.entity.ProcessedEvents;
import com.example.transaction.entity.Transaction;
import com.example.transaction.enumType.TransactionStatus;
import com.example.transaction.repository.ProcessedEventsRepository;
import com.example.transaction.repository.TransactionRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Slf4j
@Component
public class TransactionConsumer {

    private final ProcessedEventsRepository processedEventsRepository;
    private final TransactionRepository transactionRepository;
    private final MeterRegistry registry;

    @KafkaListener(topics = "tx.created", groupId = "transaction-guard-group")
    @Transactional
    public void handleMessage(TransactionEvent event) {
        if (processedEventsRepository.existsById(event.getEventId())) {
            log.info("Duplicate event {}, skipping", event.getEventId());
            registry.counter("event.processed.total", "status", "skipped").increment();
            return;
        }

        Timer.Sample sample = Timer.start(registry);

        try {
            Transaction tx = transactionRepository.findById(event.getTransactionId())
                    .orElseThrow(() -> new IllegalArgumentException("Tx not found"));

            TransactionStatus newStatus = tx.getAmount().compareTo(BigDecimal.valueOf(100_000)) > 0
                    ? TransactionStatus.FLAGGED : TransactionStatus.APPROVED;

            if (tx.getStatus() != newStatus) {
                tx.setStatus(newStatus);
                transactionRepository.save(tx);
            }

            processedEventsRepository.save(new ProcessedEvents(event.getEventId()));
            registry.counter("event.processed.total", "status", "success").increment();
            log.info("Event {} processed, status updated to {}", event.getEventId(), newStatus);
        } finally {
            sample.stop(registry.timer("event.z"));
        }
    }
}

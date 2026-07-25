package com.example.transaction.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Slf4j
@AllArgsConstructor
public class KafkaDLQConfig {

    private final MeterRegistry registry;

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, Object> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate, (record, e) -> {
            registry.counter("dlq.message.count", "status", "miss").increment();
            return new TopicPartition(record.topic() + ".dlq", record.partition());
        });
    }

    @Bean
    public CommonErrorHandler dlqErrorHandler(DeadLetterPublishingRecoverer recoverer) {
        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 2);
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, fixedBackOff);
        handler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("Retry {}/3 for topic = {}, offset = {}", deliveryAttempt, record.topic(), record.offset())
        );
        handler.setCommitRecovered(true);
        return handler;
    }
}

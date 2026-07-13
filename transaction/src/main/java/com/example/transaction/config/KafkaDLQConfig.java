package com.example.transaction.config;

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
public class KafkaDLQConfig {

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, Object> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate, (consumerRecord, e) ->
                new TopicPartition(consumerRecord.topic() + ".dlq", consumerRecord.partition()));
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

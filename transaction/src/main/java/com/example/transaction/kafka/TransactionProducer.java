package com.example.transaction.kafka;

import com.example.transaction.dto.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionProducer {

    private final String TOPIC = "tx.created";
    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(TransactionEvent event) {
        CompletableFuture<SendResult<String, TransactionEvent>> result =
                kafkaTemplate.send(TOPIC, event.getUserId().toString(), event);

        result.whenComplete((sendResult, throwable) -> {
           if (throwable != null) {
               log.error("Not send event with id = {}, messageError = {}", event.getEventId(), throwable.getMessage());
           } else {
                log.info("Sending event with id = {}", event.getEventId());
           }
        });
    }
}

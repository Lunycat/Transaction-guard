package com.example.transaction.kafka;

import com.example.transaction.dto.TransactionEvent;
import com.example.transaction.entity.TransactionOutbox;
import com.example.transaction.enumType.OutboxStatus;
import com.example.transaction.repository.TransactionOutboxRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private final String TOPIC = "tx.created";
    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private final TransactionOutboxRepository repository;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (s, t, c) -> new JsonPrimitive(s.toString()))
            .registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (j, t, c) -> Instant.parse(j.getAsString()))
            .create();

    @Scheduled(fixedDelay = 2000)
    public void pollAndSend() {
        List<TransactionOutbox> pending = repository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (TransactionOutbox msg : pending) {
            processSingleMessage(msg);
        }
    }

    @Transactional
    public void processSingleMessage(TransactionOutbox msg) {
        try {
            TransactionEvent event = gson.fromJson(msg.getPayload(), TransactionEvent.class);
            kafkaTemplate.send(TOPIC, event).get(5, TimeUnit.SECONDS);

            msg.setStatus(OutboxStatus.SENT);
            repository.save(msg);
        } catch (Exception e) {
            log.error("Failed: {}", msg, e);
        }
    }
}

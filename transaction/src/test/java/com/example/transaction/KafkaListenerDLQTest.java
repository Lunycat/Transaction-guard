package com.example.transaction;

import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Component
public class KafkaListenerDLQTest {

    private final List<ConsumerRecord<String, Object>> dlqRecords = Collections.synchronizedList(new ArrayList<>());

    @KafkaListener(topics = "tx.created.dlq")
    public void testListenerDLQ(ConsumerRecord<String, Object> record) {
        System.out.println(">>> DLQ Listener got record from topic: " + record.topic());
        dlqRecords.add(record);
    }

    public void clear() {
        dlqRecords.clear();
    }
}

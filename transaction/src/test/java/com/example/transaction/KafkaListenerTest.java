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
public class KafkaListenerTest {

    private final List<ConsumerRecord<String, Object>> records = Collections.synchronizedList(new ArrayList<>());

    @KafkaListener(topics = "tx.created")
    public void testListener(ConsumerRecord<String, Object> record) {
        records.add(record);
    }

    public void clear() {
        records.clear();
    }
}

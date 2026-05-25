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

    private final List<Object> events = Collections.synchronizedList(new ArrayList<>());

    @KafkaListener(topics = "tx.created")
    public void testListener(ConsumerRecord<String, Object> record) {
        System.out.println(">>> Got record: " + record.value());
        events.add(record.value());
    }

    public void clear() {
        events.clear();
    }
}

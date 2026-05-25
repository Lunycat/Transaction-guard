package com.example.transaction;

import com.example.transaction.dto.TransactionDTO;
import com.example.transaction.dto.TransactionEvent;
import com.example.transaction.dto.TransactionRequest;
import com.example.transaction.enumType.TransactionStatus;
import com.example.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@ActiveProfiles("test")
@Import(TestContainersConfiguration.class)
@SpringBootTest
class TransactionApplicationTests {

	@Autowired private TransactionService service;
	@Autowired private KafkaListenerTest kafkaListenerTest;

	@BeforeEach
	void clear() {
		await().atMost(5, TimeUnit.SECONDS)
				.pollInterval(100, TimeUnit.MILLISECONDS)
				.until(() -> {
					int sizeBefore = kafkaListenerTest.getEvents().size();
					TimeUnit.MILLISECONDS.sleep(300);
					return sizeBefore == kafkaListenerTest.getEvents().size();
				});
		kafkaListenerTest.clear();
	}

	@Test
	void createTransaction() {
		TransactionRequest request = new TransactionRequest(
				5L, BigDecimal.valueOf(600), "RUS", "Yandex"
		);
		TransactionDTO saved = service.create(request);
		TransactionDTO transaction = service.findById(saved.getId());

		assertNotNull(transaction.getId());
		assertEquals(request.getUserId(), transaction.getUserId());
		assertEquals(0, request.getAmount().compareTo(transaction.getAmount()));
		assertEquals(request.getCurrency(), transaction.getCurrency());
		assertEquals(request.getMerchant(), transaction.getMerchant());
	}

	@Test
	void kafka() {
		TransactionRequest request1 = new TransactionRequest(
				5L, BigDecimal.valueOf(60000), "RUS", "Ukitoki"
		);
		TransactionRequest request2 = new TransactionRequest(
				5L, BigDecimal.valueOf(60000000), "RUS", "Yes.org!"
		);

		TransactionDTO saved1 = service.create(request1);
		TransactionDTO saved2 = service.create(request2);

		await().atMost(5, TimeUnit.SECONDS)
				.pollInterval(100, TimeUnit.MILLISECONDS)
				.untilAsserted(() -> {
					List<Object> events = kafkaListenerTest.getEvents();
					assertInstanceOf(TransactionEvent.class, events.getFirst());
					assertInstanceOf(TransactionEvent.class, events.getLast());
					assertEquals(TransactionStatus.APPROVED, service.findById(saved1.getId()).getStatus());
					assertEquals(TransactionStatus.FLAGGED, service.findById(saved2.getId()).getStatus());
				});
	}

	@Test
	void negative() {
		TransactionRequest request = new TransactionRequest(
				5L, BigDecimal.valueOf(60000000), "RUS", "Yes.org!"
		);


	}
}

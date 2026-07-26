package com.example.transaction;

import com.example.transaction.dto.TransactionDTO;
import com.example.transaction.dto.TransactionEvent;
import com.example.transaction.dto.TransactionRequest;
import com.example.transaction.enumType.TransactionStatus;
import com.example.transaction.repository.TransactionRepository;
import com.example.transaction.service.TransactionService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.util.AssertionErrors.assertFalse;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@ActiveProfiles("test")
@Import(TestContainersConfiguration.class)
@SpringBootTest
class TransactionApplicationTests {

	@Autowired private TransactionService service;
	@Autowired private KafkaListenerTest kafkaListenerTest;
	@Autowired private KafkaListenerDLQTest kafkaListenerDLQTest;
	@MockitoSpyBean private TransactionRepository repository;

	@BeforeEach
	void clear() {
		await().atMost(5, TimeUnit.SECONDS)
				.pollInterval(100, TimeUnit.MILLISECONDS)
				.until(() -> {
					int sizeBefore = kafkaListenerTest.getRecords().size();
					TimeUnit.MILLISECONDS.sleep(300);
					return sizeBefore == kafkaListenerTest.getRecords().size();
				});
		kafkaListenerTest.clear();

		await().atMost(5, TimeUnit.SECONDS)
				.pollInterval(100, TimeUnit.MILLISECONDS)
				.until(() -> {
					int sizeBefore = kafkaListenerTest.getRecords().size();
					TimeUnit.MILLISECONDS.sleep(300);
					return sizeBefore == kafkaListenerTest.getRecords().size();
				});
		kafkaListenerDLQTest.clear();
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

		await().atMost(20, TimeUnit.SECONDS)
				.pollInterval(200, TimeUnit.MILLISECONDS)
				.untilAsserted(() -> {
					List<ConsumerRecord<String, Object>> records = kafkaListenerTest.getRecords();
					assertFalse( "Records list is still empty, waiting for consumer...", records.isEmpty());
					assertInstanceOf(TransactionEvent.class, records.get(0).value());
					assertInstanceOf(TransactionEvent.class, records.get(records.size() - 1).value());
					assertEquals(TransactionStatus.APPROVED, service.findById(saved1.getId()).getStatus());
					assertEquals(TransactionStatus.FLAGGED, service.findById(saved2.getId()).getStatus());
				});
	}

	@Test
	void poisonPillMovesToDlqAfterRetries() {
		doThrow(new RuntimeException("Poison message")).when(repository).findById(anyLong());
		service.create(
				new TransactionRequest(9999L, BigDecimal.valueOf(100), "RUB", "PoisonShop")
		);

		await().atMost(10, TimeUnit.SECONDS)
				.pollInterval(500, TimeUnit.MILLISECONDS)
				.untilAsserted(() -> {
					assertFalse("Падаем в топик", kafkaListenerDLQTest.getDlqRecords().isEmpty());

					ConsumerRecord<String, Object> dlqRecord = kafkaListenerDLQTest.getDlqRecords().getFirst();

					assertNotNull(dlqRecord.headers().lastHeader("kafka_dlt-original-topic"),
							"Должен быть заголовок с исходным топиком");
					assertNotNull(dlqRecord.headers().lastHeader("kafka_dlt-exception-message"),
							"Должен быть заголовок с текстом ошибки");
				});
	}
}

package com.example.transaction.kafka;

import com.example.transaction.dto.TransactionEvent;
import com.example.transaction.entity.Transaction;
import com.example.transaction.enumType.TransactionStatus;
import com.example.transaction.repository.TransactionRepository;
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

    private final TransactionRepository repository;

    @KafkaListener(topics = "tx.created", groupId = "transaction-guard-group")
    @Transactional
    public void getMessage(TransactionEvent event) {
        Long transactionId = event.getTransactionId();
        Transaction transaction = repository.findById(transactionId).orElseThrow(() ->
                new IllegalArgumentException(String.format("Transaction with id = %s not found", transactionId)));

        TransactionStatus newTransactionStatus = transaction.getAmount().compareTo(BigDecimal.valueOf(100_000)) > 0
                        ? TransactionStatus.FLAGGED
                        : TransactionStatus.APPROVED;

        transaction.setStatus(newTransactionStatus);
        repository.save(transaction);
        log.info("Changed status! Transaction event id = {}, Status = {}"
                , transaction.getId(), transaction.getStatus());
    }
}

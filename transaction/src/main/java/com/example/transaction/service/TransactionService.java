package com.example.transaction.service;

import com.example.transaction.dto.TransactionDTO;
import com.example.transaction.dto.TransactionEvent;
import com.example.transaction.dto.TransactionRequest;
import com.example.transaction.entity.Transaction;
import com.example.transaction.entity.TransactionOutbox;
import com.example.transaction.enumType.OutboxStatus;
import com.example.transaction.enumType.TransactionStatus;
import com.example.transaction.mapper.TransactionMapper;
import com.example.transaction.repository.TransactionOutboxRepository;
import com.example.transaction.repository.TransactionRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionMapper mapper;
    private final TransactionRepository transactionRepository;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (s, t, c) -> new JsonPrimitive(s.toString()))
            .registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (j, t, c) -> Instant.parse(j.getAsString()))
            .create();
    private final TransactionOutboxRepository transactionOutboxRepository;

    @Transactional
    public TransactionDTO create(TransactionRequest request) {
        Transaction transaction = mapper.toTransaction(request);
        transaction.setStatus(TransactionStatus.PENDING);
        Transaction saved = transactionRepository.save(transaction);
        TransactionEvent event = mapper.toTransactionEvent(saved);
        TransactionOutbox transactionOutbox = new TransactionOutbox(event, OutboxStatus.PENDING, gson);
        transactionOutboxRepository.save(transactionOutbox);
        return mapper.toTransactionDTO(saved);
    }

    @Transactional(readOnly = true)
    public TransactionDTO findById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Not found transaction with id = " + id));
        return mapper.toTransactionDTO(transaction);
    }
}

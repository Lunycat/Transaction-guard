package com.example.transaction.service;

import com.example.transaction.dto.TransactionDTO;
import com.example.transaction.dto.TransactionRequest;
import com.example.transaction.entity.Transaction;
import com.example.transaction.enumType.TransactionStatus;
import com.example.transaction.mapper.TransactionMapper;
import com.example.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionMapper mapper;
    private final TransactionRepository repository;

    @Transactional
    public TransactionDTO create(TransactionRequest request) {
        Transaction transaction = mapper.toTransaction(request);
        transaction.setStatus(TransactionStatus.PENDING);
        Transaction saved = repository.save(transaction);
        return mapper.toTransactionDTO(saved);
    }
}

package com.example.transaction.mapper;

import com.example.transaction.dto.TransactionRequest;
import com.example.transaction.entity.Transaction;
import org.mapstruct.Mapper;

@Mapper
public interface TransactionMapper {

    Transaction toTransaction(TransactionRequest request);
    TransactionRequest toTransactionRequest(Transaction transaction);
}

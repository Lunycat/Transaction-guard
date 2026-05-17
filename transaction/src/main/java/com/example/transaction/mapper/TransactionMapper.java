package com.example.transaction.mapper;

import com.example.transaction.dto.TransactionDTO;
import com.example.transaction.dto.TransactionRequest;
import com.example.transaction.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {

    Transaction toTransaction(TransactionRequest request);
    TransactionDTO toTransactionDTO(Transaction transaction);
}

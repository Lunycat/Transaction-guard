package com.example.transaction.mapper;

import com.example.transaction.dto.TransactionDTO;
import com.example.transaction.dto.TransactionEvent;
import com.example.transaction.dto.TransactionRequest;
import com.example.transaction.entity.Transaction;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface TransactionMapper {

    Transaction toTransaction(TransactionRequest request);
    TransactionDTO toTransactionDTO(Transaction transaction);

    @Mapping(target = "transactionId", source = "id")
    TransactionEvent toTransactionEvent(Transaction transaction);

    @AfterMapping
    default void generateEventId(@MappingTarget TransactionEvent event) {
        event.setEventId(UUID.randomUUID());
    }
}

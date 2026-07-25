package com.example.transaction.repository;

import com.example.transaction.entity.TransactionOutbox;
import com.example.transaction.enumType.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionOutboxRepository extends JpaRepository<TransactionOutbox, Long> {
    List<TransactionOutbox> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
    long countByStatus(OutboxStatus status);
}

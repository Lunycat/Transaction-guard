package com.example.transaction.repository;

import com.example.transaction.entity.ProcessedEvents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessedEventsRepository extends JpaRepository<ProcessedEvents, UUID> {
}

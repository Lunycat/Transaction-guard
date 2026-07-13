package com.example.transaction.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "processed_events")
public class ProcessedEvents {

    public ProcessedEvents(UUID eventId) {
        this.eventId = eventId;
    }

    @Id
    @EqualsAndHashCode.Include
    private UUID eventId;

    @CreationTimestamp
    private Instant processedAt;
}

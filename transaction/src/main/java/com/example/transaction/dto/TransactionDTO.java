package com.example.transaction.dto;

import com.example.transaction.enumType.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String merchant;
    private TransactionStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}

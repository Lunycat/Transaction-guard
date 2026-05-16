package com.example.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull
    private Long userId;

    @Positive
    private BigDecimal amount;

    @NotBlank
    @Size(max = 10)
    private String currency;

    @NotBlank
    @Size(max = 10)
    private String merchant;
}

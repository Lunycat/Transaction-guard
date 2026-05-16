package com.example.transaction.controller;

import com.example.transaction.dto.TransactionRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @PostMapping
    public ResponseEntity<Void> post(@RequestBody @Valid TransactionRequest request) {
        URI uri = URI.create("");
        return ResponseEntity.created().build();
    }
}

package com.example.modern_api.controller;

import com.example.modern_api.domain.Transaction;
import com.example.modern_api.dto.TransferRequest;
import com.example.modern_api.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Endpoints for fund transfers and financial transactions")
public class TransferController {
    private final TransferService transferService;

    @PostMapping
    @Operation(summary = "Transfer funds", description = "Transfers funds between two wallets. Requires an idempotency key to prevent double-spending.")
    public ResponseEntity<Transaction> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transferService.transferFunds(request));
    }
}

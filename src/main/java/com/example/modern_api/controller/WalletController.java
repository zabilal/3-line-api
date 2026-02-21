package com.example.modern_api.controller;

import com.example.modern_api.domain.Transaction;
import com.example.modern_api.domain.Wallet;
import com.example.modern_api.exception.ResourceNotFoundException;
import com.example.modern_api.repository.TransactionRepository;
import com.example.modern_api.repository.WalletRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallet Operations", description = "Endpoints for balance inquiries and transaction history")
public class WalletController {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @GetMapping("/{accountNumber}/balance")
    @Operation(summary = "Check wallet balance", description = "Returns the current available balance for a given account number")
    public BigDecimal getBalance(@PathVariable String accountNumber) {
        Wallet wallet = walletRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with account: " + accountNumber));

        validateOwnership(wallet);
        return wallet.getBalance();
    }

    @GetMapping("/{accountNumber}/transactions")
    @Operation(summary = "Get transaction history", description = "Returns all successful transactions involving this wallet (as sender or receiver)")
    public List<Transaction> getTransactionHistory(@PathVariable String accountNumber) {
        Wallet wallet = walletRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with account: " + accountNumber));

        validateOwnership(wallet);
        return transactionRepository.findBySenderWalletOrReceiverWallet(wallet, wallet);
    }

    private void validateOwnership(Wallet wallet) {
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        if (!wallet.getUser().getUsername().equals(currentUsername)) {
            throw new com.example.modern_api.exception.WalletException("Access denied: You do not own this wallet");
        }
    }
}

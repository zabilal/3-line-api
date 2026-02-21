package com.example.modern_api.service;

import com.example.modern_api.domain.Transaction;
import com.example.modern_api.domain.Wallet;
import com.example.modern_api.dto.TransferRequest;
import com.example.modern_api.exception.InsufficientFundsException;
import com.example.modern_api.exception.ResourceNotFoundException;
import com.example.modern_api.exception.WalletException;
import com.example.modern_api.repository.TransactionRepository;
import com.example.modern_api.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyService idempotencyService;
    private final ApplicationEventPublisher eventPublisher;

    private static final BigDecimal TRANSACTION_FEE_RATE = new BigDecimal("0.01"); // 1% fee

    @Transactional
    public Transaction transferFunds(TransferRequest request) {
        // 1. Check Idempotency
        var existingResponse = idempotencyService.getResponse(request.getIdempotencyKey());
        if (existingResponse.isPresent()) {
            log.info("Duplicate request detected for key: {}", request.getIdempotencyKey());
            return transactionRepository.findByReference(request.getIdempotencyKey())
                    .orElseThrow(() -> new WalletException("Inconsistent idempotency state"));
        }

        // 2. Fetch Wallets
        Wallet sender = walletRepository.findByAccountNumber(request.getSenderAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Sender account not found"));

        Wallet receiver = walletRepository.findByAccountNumber(request.getReceiverAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver account not found"));

        // 3. Validations
        validateTransfer(sender, receiver, request.getAmount());

        // 4. Calculate Fee
        BigDecimal fee = request.getAmount().multiply(TRANSACTION_FEE_RATE);
        BigDecimal totalDebit = request.getAmount().add(fee);

        if (sender.getBalance().compareTo(totalDebit) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer and fees");
        }

        // 5. Update Balances (Optimistic locking via @Version)
        sender.setBalance(sender.getBalance().subtract(totalDebit));
        receiver.setBalance(receiver.getBalance().add(request.getAmount()));

        walletRepository.save(sender);
        walletRepository.save(receiver);

        // 6. Record Transaction (Ledger)
        Transaction transaction = Transaction.builder()
                .reference(request.getIdempotencyKey())
                .senderWallet(sender)
                .receiverWallet(receiver)
                .amount(request.getAmount())
                .fee(fee)
                .type(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        // 7. Save Idempotency
        idempotencyService.saveResponse(request.getIdempotencyKey(), "SUCCESS");

        // 8. Publish Event (for webhooks/async tasks)
        eventPublisher.publishEvent(savedTransaction);

        return savedTransaction;
    }

    private void validateTransfer(Wallet sender, Wallet receiver, BigDecimal amount) {
        if (sender.getAccountNumber().equals(receiver.getAccountNumber())) {
            throw new WalletException("Cannot transfer to the same account");
        }
        if (sender.getStatus() != Wallet.WalletStatus.ACTIVE) {
            throw new WalletException("Sender account is not active");
        }
        if (receiver.getStatus() != Wallet.WalletStatus.ACTIVE) {
            throw new WalletException("Receiver account is not active");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletException("Transfer amount must be positive");
        }
    }
}

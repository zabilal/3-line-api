package com.example.modern_api.event;

import com.example.modern_api.domain.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionEventListener {

    @EventListener
    @Async // Requires @EnableAsync in main class
    public void handleTransactionSuccess(Transaction transaction) {
        log.info("WEBHOOK SIMULATION: Sending notification for transaction {}", transaction.getReference());
        log.info("Transaction Details: From {} to {}, Amount: {}, Fee: {}",
                transaction.getSenderWallet().getAccountNumber(),
                transaction.getReceiverWallet().getAccountNumber(),
                transaction.getAmount(),
                transaction.getFee());

        // In a real system, this would call an external API or push to a Kafka topic
    }
}

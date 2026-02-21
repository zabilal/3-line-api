package com.example.modern_api.repository;

import com.example.modern_api.domain.Transaction;
import com.example.modern_api.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByReference(String reference);

    List<Transaction> findBySenderWalletOrReceiverWallet(Wallet sender, Wallet receiver);
}

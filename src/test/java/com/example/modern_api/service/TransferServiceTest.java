package com.example.modern_api.service;

import com.example.modern_api.domain.Transaction;
import com.example.modern_api.domain.Wallet;
import com.example.modern_api.dto.TransferRequest;
import com.example.modern_api.exception.InsufficientFundsException;
import com.example.modern_api.exception.ResourceNotFoundException;
import com.example.modern_api.exception.WalletException;
import com.example.modern_api.repository.TransactionRepository;
import com.example.modern_api.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private IdempotencyService idempotencyService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TransferService transferService;

    private Wallet sender;
    private Wallet receiver;
    private TransferRequest request;

    @BeforeEach
    void setUp() {
        sender = Wallet.builder()
                .accountNumber("123")
                .balance(new BigDecimal("1000.00"))
                .status(Wallet.WalletStatus.ACTIVE)
                .build();

        receiver = Wallet.builder()
                .accountNumber("456")
                .balance(new BigDecimal("500.00"))
                .status(Wallet.WalletStatus.ACTIVE)
                .build();

        request = new TransferRequest();
        request.setSenderAccountNumber("123");
        request.setReceiverAccountNumber("456");
        request.setAmount(new BigDecimal("100.00"));
        request.setIdempotencyKey("unique-key");
    }

    @Test
    void transferFunds_Success() {
        when(idempotencyService.getResponse(anyString())).thenReturn(Optional.empty());
        when(walletRepository.findByAccountNumber("123")).thenReturn(Optional.of(sender));
        when(walletRepository.findByAccountNumber("456")).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction result = transferService.transferFunds(request);

        assertThat(result.getStatus()).isEqualTo(Transaction.TransactionStatus.SUCCESS);
        assertThat(sender.getBalance()).isEqualByComparingTo("899.00"); // 1000 - 100 - 1 (fee)
        assertThat(receiver.getBalance()).isEqualByComparingTo("600.00");

        verify(walletRepository, times(2)).save(any());
        verify(idempotencyService).saveResponse(eq("unique-key"), eq("SUCCESS"));
        verify(eventPublisher).publishEvent(any(Transaction.class));
    }

    @Test
    void transferFunds_Idempotent() {
        Transaction existingTx = Transaction.builder().reference("unique-key").build();
        when(idempotencyService.getResponse("unique-key")).thenReturn(Optional.of("SUCCESS"));
        when(transactionRepository.findByReference("unique-key")).thenReturn(Optional.of(existingTx));

        Transaction result = transferService.transferFunds(request);

        assertThat(result).isEqualTo(existingTx);
        verify(walletRepository, never()).save(any());
    }

    @Test
    void transferFunds_SenderNotFound() {
        when(idempotencyService.getResponse(anyString())).thenReturn(Optional.empty());
        when(walletRepository.findByAccountNumber("123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transferFunds(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Sender account not found");
    }

    @Test
    void transferFunds_ReceiverNotFound() {
        when(idempotencyService.getResponse(anyString())).thenReturn(Optional.empty());
        when(walletRepository.findByAccountNumber("123")).thenReturn(Optional.of(sender));
        when(walletRepository.findByAccountNumber("456")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transferFunds(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Receiver account not found");
    }

    @Test
    void transferFunds_SameAccount() {
        request.setReceiverAccountNumber("123");
        when(idempotencyService.getResponse(anyString())).thenReturn(Optional.empty());
        when(walletRepository.findByAccountNumber("123")).thenReturn(Optional.of(sender));

        assertThatThrownBy(() -> transferService.transferFunds(request))
                .isInstanceOf(WalletException.class)
                .hasMessageContaining("Cannot transfer to the same account");
    }

    @Test
    void transferFunds_SenderInactive() {
        sender.setStatus(Wallet.WalletStatus.FROZEN);
        when(idempotencyService.getResponse(anyString())).thenReturn(Optional.empty());
        when(walletRepository.findByAccountNumber("123")).thenReturn(Optional.of(sender));
        when(walletRepository.findByAccountNumber("456")).thenReturn(Optional.of(receiver));

        assertThatThrownBy(() -> transferService.transferFunds(request))
                .isInstanceOf(WalletException.class)
                .hasMessageContaining("Sender account is not active");
    }

    @Test
    void transferFunds_ReceiverInactive() {
        receiver.setStatus(Wallet.WalletStatus.CLOSED);
        when(idempotencyService.getResponse(anyString())).thenReturn(Optional.empty());
        when(walletRepository.findByAccountNumber("123")).thenReturn(Optional.of(sender));
        when(walletRepository.findByAccountNumber("456")).thenReturn(Optional.of(receiver));

        assertThatThrownBy(() -> transferService.transferFunds(request))
                .isInstanceOf(WalletException.class)
                .hasMessageContaining("Receiver account is not active");
    }

    @Test
    void transferFunds_InsufficientFunds() {
        sender.setBalance(new BigDecimal("50.00"));
        when(idempotencyService.getResponse(anyString())).thenReturn(Optional.empty());
        when(walletRepository.findByAccountNumber("123")).thenReturn(Optional.of(sender));
        when(walletRepository.findByAccountNumber("456")).thenReturn(Optional.of(receiver));

        assertThatThrownBy(() -> transferService.transferFunds(request))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    void transferFunds_NegativeAmount() {
        request.setAmount(new BigDecimal("-10.00"));
        when(idempotencyService.getResponse(anyString())).thenReturn(Optional.empty());
        when(walletRepository.findByAccountNumber("123")).thenReturn(Optional.of(sender));
        when(walletRepository.findByAccountNumber("456")).thenReturn(Optional.of(receiver));

        assertThatThrownBy(() -> transferService.transferFunds(request))
                .isInstanceOf(WalletException.class)
                .hasMessageContaining("Transfer amount must be positive");
    }

    @Test
    void transferFunds_InconsistentIdempotency() {
        when(idempotencyService.getResponse("unique-key")).thenReturn(Optional.of("SUCCESS"));
        when(transactionRepository.findByReference("unique-key")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transferFunds(request))
                .isInstanceOf(WalletException.class)
                .hasMessageContaining("Inconsistent idempotency state");
    }
}

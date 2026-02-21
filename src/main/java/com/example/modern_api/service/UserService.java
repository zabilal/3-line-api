package com.example.modern_api.service;

import com.example.modern_api.domain.User;
import com.example.modern_api.domain.Wallet;
import com.example.modern_api.dto.UserRegistrationRequest;
import com.example.modern_api.exception.WalletException;
import com.example.modern_api.repository.UserRepository;
import com.example.modern_api.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final java.security.SecureRandom random = new java.security.SecureRandom();

    @org.springframework.beans.factory.annotation.Value("${app.wallet.welcome-bonus:1000.00}")
    private BigDecimal welcomeBonus;

    @Transactional
    public User registerUser(UserRegistrationRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new WalletException("Username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new WalletException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);

        // Generate and link wallet
        Wallet wallet = Wallet.builder()
                .accountNumber(generateAccountNumber())
                .balance(welcomeBonus)
                .status(Wallet.WalletStatus.ACTIVE)
                .user(savedUser)
                .build();

        walletRepository.save(wallet);
        savedUser.setWallet(wallet);

        return savedUser;
    }

    private String generateAccountNumber() {
        // Use SecureRandom for non-predictable account numbers
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}

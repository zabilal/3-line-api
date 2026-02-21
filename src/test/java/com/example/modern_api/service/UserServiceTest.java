package com.example.modern_api.service;

import com.example.modern_api.domain.User;
import com.example.modern_api.dto.UserRegistrationRequest;
import com.example.modern_api.exception.WalletException;
import com.example.modern_api.repository.UserRepository;
import com.example.modern_api.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_Success() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("john");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.registerUser(request);

        assertThat(result.getUsername()).isEqualTo("john");
        assertThat(result.getWallet()).isNotNull();
        assertThat(result.getWallet().getAccountNumber()).hasSize(10);

        verify(walletRepository).save(any());
        verify(userRepository).save(any());
    }

    @Test
    void registerUser_DuplicateUsername() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(WalletException.class)
                .hasMessage("Username already exists");
    }

    @Test
    void registerUser_DuplicateEmail() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("john");
        request.setEmail("john@example.com");
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(WalletException.class)
                .hasMessage("Email already exists");
    }
}

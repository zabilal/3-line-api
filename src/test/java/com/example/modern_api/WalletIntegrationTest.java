package com.example.modern_api;

import com.example.modern_api.controller.AuthController;
import com.example.modern_api.domain.User;
import com.example.modern_api.dto.TransferRequest;
import com.example.modern_api.dto.UserRegistrationRequest;
import com.example.modern_api.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class WalletIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private WalletRepository walletRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void fullWalletLifecycleTest() throws Exception {
        String password = "StrongPassword123!";

        // 1. Register User A
        UserRegistrationRequest regA = new UserRegistrationRequest();
        regA.setUsername("userA_" + UUID.randomUUID());
        regA.setEmail(UUID.randomUUID() + "@example.com");
        regA.setPassword(password);

        String responseA = mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regA)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        User userA = objectMapper.readValue(responseA, User.class);
        String accountA = userA.getWallet().getAccountNumber();

        // 2. Register User B
        UserRegistrationRequest regB = new UserRegistrationRequest();
        regB.setUsername("userB_" + UUID.randomUUID());
        regB.setEmail(UUID.randomUUID() + "@example.com");
        regB.setPassword(password);

        String responseB = mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regB)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        User userB = objectMapper.readValue(responseB, User.class);
        String accountB = userB.getWallet().getAccountNumber();

        // 3. Login as User A to get Token
        AuthController.LoginRequest login = new AuthController.LoginRequest();
        login.setUsername(regA.getUsername());
        login.setPassword(password);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthController.LoginResponse tokenResponse = objectMapper.readValue(loginResponse,
                AuthController.LoginResponse.class);
        String token = tokenResponse.getAccessToken();

        // 4. Perform Transfer with Token
        TransferRequest transfer = new TransferRequest();
        transfer.setSenderAccountNumber(accountA);
        transfer.setReceiverAccountNumber(accountB);
        transfer.setAmount(new BigDecimal("200.00"));
        transfer.setIdempotencyKey(UUID.randomUUID().toString());
        transfer.setDescription("Lunch payment");

        mockMvc.perform(post("/api/transfers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.amount").value(200.00))
                .andExpect(jsonPath("$.fee").value(2.00));

        // 5. Verify Final Balances
        var finalSender = walletRepository.findByAccountNumber(accountA).get();
        var finalReceiver = walletRepository.findByAccountNumber(accountB).get();

        assertThat(finalSender.getBalance()).isEqualByComparingTo("798.00");
        assertThat(finalReceiver.getBalance()).isEqualByComparingTo("1200.00");
    }

    @Test
    void testValidationErrorsRequiresLogin() throws Exception {
        TransferRequest invalid = new TransferRequest();

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isForbidden());
    }
}

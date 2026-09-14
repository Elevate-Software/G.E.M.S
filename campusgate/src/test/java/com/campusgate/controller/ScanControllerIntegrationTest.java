package com.campusgate.controller;

import com.campusgate.entity.*;
import com.campusgate.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the scan endpoint
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScanControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GateRepository gateRepository;

    @Autowired
    private AccessCredentialRepository credentialRepository;

    @Autowired
    private EntryLogRepository entryLogRepository;

    private Gate operationalGate;
    private Gate closedGate;
    private User activeStudent;

    @BeforeEach
    void setUp() {
        cleanDb();

        operationalGate = gateRepository.save(
                Gate.builder().gateName("Main Gate").location("North Campus").isOperational(true).build()
        );
        closedGate = gateRepository.save(
                Gate.builder().gateName("South Gate").location("South Campus").isOperational(false).build()
        );

        activeStudent = userRepository.save(
                User.builder()
                        .fullName("Test Student")
                        .email("student@test.com")
                        .passwordHash("$2a$10$dummyhashfortestingpurposesonly.abcdefghijklmnopqr")
                        .role(Role.STUDENT)
                        .accountStatus(AccountStatus.ACTIVE)
                        .studentNumber("S9999")
                        .build()
        );
    }

    private Map<String, Object> scanRequest(Long gateId, String token, String direction) {
        return Map.of("gateId", gateId, "token", token, "direction", direction);
    }

    // valid credential -> GRANTED
    @Test
    @WithMockUser(roles = "SECURITY")
    void scan_validCredential_shouldGrantAccess() throws Exception {
        credentialRepository.save(AccessCredential.builder()
                .user(activeStudent)
                .authToken("valid-token-001")
                .status(CredentialStatus.VALID)
                .issueDate(LocalDateTime.now().minusDays(1))
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build());

        mockMvc.perform(post("/api/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                scanRequest(operationalGate.getId(), "valid-token-001", "ENTRY"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("GRANTED"))
                .andExpect(jsonPath("$.message").value("Access Granted"))
                .andExpect(jsonPath("$.user.fullName").value("Test Student"));
    }

    // expired token -> DENIED
    @Test
    @WithMockUser(roles = "SECURITY")
    void scan_expiredToken_shouldDenyAccess() throws Exception {
        credentialRepository.save(AccessCredential.builder()
                .user(activeStudent)
                .authToken("expired-token-002")
                .status(CredentialStatus.VALID)
                .issueDate(LocalDateTime.now().minusDays(5))
                .expiryDate(LocalDateTime.now().minusSeconds(1))
                .build());

        mockMvc.perform(post("/api/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                scanRequest(operationalGate.getId(), "expired-token-002", "ENTRY"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("DENIED"))
                .andExpect(jsonPath("$.message").value("Token Expired"));
    }

    // unknown token -> DENIED
    @Test
    @WithMockUser(roles = "SECURITY")
    void scan_invalidToken_shouldDenyAccess() throws Exception {
        mockMvc.perform(post("/api/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                scanRequest(operationalGate.getId(), "completely-unknown-token", "ENTRY"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("DENIED"))
                .andExpect(jsonPath("$.message").value("Invalid or Revoked Token"));
    }

    // valid token but gate closed -> DENIED
    @Test
    @WithMockUser(roles = "SECURITY")
    void scan_validTokenButGateClosed_shouldDenyAccess() throws Exception {
        credentialRepository.save(AccessCredential.builder()
                .user(activeStudent)
                .authToken("valid-token-004")
                .status(CredentialStatus.VALID)
                .issueDate(LocalDateTime.now().minusDays(1))
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build());

        mockMvc.perform(post("/api/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                scanRequest(closedGate.getId(), "valid-token-004", "EXIT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("DENIED"))
                .andExpect(jsonPath("$.message").value("Gate is not operational"));
    }

    // valid token but account suspended -> DENIED
    @Test
    @WithMockUser(roles = "SECURITY")
    void scan_suspendedAccount_shouldDenyAccess() throws Exception {
        User suspended = userRepository.save(
                User.builder()
                        .fullName("Suspended Student")
                        .email("suspended@test.com")
                        .passwordHash("$2a$10$dummyhashsuspendedonly.abcdefghijklmnopqrstuvwx")
                        .role(Role.STUDENT)
                        .accountStatus(AccountStatus.SUSPENDED)
                        .studentNumber("S8888")
                        .build()
        );

        credentialRepository.save(AccessCredential.builder()
                .user(suspended)
                .authToken("suspended-token-005")
                .status(CredentialStatus.VALID)
                .issueDate(LocalDateTime.now().minusDays(1))
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build());

        mockMvc.perform(post("/api/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                scanRequest(operationalGate.getId(), "suspended-token-005", "ENTRY"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("DENIED"))
                .andExpect(jsonPath("$.message").value("Account SUSPENDED"));
    }

    // unauthenticated request -> 403 Forbidden
    @Test
    void scan_withoutAuthentication_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                scanRequest(operationalGate.getId(), "any-token", "ENTRY"))))
                .andExpect(status().isForbidden());
    }

    @AfterEach
    void tearDown() {
        cleanDb();
    }

    private void cleanDb() {
        entryLogRepository.deleteAll();
        credentialRepository.deleteAll();
        userRepository.deleteAll();
        gateRepository.deleteAll();
    }
}

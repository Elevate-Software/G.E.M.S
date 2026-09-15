package com.campusgate.controller;

import com.campusgate.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CredentialControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccessCredentialRepository credentialRepository;

    @Autowired
    private EntryLogRepository entryLogRepository;

    @Autowired
    private IncidentReportRepository incidentReportRepository;

    @Autowired
    private GateRepository gateRepository;

    private String studentToken;

    @BeforeEach
    void setUp() throws Exception {
        cleanup();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "fullName", "Cred Test Student",
                        "email", "credtest@test.com",
                        "password", "Pass123!",
                        "role", "STUDENT",
                        "studentNumber", "S5555"
                ))));

        var loginRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "credtest@test.com",
                                "password", "Pass123!"
                        ))))
                .andReturn();
        studentToken = objectMapper.readTree(loginRes.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    @AfterEach
    void cleanup() {
        incidentReportRepository.deleteAll();
        entryLogRepository.deleteAll();
        credentialRepository.deleteAll();
        userRepository.deleteAll();
        gateRepository.deleteAll();
    }

    // ── POST /api/credentials/generate ───────────────────────────────────────

    @Test
    void generateCredential_authenticated_shouldReturnCredential() throws Exception {
        mockMvc.perform(post("/api/credentials/generate")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authToken", notNullValue()))
                .andExpect(jsonPath("$.status").value("VALID"));
    }

    @Test
    void generateCredential_unauthenticated_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/credentials/generate"))
                .andExpect(status().isForbidden());
    }

    // ── GET /api/credentials/my-credential ───────────────────────────────────

    @Test
    void getMyCredential_afterGenerate_shouldReturnExistingCredential() throws Exception {
        // Generate first
        mockMvc.perform(post("/api/credentials/generate")
                .header("Authorization", "Bearer " + studentToken));

        // Then retrieve
        mockMvc.perform(get("/api/credentials/my-credential")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authToken", notNullValue()))
                .andExpect(jsonPath("$.status").value("VALID"));
    }

    @Test
    void getMyCredential_unauthenticated_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/credentials/my-credential"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyCredential_whenNoneExists_shouldReturn404() throws Exception {
        // No credential generated — service should throw ResourceNotFoundException
        mockMvc.perform(get("/api/credentials/my-credential")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isNotFound());
    }
}

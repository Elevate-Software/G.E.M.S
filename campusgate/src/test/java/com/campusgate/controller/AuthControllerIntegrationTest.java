package com.campusgate.controller;

import com.campusgate.repository.AccessCredentialRepository;
import com.campusgate.repository.EntryLogRepository;
import com.campusgate.repository.IncidentReportRepository;
import com.campusgate.repository.UserRepository;
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

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntryLogRepository entryLogRepository;

    @Autowired
    private AccessCredentialRepository credentialRepository;

    @Autowired
    private IncidentReportRepository incidentReportRepository;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        incidentReportRepository.deleteAll();
        entryLogRepository.deleteAll();
        credentialRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerAndLoginFlow_shouldSucceed() throws Exception {
        Map<String, Object> registerRequest = Map.of(
                "fullName", "Integration User",
                "email", "integration@example.com",
                "password", "StrongPass123!",
                "role", "STUDENT"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integration@example.com"));

        Map<String, Object> loginRequest = Map.of(
                "email", "integration@example.com",
                "password", "StrongPass123!"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()));
    }
}

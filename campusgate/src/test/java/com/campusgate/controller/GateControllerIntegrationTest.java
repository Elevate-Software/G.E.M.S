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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GateRepository gateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntryLogRepository entryLogRepository;

    @Autowired
    private AccessCredentialRepository credentialRepository;

    @Autowired
    private IncidentReportRepository incidentReportRepository;

    private String studentToken;

    @BeforeEach
    void setUp() throws Exception {
        cleanup();

        gateRepository.save(Gate.builder().gateName("Main Gate").location("North").isOperational(true).build());
        gateRepository.save(Gate.builder().gateName("East Gate").location("East").isOperational(false).build());

        // Register & login student to get a valid JWT
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "fullName", "Gate Test Student",
                        "email", "gatetest@test.com",
                        "password", "Pass123!",
                        "role", "STUDENT",
                        "studentNumber", "S7777"
                ))));

        var loginRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "gatetest@test.com",
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

    @Test
    void getAllGates_authenticated_shouldReturnGateList() throws Exception {
        mockMvc.perform(get("/api/gates")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].gateName", hasItems("Main Gate", "East Gate")));
    }

    @Test
    void getAllGates_unauthenticated_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/gates"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllGates_shouldIncludeOperationalFlag() throws Exception {
        mockMvc.perform(get("/api/gates")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.gateName=='Main Gate')].operational", hasItem(true)))
                .andExpect(jsonPath("$[?(@.gateName=='East Gate')].operational", hasItem(false)));
    }
}

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

    @Test
    void login_invalidCredentials_shouldReturn401() throws Exception {
        Map<String, Object> loginRequest = Map.of(
                "email", "nobody@example.com",
                "password", "wrongpass"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_withValidToken_shouldReturn204() throws Exception {
        // First register and login to get a token
        Map<String, Object> registerRequest = Map.of(
                "fullName", "Logout User",
                "email", "logout@example.com",
                "password", "Pass123!",
                "role", "STUDENT"
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        var loginRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "logout@example.com",
                                "password", "Pass123!"
                        ))))
                .andReturn();
        String token = objectMapper.readTree(loginRes.getResponse().getContentAsString())
                .get("accessToken").asText();

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_withoutToken_shouldReturn403() throws Exception {
        // Unauthenticated request to /api/auth/logout should be rejected with 403 Forbidden
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePassword_withCorrectCurrentPassword_shouldReturn200() throws Exception {
        Map<String, Object> registerRequest = Map.of(
                "fullName", "Change Pass User",
                "email", "changepass@example.com",
                "password", "OldPass123!",
                "role", "STUDENT"
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        var loginRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "changepass@example.com",
                                "password", "OldPass123!"
                        ))))
                .andReturn();
        String token = objectMapper.readTree(loginRes.getResponse().getContentAsString())
                .get("accessToken").asText();

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "currentPassword", "OldPass123!",
                                "newPassword", "NewPass456!"
                        ))))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_withWrongCurrentPassword_shouldReturn400() throws Exception {
        Map<String, Object> registerRequest = Map.of(
                "fullName", "Wrong Pass User",
                "email", "wrongpass@example.com",
                "password", "Correct123!",
                "role", "STUDENT"
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        var loginRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "wrongpass@example.com",
                                "password", "Correct123!"
                        ))))
                .andReturn();
        String token = objectMapper.readTree(loginRes.getResponse().getContentAsString())
                .get("accessToken").asText();

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "currentPassword", "WrongOld!",
                                "newPassword", "NewPass456!"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid current password"));
    }
}

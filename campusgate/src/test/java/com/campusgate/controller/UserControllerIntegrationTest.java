package com.campusgate.controller;

import com.campusgate.entity.Role;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

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

    private String adminToken;
    private String studentToken;
    private Long studentId;

    @BeforeEach
    void setUp() throws Exception {
        cleanUp();
        // Register an admin user
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "fullName", "Admin User",
                        "email", "admin@usertest.com",
                        "password", "Admin123!",
                        "role", "ADMIN"
                ))));

        // Force role to ADMIN since register defaults to ACTIVE
        userRepository.findByEmail("admin@usertest.com").ifPresent(u -> {
            u.setRole(Role.ADMIN);
            userRepository.save(u);
        });

        // Login as admin
        var loginRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "admin@usertest.com",
                                "password", "Admin123!"
                        ))))
                .andReturn();
        adminToken = objectMapper.readTree(loginRes.getResponse().getContentAsString())
                .get("accessToken").asText();

        // Register a student
        var regRes = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Student User",
                                "email", "student@usertest.com",
                                "password", "Student123!",
                                "role", "STUDENT",
                                "studentNumber", "S9001"
                        ))))
                .andReturn();
        studentId = objectMapper.readTree(regRes.getResponse().getContentAsString())
                .get("id").asLong();

        // Login as student
        var studentLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "student@usertest.com",
                                "password", "Student123!"
                        ))))
                .andReturn();
        studentToken = objectMapper.readTree(studentLogin.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    @AfterEach
    void cleanUp() {
        incidentReportRepository.deleteAll();
        entryLogRepository.deleteAll();
        credentialRepository.deleteAll();
        userRepository.deleteAll();
    }

    // GET /api/users/me

    @Test
    void getMyProfile_shouldReturnCurrentUser() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student@usertest.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void getMyProfile_unauthenticated_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    // GET /api/users

    @Test
    void getAllUsers_asAdmin_shouldReturnPage() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void getAllUsers_asStudent_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    // GET /api/users/{id}

    @Test
    void getUserById_asAdmin_shouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/" + studentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentId));
    }

    @Test
    void getUserById_nonExistent_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/users/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // PATCH /api/users/{id}/status

    @Test
    void updateUserStatus_asAdmin_shouldUpdateStatus() throws Exception {
        mockMvc.perform(patch("/api/users/" + studentId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "SUSPENDED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountStatus").value("SUSPENDED"));
    }

    @Test
    void updateUserStatus_asStudent_shouldReturn403() throws Exception {
        mockMvc.perform(patch("/api/users/" + studentId + "/status")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "SUSPENDED"))))
                .andExpect(status().isForbidden());
    }

    // POST /api/users (admin create user)

    @Test
    void createUser_asAdmin_shouldReturn201() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "New Guard",
                                "email", "guard@usertest.com",
                                "password", "Guard123!",
                                "role", "SECURITY"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("guard@usertest.com"));
    }

    @Test
    void createUser_asStudent_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "New Guard",
                                "email", "guard2@usertest.com",
                                "password", "Guard123!",
                                "role", "SECURITY"
                        ))))
                .andExpect(status().isForbidden());
    }
}

package com.campusgate.exception;

import com.campusgate.controller.AuthController;
import com.campusgate.security.JwtService;
import com.campusgate.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Map;

import com.campusgate.entity.AccountStatus;
import com.campusgate.entity.Role;
import com.campusgate.entity.User;
import com.campusgate.security.UserDetailsImpl;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @TestConfiguration
    static class SecurityTestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    // 409 ConflictException

    @Test
    void register_duplicateEmail_shouldReturn409() throws Exception {
        when(authService.register(any())).thenThrow(new ConflictException("Email already in use"));

        Map<String, Object> body = Map.of(
                "fullName", "Test User",
                "email", "dup@test.com",
                "password", "Pass123!",
                "role", "STUDENT"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email already in use"));
    }

    // 409 DataIntegrityViolationException

    @Test
    void register_dbConstraintViolation_shouldReturn409() throws Exception {
        when(authService.register(any()))
                .thenThrow(new DataIntegrityViolationException("unique constraint violation",
                        new RuntimeException("Unique index or primary key violation")));

        Map<String, Object> body = Map.of(
                "fullName", "Test User",
                "email", "dup2@test.com",
                "password", "Pass123!",
                "role", "STUDENT"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("unique value already exists")));
    }

    // 401 BadCredentialsException

    @Test
    void login_badCredentials_shouldReturn401() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        Map<String, Object> body = Map.of("email", "user@test.com", "password", "wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    // 401 AuthenticationException

    @Test
    void login_authenticationException_shouldReturn401() throws Exception {
        when(authService.login(any())).thenThrow(new InsufficientAuthenticationException("Full auth required"));

        Map<String, Object> body = Map.of("email", "user@test.com", "password", "secret");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Full auth required"));
    }

    // 404 ResourceNotFoundException

    @Test
    void login_userNotFound_shouldReturn404() throws Exception {
        when(authService.login(any())).thenThrow(new ResourceNotFoundException("User not found"));

        Map<String, Object> body = Map.of("email", "unknown@test.com", "password", "Pass123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    // 403 AccessDeniedException

    @Test
    void login_accessDenied_shouldReturn403() throws Exception {
        when(authService.login(any())).thenThrow(new AccessDeniedException("Forbidden action"));

        Map<String, Object> body = Map.of("email", "denied@test.com", "password", "Pass123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value(containsString("Access denied: Forbidden action")));
    }

    // 400 MethodArgumentNotValidException

    @Test
    void register_missingRequiredFields_shouldReturn400WithFieldErrors() throws Exception {
        Map<String, Object> body = Map.of();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").exists());
    }

    // 400 IllegalArgumentException

    @Test
    void changePassword_wrongCurrentPassword_shouldReturn400() throws Exception {
        User mockUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .passwordHash("hashed")
                .role(Role.STUDENT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        UserDetailsImpl userDetails = new UserDetailsImpl(mockUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        try {
            doThrow(new IllegalArgumentException("Invalid current password"))
                    .when(authService).changePassword(any(), any(), any());

            Map<String, Object> body = Map.of("currentPassword", "wrong", "newPassword", "newPass123!");

            mockMvc.perform(post("/api/auth/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid current password"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // 500 Generic Exception

    @Test
    void register_unhandledException_shouldReturn500() throws Exception {
        when(authService.register(any())).thenThrow(new RuntimeException("Database exploded"));

        Map<String, Object> body = Map.of(
                "fullName", "Crash User",
                "email", "crash@test.com",
                "password", "Pass123!",
                "role", "STUDENT"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(containsString("unexpected error occurred")));
    }
}

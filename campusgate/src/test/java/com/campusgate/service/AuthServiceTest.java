package com.campusgate.service;

import com.campusgate.dto.LoginRequest;
import com.campusgate.dto.RegisterRequest;
import com.campusgate.dto.TokenResponse;
import com.campusgate.entity.AccountStatus;
import com.campusgate.entity.Role;
import com.campusgate.entity.User;
import com.campusgate.repository.UserRepository;
import com.campusgate.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateUserAndReturnDto() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Alice Example");
        request.setEmail("alice@example.com");
        request.setPassword("secret123");
        request.setRole(Role.STUDENT);
        request.setStudentNumber("S12345");
        request.setPhoneNumber("0911222333");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-password");

        var result = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("Alice Example", savedUser.getFullName());
        assertEquals("alice@example.com", savedUser.getEmail());
        assertEquals("hashed-password", savedUser.getPasswordHash());
        assertEquals(Role.STUDENT, savedUser.getRole());
        assertEquals(AccountStatus.ACTIVE, savedUser.getAccountStatus());
        assertEquals("S12345", savedUser.getStudentNumber());
        assertEquals("0911222333", savedUser.getPhoneNumber());

        assertEquals("alice@example.com", result.getEmail());
        assertEquals(Role.STUDENT, result.getRole());
    }

    @Test
    void login_shouldAuthenticateAndReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("bob@example.com");
        request.setPassword("password123");

        User user = User.builder()
                .id(10L)
                .fullName("Bob")
                .email("bob@example.com")
                .passwordHash("hash")
                .role(Role.SECURITY)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        TokenResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void changePassword_shouldUpdatePasswordWhenCurrentMatches() {
        User user = User.builder()
                .id(7L)
                .fullName("Carol")
                .email("carol@example.com")
                .passwordHash("old-hash")
                .role(Role.STUDENT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        authService.changePassword(7L, "old-password", "new-password");

        assertEquals("new-hash", user.getPasswordHash());
        verify(userRepository).save(user);
    }
}

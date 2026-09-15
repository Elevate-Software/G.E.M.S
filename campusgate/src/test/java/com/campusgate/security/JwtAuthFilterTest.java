package com.campusgate.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_withoutAuthHeader_shouldPassThrough() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_withNonBearerHeader_shouldPassThrough() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_withBlacklistedToken_shouldReturn401() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer blacklisted-token");
        when(jwtService.isTokenBlacklisted("blacklisted-token")).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withValidToken_shouldAuthenticate() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtService.isTokenBlacklisted("valid-token")).thenReturn(false);
        when(jwtService.extractUsername("valid-token")).thenReturn("user@test.com");

        UserDetails userDetails = new User("user@test.com", "pass", List.of());
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid-token", userDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user@test.com", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternal_withInvalidToken_shouldNotAuthenticate() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtService.isTokenBlacklisted("invalid-token")).thenReturn(false);
        when(jwtService.extractUsername("invalid-token")).thenReturn("user@test.com");

        UserDetails userDetails = new User("user@test.com", "pass", List.of());
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("invalid-token", userDetails)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_whenExceptionThrown_shouldReturn401() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer malformed-token");
        when(jwtService.isTokenBlacklisted("malformed-token")).thenThrow(new RuntimeException("Malformed token"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }
}

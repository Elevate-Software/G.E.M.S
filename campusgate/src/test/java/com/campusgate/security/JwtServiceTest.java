package com.campusgate.security;

import com.campusgate.entity.AccountStatus;
import com.campusgate.entity.Role;
import com.campusgate.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    private JwtService jwtService;

    private static final String SECRET = "test-secret-key-for-campusgate-ci-pipeline-must-be-at-least-256-bits-xyz123abc";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(redisTemplate);
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 600_000L);
    }

    private UserDetailsImpl buildUserDetails(String email, Role role) {
        User user = User.builder()
                .id(1L)
                .fullName("Test User")
                .email(email)
                .passwordHash("hash")
                .role(role)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        return new UserDetailsImpl(user);
    }

    // generateToken & extractUsername

    @Test
    void generateToken_shouldReturnNonNullToken() {
        UserDetailsImpl user = buildUserDetails("alice@test.com", Role.STUDENT);
        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractUsername_shouldReturnSubjectFromToken() {
        UserDetailsImpl user = buildUserDetails("alice@test.com", Role.STUDENT);
        String token = jwtService.generateToken(user);
        assertEquals("alice@test.com", jwtService.extractUsername(token));
    }

    @Test
    void extractJti_shouldReturnNonNullJtiFromToken() {
        UserDetailsImpl user = buildUserDetails("alice@test.com", Role.STUDENT);
        String token = jwtService.generateToken(user);
        assertNotNull(jwtService.extractJti(token));
    }

    @Test
    void generateToken_withExtraClaims_shouldEmbedClaims() {
        UserDetailsImpl user = buildUserDetails("bob@test.com", Role.ADMIN);
        String token = jwtService.generateToken(new java.util.HashMap<>(), user);
        String username = jwtService.extractUsername(token);
        assertEquals("bob@test.com", username);
    }

    // isTokenValid

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        UserDetailsImpl user = buildUserDetails("carol@test.com", Role.SECURITY);
        String token = jwtService.generateToken(user);
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValid_shouldReturnFalseForDifferentUser() {
        UserDetailsImpl alice = buildUserDetails("alice@test.com", Role.STUDENT);
        UserDetailsImpl bob = buildUserDetails("bob@test.com", Role.STUDENT);
        String aliceToken = jwtService.generateToken(alice);
        assertFalse(jwtService.isTokenValid(aliceToken, bob));
    }

    @Test
    void isTokenValid_shouldReturnFalseForBlacklistedToken() {
        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        UserDetailsImpl user = buildUserDetails("dave@test.com", Role.STUDENT);
        String token = jwtService.generateToken(user);
        assertFalse(jwtService.isTokenValid(token, user));
    }

    // blacklistToken

    @Test
    void blacklistToken_shouldStoreTokenInRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        UserDetailsImpl user = buildUserDetails("eve@test.com", Role.STUDENT);
        String token = jwtService.generateToken(user);
        jwtService.blacklistToken(token);
        verify(valueOps, atLeastOnce()).set(anyString(), eq("true"), anyLong(), any());
    }

    @Test
    void blacklistToken_shouldNotThrowWhenRedisUnavailable() {
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis down"));
        UserDetailsImpl user = buildUserDetails("eve@test.com", Role.STUDENT);
        String token = jwtService.generateToken(user);
        // Should not throw — fail-open
        assertDoesNotThrow(() -> jwtService.blacklistToken(token));
    }

    // isTokenBlacklisted

    @Test
    void isTokenBlacklisted_shouldReturnTrueWhenKeyExists() {
        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        UserDetailsImpl user = buildUserDetails("frank@test.com", Role.STUDENT);
        String token = jwtService.generateToken(user);
        assertTrue(jwtService.isTokenBlacklisted(token));
    }

    @Test
    void isTokenBlacklisted_shouldReturnFalseWhenKeyAbsent() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        UserDetailsImpl user = buildUserDetails("grace@test.com", Role.STUDENT);
        String token = jwtService.generateToken(user);
        assertFalse(jwtService.isTokenBlacklisted(token));
    }

    @Test
    void isTokenBlacklisted_shouldReturnFalseWhenRedisUnavailable() {
        when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("Redis connection refused"));
        UserDetailsImpl user = buildUserDetails("heidi@test.com", Role.STUDENT);
        String token = jwtService.generateToken(user);
        // Fail-open: returns false instead of throwing
        assertFalse(jwtService.isTokenBlacklisted(token));
    }

    @Test
    void generateToken_withGenericUserDetails_shouldSucceedWithoutUserDetailsImplFields() {
        org.springframework.security.core.userdetails.User genericUser =
                new org.springframework.security.core.userdetails.User("generic@test.com", "pass", java.util.List.of());
        String token = jwtService.generateToken(genericUser);
        assertNotNull(token);
        assertEquals("generic@test.com", jwtService.extractUsername(token));
    }

    @Test
    void isTokenValid_whenUsernameDoesNotMatch_shouldReturnFalse() {
        UserDetailsImpl user1 = buildUserDetails("user1@test.com", Role.STUDENT);
        UserDetailsImpl user2 = buildUserDetails("user2@test.com", Role.STUDENT);
        String token = jwtService.generateToken(user1);
        assertFalse(jwtService.isTokenValid(token, user2));
    }

    @Test
    void isTokenValid_whenTokenIsBlacklisted_shouldReturnFalse() {
        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        UserDetailsImpl user = buildUserDetails("blacklisted@test.com", Role.STUDENT);
        String token = jwtService.generateToken(user);
        assertFalse(jwtService.isTokenValid(token, user));
    }
}

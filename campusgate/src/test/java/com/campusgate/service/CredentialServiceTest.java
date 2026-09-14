package com.campusgate.service;

import com.campusgate.dto.CredentialDTO;
import com.campusgate.entity.*;
import com.campusgate.exception.ResourceNotFoundException;
import com.campusgate.repository.AccessCredentialRepository;
import com.campusgate.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CredentialService
 */
@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    @Mock
    private AccessCredentialRepository credentialRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CredentialService credentialService;

    @Test
    void getMyCredential_shouldReturnDtoWhenCredentialExists() {
        User user = User.builder().id(1L).fullName("Alice").build();
        AccessCredential credential = AccessCredential.builder()
                .id(10L)
                .user(user)
                .authToken("token-abc")
                .status(CredentialStatus.VALID)
                .issueDate(LocalDateTime.now().minusDays(1))
                .expiryDate(LocalDateTime.now().plusDays(30))
                .build();

        when(credentialRepository.findByUserIdAndStatus(1L, CredentialStatus.VALID))
                .thenReturn(Optional.of(credential));

        CredentialDTO result = credentialService.getMyCredential(1L);

        assertNotNull(result);
        assertEquals("token-abc", result.getAuthToken());
        assertEquals(CredentialStatus.VALID, result.getStatus());
        assertEquals(1L, result.getUserId());
    }

    @Test
    void getMyCredential_shouldThrowWhenNoActiveCredential() {
        when(credentialRepository.findByUserIdAndStatus(99L, CredentialStatus.VALID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> credentialService.getMyCredential(99L));
        assertEquals("No active credential found", ex.getMessage());
    }
    
    @Test
    void generateCredential_shouldCreateNewWhenNoneExists() {
        User user = User.builder().id(2L).fullName("Bob").build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(credentialRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(credentialRepository.save(any(AccessCredential.class)))
                .thenAnswer(inv -> {
                    AccessCredential c = inv.getArgument(0);
                    c.setId(20L);
                    return c;
                });

        CredentialDTO result = credentialService.generateCredential(2L);

        ArgumentCaptor<AccessCredential> captor = ArgumentCaptor.forClass(AccessCredential.class);
        verify(credentialRepository).save(captor.capture());

        AccessCredential saved = captor.getValue();
        assertEquals(CredentialStatus.VALID, saved.getStatus());
        assertNotNull(saved.getAuthToken());
        assertEquals(user, saved.getUser());
        assertTrue(saved.getExpiryDate().isAfter(LocalDateTime.now().plusDays(364)));

        assertNotNull(result);
        assertEquals(CredentialStatus.VALID, result.getStatus());
    }

    // existing credential (renews / overwrites token)
    @Test
    void generateCredential_shouldRenewExistingCredential() {
        User user = User.builder().id(3L).fullName("Carol").build();
        AccessCredential existing = AccessCredential.builder()
                .id(30L)
                .user(user)
                .authToken("old-token")
                .status(CredentialStatus.VALID)
                .issueDate(LocalDateTime.now().minusYears(1))
                .expiryDate(LocalDateTime.now().minusDays(5))
                .build();

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(credentialRepository.findByUserId(3L)).thenReturn(Optional.of(existing));
        when(credentialRepository.save(any(AccessCredential.class))).thenReturn(existing);

        CredentialDTO result = credentialService.generateCredential(3L);

        // Token should be replaced
        assertNotEquals("old-token", existing.getAuthToken());
        // Status should be back to VALID
        assertEquals(CredentialStatus.VALID, existing.getStatus());
        // New expiry should be in the future
        assertTrue(existing.getExpiryDate().isAfter(LocalDateTime.now()));

        verify(credentialRepository).save(existing);
        assertEquals(CredentialStatus.VALID, result.getStatus());
    }

    // user not found
    @Test
    void generateCredential_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> credentialService.generateCredential(999L));
        assertEquals("User not found", ex.getMessage());
        verify(credentialRepository, never()).save(any());
    }
}

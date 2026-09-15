package com.campusgate.service;

import com.campusgate.dto.ScanRequest;
import com.campusgate.dto.ScanResultDTO;
import com.campusgate.entity.*;
import com.campusgate.repository.AccessCredentialRepository;
import com.campusgate.repository.EntryLogRepository;
import com.campusgate.repository.GateRepository;
import com.campusgate.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScanServiceTest {

    @Mock
    private AccessCredentialRepository credentialRepository;

    @Mock
    private GateRepository gateRepository;

    @Mock
    private EntryLogRepository entryLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ScanService scanService;

    @Test
    void scanCredential_shouldGrantAccessForValidCredential() {
        ScanRequest request = new ScanRequest();
        request.setGateId(1L);
        request.setToken("valid-token");
        request.setDirection(Direction.ENTRY);

        Gate gate = Gate.builder().id(1L).gateName("Main Gate").isOperational(true).build();
        User student = User.builder().id(10L).fullName("Finn").studentNumber("S001").accountStatus(AccountStatus.ACTIVE).build();
        AccessCredential credential = AccessCredential.builder()
                .user(student)
                .authToken("valid-token")
                .status(CredentialStatus.VALID)
                .issueDate(LocalDateTime.now().minusDays(1))
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        when(gateRepository.findById(1L)).thenReturn(Optional.of(gate));
        when(credentialRepository.findByAuthTokenAndStatus("valid-token", CredentialStatus.VALID)).thenReturn(Optional.of(credential));
        when(entryLogRepository.save(any(EntryLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ScanResultDTO result = scanService.scanCredential(request, null);

        assertEquals(AccessResult.GRANTED, result.getResult());
        assertEquals("Access Granted", result.getMessage());
        assertNotNull(result.getUser());
        assertEquals("Finn", result.getUser().getFullName());
        verify(entryLogRepository).save(any(EntryLog.class));
    }

    @Test
    void scanCredential_shouldDenyAccessWhenTokenExpired() {
        ScanRequest request = new ScanRequest();
        request.setGateId(2L);
        request.setToken("expired-token");
        request.setDirection(Direction.EXIT);

        Gate gate = Gate.builder().id(2L).gateName("North Gate").isOperational(true).build();
        User student = User.builder().id(11L).fullName("Mina").accountStatus(AccountStatus.ACTIVE).build();
        AccessCredential credential = AccessCredential.builder()
                .user(student)
                .authToken("expired-token")
                .status(CredentialStatus.VALID)
                .issueDate(LocalDateTime.now().minusDays(2))
                .expiryDate(LocalDateTime.now().minusDays(1))
                .build();

        when(gateRepository.findById(2L)).thenReturn(Optional.of(gate));
        when(credentialRepository.findByAuthTokenAndStatus("expired-token", CredentialStatus.VALID)).thenReturn(Optional.of(credential));

        ScanResultDTO result = scanService.scanCredential(request, null);

        assertEquals(AccessResult.DENIED, result.getResult());
        assertEquals("Token Expired", result.getMessage());
        assertNull(result.getUser());
    }

    @Test
    void scanCredential_shouldThrowWhenGateNotFound() {
        ScanRequest request = new ScanRequest();
        request.setGateId(999L);
        request.setToken("any-token");
        request.setDirection(Direction.ENTRY);

        when(gateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(com.campusgate.exception.ResourceNotFoundException.class,
                () -> scanService.scanCredential(request, null));
    }

    @Test
    void scanCredential_shouldDenyWhenTokenNotFound() {
        ScanRequest request = new ScanRequest();
        request.setGateId(1L);
        request.setToken("unknown-token");
        request.setDirection(Direction.ENTRY);

        Gate gate = Gate.builder().id(1L).gateName("Main Gate").isOperational(true).build();
        when(gateRepository.findById(1L)).thenReturn(Optional.of(gate));
        when(credentialRepository.findByAuthTokenAndStatus("unknown-token", CredentialStatus.VALID))
                .thenReturn(Optional.empty());

        ScanResultDTO result = scanService.scanCredential(request, null);

        assertEquals(AccessResult.DENIED, result.getResult());
        assertEquals("Invalid or Revoked Token", result.getMessage());
        assertNull(result.getUser());
    }

    @Test
    void scanCredential_shouldDenyWhenAccountSuspended() {
        ScanRequest request = new ScanRequest();
        request.setGateId(3L);
        request.setToken("suspended-token");
        request.setDirection(Direction.ENTRY);

        Gate gate = Gate.builder().id(3L).gateName("West Gate").isOperational(true).build();
        User suspended = User.builder().id(20L).fullName("Sus").accountStatus(AccountStatus.SUSPENDED).build();
        AccessCredential credential = AccessCredential.builder()
                .user(suspended)
                .authToken("suspended-token")
                .status(CredentialStatus.VALID)
                .issueDate(LocalDateTime.now().minusDays(1))
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        when(gateRepository.findById(3L)).thenReturn(Optional.of(gate));
        when(credentialRepository.findByAuthTokenAndStatus("suspended-token", CredentialStatus.VALID))
                .thenReturn(Optional.of(credential));
        when(entryLogRepository.save(any(EntryLog.class))).thenAnswer(inv -> inv.getArgument(0));

        ScanResultDTO result = scanService.scanCredential(request, null);

        assertEquals(AccessResult.DENIED, result.getResult());
        assertTrue(result.getMessage().contains("SUSPENDED"));
    }

    @Test
    void scanCredential_shouldResolveScannedByWhenIdProvided() {
        ScanRequest request = new ScanRequest();
        request.setGateId(1L);
        request.setToken("valid-with-scanner");
        request.setDirection(Direction.ENTRY);

        Gate gate = Gate.builder().id(1L).gateName("Main Gate").isOperational(true).build();
        User student = User.builder().id(10L).fullName("Alice").studentNumber("S001").accountStatus(AccountStatus.ACTIVE).build();
        User security = User.builder().id(99L).fullName("Guard").role(com.campusgate.entity.Role.SECURITY).accountStatus(AccountStatus.ACTIVE).build();
        AccessCredential credential = AccessCredential.builder()
                .user(student)
                .authToken("valid-with-scanner")
                .status(CredentialStatus.VALID)
                .issueDate(LocalDateTime.now().minusDays(1))
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        when(gateRepository.findById(1L)).thenReturn(Optional.of(gate));
        when(credentialRepository.findByAuthTokenAndStatus("valid-with-scanner", CredentialStatus.VALID))
                .thenReturn(Optional.of(credential));
        when(userRepository.findById(99L)).thenReturn(Optional.of(security));
        when(entryLogRepository.save(any(EntryLog.class))).thenAnswer(inv -> inv.getArgument(0));

        ScanResultDTO result = scanService.scanCredential(request, 99L);

        assertEquals(AccessResult.GRANTED, result.getResult());
        verify(userRepository).findById(99L);
    }
}

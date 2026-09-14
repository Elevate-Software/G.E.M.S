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
}

package com.campusgate.service;

import com.campusgate.dto.ScanRequest;
import com.campusgate.dto.ScanResultDTO;
import com.campusgate.entity.*;
import com.campusgate.repository.AccessCredentialRepository;
import com.campusgate.repository.EntryLogRepository;
import com.campusgate.repository.GateRepository;
import com.campusgate.repository.UserRepository;
import com.campusgate.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ScanService {

    private final AccessCredentialRepository credentialRepository;
    private final GateRepository gateRepository;
    private final EntryLogRepository entryLogRepository;
    private final UserRepository userRepository;

    public ScanService(AccessCredentialRepository credentialRepository, GateRepository gateRepository, EntryLogRepository entryLogRepository, UserRepository userRepository) {
        this.credentialRepository = credentialRepository;
        this.gateRepository = gateRepository;
        this.entryLogRepository = entryLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ScanResultDTO scanCredential(ScanRequest request, Long scannedById) {
        Gate gate = gateRepository.findById(request.getGateId())
                .orElseThrow(() -> new ResourceNotFoundException("Gate not found"));

        User scannedBy = null;
        if (scannedById != null) {
            scannedBy = userRepository.findById(scannedById).orElse(null);
        }

        AccessCredential credential = credentialRepository.findByAuthTokenAndStatus(request.getToken(), CredentialStatus.VALID)
                .orElse(null);

        AccessResult result = AccessResult.DENIED;
        String reason = null;
        User student = null;

        if (credential == null) {
            reason = "Invalid or Revoked Token";
        } else if (credential.getExpiryDate().isBefore(LocalDateTime.now())) {
            reason = "Token Expired";
        } else {
            student = credential.getUser();
            if (student.getAccountStatus() != AccountStatus.ACTIVE) {
                reason = "Account " + student.getAccountStatus().name();
            } else if (!gate.isOperational()) {
                reason = "Gate is not operational";
            } else {
                result = AccessResult.GRANTED;
            }
        }

        if (student != null) {
            EntryLog log = EntryLog.builder()
                    .user(student)
                    .gate(gate)
                    .timestamp(LocalDateTime.now())
                    .direction(request.getDirection())
                    .accessResult(result)
                    .denialReason(reason)
                    .scannedBy(scannedBy)
                    .build();
            entryLogRepository.save(log);
        }

        com.campusgate.dto.UserDTO userDTO = null;
        if (student != null) {
            userDTO = com.campusgate.dto.UserDTO.builder()
                    .id(student.getId())
                    .fullName(student.getFullName())
                    .studentNumber(student.getStudentNumber())
                    .accountStatus(student.getAccountStatus())
                    .build();
        }

        return ScanResultDTO.builder()
                .result(result)
                .message(reason == null ? "Access Granted" : reason)
                .user(userDTO)
                .build();
    }
}

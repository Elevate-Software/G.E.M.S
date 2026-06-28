package com.campusgate.service;

import com.campusgate.dto.CredentialDTO;
import com.campusgate.entity.AccessCredential;
import com.campusgate.entity.CredentialStatus;
import com.campusgate.entity.User;
import com.campusgate.repository.AccessCredentialRepository;
import com.campusgate.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CredentialService {
    private final AccessCredentialRepository credentialRepository;
    private final UserRepository userRepository;

    public CredentialService(AccessCredentialRepository credentialRepository, UserRepository userRepository) {
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
    }

    public CredentialDTO getMyCredential(Long userId) {
        AccessCredential credential = credentialRepository.findByUserIdAndStatus(userId, CredentialStatus.VALID)
                .orElseThrow(() -> new RuntimeException("No active credential found"));
        return mapToDTO(credential);
    }

    @Transactional
    public CredentialDTO generateCredential(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime now = LocalDateTime.now();
        AccessCredential credential = credentialRepository.findByUserId(userId)
                .map(existing -> {
                    existing.setAuthToken(UUID.randomUUID().toString());
                    existing.setIssueDate(now);
                    existing.setExpiryDate(now.plusYears(1));
                    existing.setStatus(CredentialStatus.VALID);
                    return existing;
                })
                .orElseGet(() -> AccessCredential.builder()
                        .user(user)
                        .authToken(UUID.randomUUID().toString())
                        .issueDate(now)
                        .expiryDate(now.plusYears(1))
                        .status(CredentialStatus.VALID)
                        .build());

        return mapToDTO(credentialRepository.save(credential));
    }

    private CredentialDTO mapToDTO(AccessCredential credential) {
        return CredentialDTO.builder()
                .id(credential.getId())
                .userId(credential.getUser().getId())
                .authToken(credential.getAuthToken())
                .issueDate(credential.getIssueDate())
                .expiryDate(credential.getExpiryDate())
                .status(credential.getStatus())
                .build();
    }
}

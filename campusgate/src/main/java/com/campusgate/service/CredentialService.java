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
import java.util.Optional;
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

        // Revoke any existing valid credentials
        Optional<AccessCredential> existing = credentialRepository.findByUserIdAndStatus(userId, CredentialStatus.VALID);
        if (existing.isPresent()) {
            AccessCredential old = existing.get();
            old.setStatus(CredentialStatus.REVOKED);
            credentialRepository.save(old);
        }

        // Generate a new credential
        AccessCredential credential = AccessCredential.builder()
                .user(user)
                .authToken(UUID.randomUUID().toString())
                .issueDate(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusYears(1)) // Expires in 1 year
                .status(CredentialStatus.VALID)
                .build();

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

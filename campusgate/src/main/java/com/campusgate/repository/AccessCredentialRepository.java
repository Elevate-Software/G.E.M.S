package com.campusgate.repository;

import com.campusgate.entity.AccessCredential;
import com.campusgate.entity.CredentialStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccessCredentialRepository extends JpaRepository<AccessCredential, Long> {
    Optional<AccessCredential> findByAuthTokenAndStatus(String authToken, CredentialStatus status);
    Optional<AccessCredential> findByUserIdAndStatus(Long userId, CredentialStatus status);
    Optional<AccessCredential> findByUserId(Long userId);
}

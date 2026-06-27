package com.campusgate.controller;

import com.campusgate.dto.CredentialDTO;
import com.campusgate.service.CredentialService;
import com.campusgate.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/credentials")
public class CredentialController {
    private final CredentialService credentialService;

    public CredentialController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @GetMapping("/my-credential")
    public ResponseEntity<CredentialDTO> getMyCredential(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Unauthorized");
        }
        return ResponseEntity.ok(credentialService.getMyCredential(userDetails.getId()));
    }

    @PostMapping("/generate")
    public ResponseEntity<CredentialDTO> generateCredential(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Unauthorized");
        }
        return ResponseEntity.ok(credentialService.generateCredential(userDetails.getId()));
    }
}

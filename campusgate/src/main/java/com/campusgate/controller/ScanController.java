package com.campusgate.controller;

import com.campusgate.dto.ScanRequest;
import com.campusgate.dto.ScanResultDTO;
import com.campusgate.service.ScanService;
import com.campusgate.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scan")
public class ScanController {

    private final ScanService scanService;

    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SECURITY', 'ADMIN')")
    public ResponseEntity<ScanResultDTO> scan(@Valid @RequestBody ScanRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long scannedById = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(scanService.scanCredential(request, scannedById));
    }
}

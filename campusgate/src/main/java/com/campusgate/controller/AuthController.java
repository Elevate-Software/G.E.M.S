package com.campusgate.controller;

import com.campusgate.dto.LoginRequest;
import com.campusgate.dto.RegisterRequest;
import com.campusgate.dto.TokenResponse;
import com.campusgate.dto.UserDTO;
import com.campusgate.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7));
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody java.util.Map<String, String> request, @org.springframework.security.core.annotation.AuthenticationPrincipal com.campusgate.security.UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Unauthorized");
        }
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        authService.changePassword(userDetails.getId(), currentPassword, newPassword);
        return ResponseEntity.ok().build();
    }
}

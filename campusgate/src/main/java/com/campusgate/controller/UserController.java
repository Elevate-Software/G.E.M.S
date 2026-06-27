package com.campusgate.controller;

import com.campusgate.dto.UserDTO;
import com.campusgate.entity.AccountStatus;
import com.campusgate.service.UserService;
import com.campusgate.security.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final com.campusgate.service.AuthService authService;

    public UserController(UserService userService, com.campusgate.service.AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECURITY')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        AccountStatus status = AccountStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(userService.updateStatus(id, status));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.getUserById(userDetails.getId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@jakarta.validation.Valid @RequestBody com.campusgate.dto.RegisterRequest request) {
        return new ResponseEntity<>(authService.register(request), org.springframework.http.HttpStatus.CREATED);
    }
}

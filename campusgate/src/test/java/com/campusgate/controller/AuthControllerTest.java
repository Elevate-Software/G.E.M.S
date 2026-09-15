package com.campusgate.controller;

import com.campusgate.dto.ChangePasswordRequest;
import com.campusgate.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void logout_withNonBearerHeader_shouldNotCallLogoutService() {
        ResponseEntity<Void> response = authController.logout("Basic dXNlcjpwYXNz");
        assertEquals(204, response.getStatusCode().value());
        verify(authService, never()).logout(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void logout_withNullHeader_shouldNotCallLogoutService() {
        ResponseEntity<Void> response = authController.logout(null);
        assertEquals(204, response.getStatusCode().value());
        verify(authService, never()).logout(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void changePassword_whenUserDetailsNull_shouldThrowRuntimeException() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPass");
        request.setNewPassword("newPass");
        assertThrows(RuntimeException.class, () -> authController.changePassword(request, null));
    }
}

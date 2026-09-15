package com.campusgate.controller;

import com.campusgate.service.CredentialService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CredentialControllerTest {

    @Mock
    private CredentialService credentialService;

    @InjectMocks
    private CredentialController credentialController;

    @Test
    void getMyCredential_whenUserDetailsNull_shouldThrowRuntimeException() {
        assertThrows(RuntimeException.class, () -> credentialController.getMyCredential(null));
    }

    @Test
    void generateCredential_whenUserDetailsNull_shouldThrowRuntimeException() {
        assertThrows(RuntimeException.class, () -> credentialController.generateCredential(null));
    }
}

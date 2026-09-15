package com.campusgate.controller;

import com.campusgate.dto.ScanRequest;
import com.campusgate.dto.ScanResultDTO;
import com.campusgate.entity.AccessResult;
import com.campusgate.entity.Direction;
import com.campusgate.service.ScanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScanControllerTest {

    @Mock
    private ScanService scanService;

    @InjectMocks
    private ScanController scanController;

    @Test
    void scan_whenUserDetailsNull_shouldPassNullScannedById() {
        ScanRequest request = new ScanRequest();
        request.setGateId(1L);
        request.setToken("token-123");
        request.setDirection(Direction.ENTRY);

        ScanResultDTO mockResult = ScanResultDTO.builder()
                .result(AccessResult.GRANTED)
                .message("Access granted")
                .build();

        when(scanService.scanCredential(any(ScanRequest.class), isNull())).thenReturn(mockResult);

        ResponseEntity<ScanResultDTO> response = scanController.scan(request, null);

        assertNotNull(response.getBody());
        assertEquals(AccessResult.GRANTED, response.getBody().getResult());
        verify(scanService).scanCredential(request, null);
    }
}

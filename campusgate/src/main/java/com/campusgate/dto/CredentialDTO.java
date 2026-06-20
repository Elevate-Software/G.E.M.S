package com.campusgate.dto;

import com.campusgate.entity.CredentialStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CredentialDTO {
    private Long id;
    private Long userId;
    private String authToken;
    private LocalDateTime issueDate;
    private LocalDateTime expiryDate;
    private CredentialStatus status;
}

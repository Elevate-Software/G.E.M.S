package com.campusgate.dto;

import com.campusgate.entity.AccessResult;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScanResultDTO {
    private AccessResult result;
    private String message;
    private UserDTO user;
}

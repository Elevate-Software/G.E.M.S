package com.campusgate.dto;

import com.campusgate.entity.Direction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScanRequest {
    @NotBlank
    private String token;
    @NotNull
    private Long gateId;
    @NotNull
    private Direction direction;
}

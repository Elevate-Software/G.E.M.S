package com.campusgate.dto;

import com.campusgate.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull(message = "Status must not be null")
    private AccountStatus status;
}

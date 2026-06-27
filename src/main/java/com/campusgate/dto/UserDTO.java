package com.campusgate.dto;

import com.campusgate.entity.AccountStatus;
import com.campusgate.entity.Role;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private AccountStatus accountStatus;
    private String studentNumber;
    private String phoneNumber;
    private LocalDateTime createdAt;
}

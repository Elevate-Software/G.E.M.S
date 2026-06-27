package com.campusgate.dto;

import com.campusgate.entity.AccessResult;
import com.campusgate.entity.Direction;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class EntryLogDTO {
    private Long id;
    private Long userId;
    private Long gateId;
    private LocalDateTime timestamp;
    private Direction direction;
    private AccessResult accessResult;
    private String denialReason;
    private Long scannedById;
}

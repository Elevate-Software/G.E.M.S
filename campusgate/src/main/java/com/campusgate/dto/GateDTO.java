package com.campusgate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GateDTO {
    private Long id;
    private String gateName;
    private String location;
    private boolean isOperational;
    private LocalTime operatingHoursStart;
    private LocalTime operatingHoursEnd;
}

package com.campusgate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "gates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "gate_name", nullable = false, length = 100)
    private String gateName;

    @Column(length = 200)
    private String location;

    @Column(name = "is_operational", nullable = false)
    @Builder.Default
    private boolean isOperational = true;

    @Column(name = "operating_hours_start")
    private LocalTime operatingHoursStart;

    @Column(name = "operating_hours_end")
    private LocalTime operatingHoursEnd;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "gate", fetch = FetchType.LAZY)
    private List<EntryLog> entryLogs;
}

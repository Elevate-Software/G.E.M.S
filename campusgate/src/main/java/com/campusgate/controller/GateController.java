package com.campusgate.controller;

import com.campusgate.dto.GateDTO;
import com.campusgate.repository.GateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gates")
public class GateController {
    private final GateRepository gateRepository;

    public GateController(GateRepository gateRepository) {
        this.gateRepository = gateRepository;
    }

    @GetMapping
    public ResponseEntity<List<GateDTO>> getAllGates() {
        List<GateDTO> gates = gateRepository.findAll().stream()
                .map(gate -> GateDTO.builder()
                        .id(gate.getId())
                        .gateName(gate.getGateName())
                        .location(gate.getLocation())
                        .isOperational(gate.isOperational())
                        .operatingHoursStart(gate.getOperatingHoursStart())
                        .operatingHoursEnd(gate.getOperatingHoursEnd())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(gates);
    }
}

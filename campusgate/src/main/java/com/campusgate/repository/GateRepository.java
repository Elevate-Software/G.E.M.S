package com.campusgate.repository;

import com.campusgate.entity.Gate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GateRepository extends JpaRepository<Gate, Long> {
}

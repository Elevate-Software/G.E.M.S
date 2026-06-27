package com.campusgate.repository;

import com.campusgate.entity.IncidentReport;
import com.campusgate.entity.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentReportRepository extends JpaRepository<IncidentReport, Long> {
    Page<IncidentReport> findByStatus(ReportStatus status, Pageable pageable);
}

package com.campusgate.repository;

import com.campusgate.entity.EntryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EntryLogRepository extends JpaRepository<EntryLog, Long>, JpaSpecificationExecutor<EntryLog> {
    Page<EntryLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
}

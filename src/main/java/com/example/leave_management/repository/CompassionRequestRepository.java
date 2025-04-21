package com.example.leave_management.repository;

import com.example.leave_management.model.CompassionRequest;
import com.example.leave_management.model.CompassionRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface CompassionRequestRepository extends JpaRepository<CompassionRequest, UUID> {
    List<CompassionRequest> findByEmployeeId(UUID employeeId);

    List<CompassionRequest> findByWorkDate(LocalDate workDate);

    List<CompassionRequest> findByStatus(CompassionRequestStatus status);
}
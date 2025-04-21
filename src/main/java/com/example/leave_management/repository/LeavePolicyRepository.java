package com.example.leave_management.repository;

import com.example.leave_management.model.LeavePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, UUID> {

    LeavePolicy findFirstByOrderByCreatedAtAsc();
}
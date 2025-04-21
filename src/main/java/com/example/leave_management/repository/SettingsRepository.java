package com.example.leave_management.repository;

import com.example.leave_management.model.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, UUID> {
    // We'll only have one settings record
    Settings findFirstByOrderBySettingsIdAsc();

    Settings findFirstByOrderByCreatedAtAsc();
}
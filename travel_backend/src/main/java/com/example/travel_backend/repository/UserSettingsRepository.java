package com.example.travel_backend.repository;

import com.example.travel_backend.entity.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSetting, UUID> {
}

package com.example.cams.repository;

import com.example.cams.entity.CamsSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CamsSettingRepository extends JpaRepository<CamsSetting, String> {
}

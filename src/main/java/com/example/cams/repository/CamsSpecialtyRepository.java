package com.example.cams.repository;

import com.example.cams.entity.CamsSpecialty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CamsSpecialtyRepository extends JpaRepository<CamsSpecialty, Long> {
    List<CamsSpecialty> findAllByOrderByNameAsc();

    Optional<CamsSpecialty> findByName(String name);
}

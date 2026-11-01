package com.example.cams.repository;

import com.example.cams.entity.CamsSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface CamsSessionRepository extends JpaRepository<CamsSession, String> {

    Optional<CamsSession> findByTokenHashAndExpiresAfter(String tokenHash, Instant now);

    @Transactional
    void deleteByTokenHash(String tokenHash);

    @Transactional
    void deleteByUserId(Long userId);
}

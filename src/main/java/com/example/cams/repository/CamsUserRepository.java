package com.example.cams.repository;

import com.example.cams.entity.CamsUser;
import com.example.cams.entity.enums.CamsRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CamsUserRepository extends JpaRepository<CamsUser, Long> {

    boolean existsByEmail(String email);

    Optional<CamsUser> findByEmailAndActiveTrue(String email);

    Optional<CamsUser> findByEmail(String email);

    Optional<CamsUser> findByIdAndActiveTrue(Long id);

    Optional<CamsUser> findByEmailAndRoleAndActiveTrue(String email, CamsRole role);

    List<CamsUser> findByRoleAndActiveTrueOrderByNameAsc(CamsRole role);

    List<CamsUser> findByRoleInOrderByIdAsc(Collection<CamsRole> roles);

    boolean existsByIdAndRoleAndActiveTrue(Long id, CamsRole role);
}

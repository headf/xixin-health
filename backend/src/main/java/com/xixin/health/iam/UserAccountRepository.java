package com.xixin.health.iam;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Page<UserAccount> findByBranchIdAndDeletedFalse(Long branchId, Pageable pageable);

    Optional<UserAccount> findByPhone(String phone);

    Optional<UserAccount> findByEmail(String email);

    boolean existsByPhone(String phone);

    @Query("SELECT u FROM UserAccount u JOIN u.roles r WHERE r = :role AND u.deleted = false AND u.enabled = true")
    List<UserAccount> findByRolesContaining(@Param("role") Role role);

    boolean existsByRoles_IdAndDeletedFalse(Long roleId);
}

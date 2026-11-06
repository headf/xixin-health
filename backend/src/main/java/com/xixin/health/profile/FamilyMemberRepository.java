package com.xixin.health.profile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

    List<FamilyMember> findByOwnerUserIdAndDeletedFalseOrderByIdAsc(Long ownerUserId);
}

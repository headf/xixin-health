package com.xixin.health.profile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HealthProfileRepository extends JpaRepository<HealthProfile, Long> {

    Optional<HealthProfile> findByOwnerUserIdAndProfileTypeAndDeletedFalse(Long ownerUserId, ProfileType profileType);

    List<HealthProfile> findByOwnerUserIdAndDeletedFalse(Long ownerUserId);
}

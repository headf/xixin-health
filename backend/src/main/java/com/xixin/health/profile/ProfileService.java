package com.xixin.health.profile;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;

import java.util.List;

@Service
public class ProfileService {

    private final HealthProfileRepository healthProfileRepository;
    private final FamilyMemberRepository familyMemberRepository;

    public ProfileService(HealthProfileRepository healthProfileRepository, FamilyMemberRepository familyMemberRepository) {
        this.healthProfileRepository = healthProfileRepository;
        this.familyMemberRepository = familyMemberRepository;
    }

    @Transactional
    public HealthProfile createOrUpdateProfile(
        Long ownerUserId,
        String name,
        String idCardNo,
        String phone,
        String allergyHistory,
        String medicalHistory,
        String emergencyContactName,
        String emergencyContactPhone,
        String profileType
    ) {
        ProfileType type = (profileType != null && !profileType.isBlank())
            ? ProfileType.valueOf(profileType) : ProfileType.SELF;
        HealthProfile profile = healthProfileRepository
            .findByOwnerUserIdAndProfileTypeAndDeletedFalse(ownerUserId, type)
            .orElseGet(() -> new HealthProfile(ownerUserId, type));
        profile.update(name, idCardNo, phone, allergyHistory, medicalHistory, emergencyContactName, emergencyContactPhone, type);
        return healthProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public HealthProfile getPersonalProfile(Long ownerUserId) {
        return healthProfileRepository.findByOwnerUserIdAndProfileTypeAndDeletedFalse(ownerUserId, ProfileType.SELF)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Personal profile not found"));
    }

    @Transactional
    public FamilyMember createFamilyMember(Long ownerUserId, String name, String relationship, String idCardNo, String phone) {
        return familyMemberRepository.save(new FamilyMember(ownerUserId, name, relationship, idCardNo, phone));
    }

    @Transactional
    public FamilyMember updateFamilyMember(Long ownerUserId, Long familyMemberId, String name, String relationship, String idCardNo, String phone) {
        FamilyMember familyMember = findOwnedFamilyMember(ownerUserId, familyMemberId);
        familyMember.update(name, relationship, idCardNo, phone);
        return familyMemberRepository.save(familyMember);
    }

    @Transactional(readOnly = true)
    public List<FamilyMember> listFamilyMembers(Long ownerUserId) {
        return familyMemberRepository.findByOwnerUserIdAndDeletedFalseOrderByIdAsc(ownerUserId);
    }

    @Transactional
    public void deleteFamilyMember(Long ownerUserId, Long familyMemberId) {
        FamilyMember familyMember = findOwnedFamilyMember(ownerUserId, familyMemberId);
        familyMember.markDeleted();
        familyMemberRepository.save(familyMember);
    }

    private FamilyMember findOwnedFamilyMember(Long ownerUserId, Long familyMemberId) {
        return familyMemberRepository.findById(familyMemberId)
            .filter(member -> member.getOwnerUserId().equals(ownerUserId) && !member.isDeleted())
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Family member not found"));
    }
}

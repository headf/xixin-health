package com.xixin.health.profile;

import com.xixin.health.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping("/me")
    public ApiResponse<HealthProfile> upsertProfile(
        @RequestParam Long ownerUserId,
        @Valid @RequestBody ProfileRequest request
    ) {
        return ApiResponse.success(profileService.createOrUpdateProfile(
            ownerUserId,
            request.name(),
            request.idCardNo(),
            request.phone(),
            request.allergyHistory(),
            request.medicalHistory(),
            request.emergencyContactName(),
            request.emergencyContactPhone(),
            request.profileType()
        ));
    }

    @GetMapping("/me")
    public ApiResponse<HealthProfile> personalProfile(@RequestParam Long ownerUserId) {
        return ApiResponse.success(profileService.getPersonalProfile(ownerUserId));
    }

    @PostMapping("/family")
    public ApiResponse<FamilyMember> createFamilyMember(
        @RequestParam Long ownerUserId,
        @Valid @RequestBody FamilyMemberRequest request
    ) {
        return ApiResponse.success(profileService.createFamilyMember(
            ownerUserId,
            request.name(),
            request.relationship(),
            request.idCardNo(),
            request.phone()
        ));
    }

    @GetMapping("/family")
    public ApiResponse<List<FamilyMember>> familyMembers(@RequestParam Long ownerUserId) {
        return ApiResponse.success(profileService.listFamilyMembers(ownerUserId));
    }

    @PutMapping("/family/{familyMemberId}")
    public ApiResponse<FamilyMember> updateFamilyMember(
        @RequestParam Long ownerUserId,
        @PathVariable Long familyMemberId,
        @Valid @RequestBody FamilyMemberRequest request
    ) {
        return ApiResponse.success(profileService.updateFamilyMember(
            ownerUserId,
            familyMemberId,
            request.name(),
            request.relationship(),
            request.idCardNo(),
            request.phone()
        ));
    }

    @DeleteMapping("/family/{familyMemberId}")
    public ApiResponse<Void> deleteFamilyMember(@RequestParam Long ownerUserId, @PathVariable Long familyMemberId) {
        profileService.deleteFamilyMember(ownerUserId, familyMemberId);
        return ApiResponse.success(null);
    }

    public record ProfileRequest(
        @NotBlank String name,
        String idCardNo,
        String phone,
        String allergyHistory,
        String medicalHistory,
        String emergencyContactName,
        String emergencyContactPhone,
        String profileType
    ) {
    }

    public record FamilyMemberRequest(
        @NotBlank String name,
        @NotBlank String relationship,
        String idCardNo,
        String phone
    ) {
    }
}

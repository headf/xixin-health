package com.xixin.health.profile;

import com.xixin.health.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/profiles")
public class ProfileQueryController {

    private final HealthProfileRepository healthProfileRepository;

    public ProfileQueryController(HealthProfileRepository healthProfileRepository) {
        this.healthProfileRepository = healthProfileRepository;
    }

    @GetMapping("/by-user")
    public ApiResponse<List<ProfileBrief>> getByUser(@RequestParam Long userId) {
        List<HealthProfile> profiles = healthProfileRepository.findByOwnerUserIdAndDeletedFalse(userId);
        return ApiResponse.success(profiles.stream()
            .map(p -> new ProfileBrief(p.getId(), p.getName(), p.getProfileType().name(), p.getPhone()))
            .toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<HealthProfile> getById(@PathVariable Long id) {
        return ApiResponse.success(healthProfileRepository.findById(id)
            .orElse(null));
    }

    public record ProfileBrief(Long id, String name, String profileType, String phone) {}
}

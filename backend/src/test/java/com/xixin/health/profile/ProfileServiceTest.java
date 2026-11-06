package com.xixin.health.profile;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class ProfileServiceTest {

    @Autowired
    ProfileService profileService;

    @Test
    void createsPersonalProfileForUser() {
        HealthProfile profile = profileService.createOrUpdateProfile(
            1L,
            "Zhang San",
            "110101199001010011",
            "13800000001",
            "Penicillin",
            "None",
            "Li Si",
            "13900000001",
            "SELF"
        );

        assertThat(profile.getOwnerUserId()).isEqualTo(1L);
        assertThat(profile.getName()).isEqualTo("Zhang San");
        assertThat(profile.getProfileType()).isEqualTo(ProfileType.SELF);
    }
}

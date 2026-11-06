package com.xixin.health.iam;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrapConfig implements ApplicationRunner {

    public static final String DEFAULT_ADMIN_PHONE = "13900000000";
    public static final String DEFAULT_ADMIN_PASSWORD = System.getenv().getOrDefault("XIXIN_ADMIN_PASSWORD", "ChangeMe123!");

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrapConfig(
        UserAccountRepository userAccountRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userAccountRepository.findByPhone(DEFAULT_ADMIN_PHONE).isPresent()) {
            return;
        }
        Role systemAdmin = roleRepository.findByCode("SYSTEM_ADMIN")
            .orElseGet(() -> roleRepository.save(Role.system("SYSTEM_ADMIN", "System Admin", "ADMIN")));
        UserAccount account = new UserAccount(
            DEFAULT_ADMIN_PHONE,
            "admin@xixin.local",
            "System Administrator",
            passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD)
        );
        account.addRole(systemAdmin);
        userAccountRepository.save(account);
    }
}

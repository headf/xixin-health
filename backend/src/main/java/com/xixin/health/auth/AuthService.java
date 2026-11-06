package com.xixin.health.auth;

import com.xixin.health.audit.AuditService;
import com.xixin.health.auth.dto.AuthTokenResponse;
import com.xixin.health.auth.dto.LoginRequest;
import com.xixin.health.auth.dto.RegisterRequest;
import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import com.xixin.health.iam.Role;
import com.xixin.health.iam.RoleRepository;
import com.xixin.health.iam.UserAccount;
import com.xixin.health.iam.UserAccountRepository;
import com.xixin.health.profile.HealthProfile;
import com.xixin.health.profile.HealthProfileRepository;
import com.xixin.health.profile.ProfileType;
import com.xixin.health.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String NORMAL_USER = "NORMAL_USER";

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuditService auditService;
    private final HealthProfileRepository healthProfileRepository;

    public AuthService(
        UserAccountRepository userAccountRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder,
        TokenService tokenService,
        AuditService auditService,
        HealthProfileRepository healthProfileRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.auditService = auditService;
        this.healthProfileRepository = healthProfileRepository;
    }

    @Transactional
    public UserAccount register(RegisterRequest request) {
        if (userAccountRepository.existsByPhone(request.phone())) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "Phone already registered");
        }
        Role normalUser = roleRepository.findByCode(NORMAL_USER)
            .orElseGet(() -> roleRepository.save(Role.system(NORMAL_USER, "普通用户", "USER")));
        UserAccount account = new UserAccount(
            request.phone(),
            request.email(),
            request.displayName(),
            passwordEncoder.encode(request.password())
        );
        account.addRole(normalUser);
        account.setIdCard(request.idCard());
        UserAccount saved = userAccountRepository.save(account);

        // Auto-create health profile for the new user
        HealthProfile profile = new HealthProfile(saved.getId(), ProfileType.SELF);
        profile.update(request.displayName(), request.idCard(), request.phone(), "", "", "", "", ProfileType.SELF);
        healthProfileRepository.save(profile);

        auditService.record(String.valueOf(saved.getId()), "USER_REGISTER", "sys_user", String.valueOf(saved.getId()), "SUCCESS", null, null, null);
        return saved;
    }

    @Transactional
    public AuthTokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        UserAccount account = userAccountRepository.findByPhone(request.loginId())
            .or(() -> userAccountRepository.findByEmail(request.loginId()))
            .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));
        if (!account.isEnabled() || account.isDeleted() || !passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
        TokenService.TokenPair pair = tokenService.issueTokens(account);
        auditService.record(String.valueOf(account.getId()), "LOGIN", "sys_user", String.valueOf(account.getId()), "SUCCESS", ipAddress, userAgent, null);
        return new AuthTokenResponse("Bearer", pair.accessToken(), pair.refreshToken(), pair.expiresInSeconds(), account.getId(), account.getPhone(), account.getDisplayName());
    }

    @Transactional
    public void resetPassword(String loginId, String newPassword) {
        UserAccount account = userAccountRepository.findByPhone(loginId)
            .or(() -> userAccountRepository.findByEmail(loginId))
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Account not found"));
        account.changePassword(passwordEncoder.encode(newPassword));
        userAccountRepository.save(account);
        auditService.record(String.valueOf(account.getId()), "PASSWORD_RESET", "sys_user", String.valueOf(account.getId()), "SUCCESS", null, null, null);
    }

    @Transactional(readOnly = true)
    public AuthTokenResponse refresh(String refreshToken) {
        TokenService.AuthenticatedToken authenticatedToken = tokenService.authenticateRefreshToken(refreshToken)
            .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, "Invalid refresh token"));
        UserAccount account = userAccountRepository.findById(authenticatedToken.userId())
            .filter(user -> user.isEnabled() && !user.isDeleted())
            .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, "Invalid refresh token"));
        TokenService.TokenPair pair = tokenService.issueTokens(account);
        return new AuthTokenResponse("Bearer", pair.accessToken(), pair.refreshToken(), pair.expiresInSeconds(), account.getId(), account.getPhone(), account.getDisplayName());
    }
}

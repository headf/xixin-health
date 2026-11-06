package com.xixin.health.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException(username);
        };
    }

    /**
     * 角色层级：各科室医生(EXAM_DOCTOR_*)继承EXAM_DOCTOR权限
     * 这样 @PreAuthorize("hasRole('EXAM_DOCTOR')") 对所有科室医生生效
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(
            "ROLE_EXAM_DOCTOR_LAB > ROLE_EXAM_DOCTOR\n" +
            "ROLE_EXAM_DOCTOR_IMAGING > ROLE_EXAM_DOCTOR\n" +
            "ROLE_EXAM_DOCTOR_INTERNAL > ROLE_EXAM_DOCTOR\n" +
            "ROLE_EXAM_DOCTOR_SURGERY > ROLE_EXAM_DOCTOR\n" +
            "ROLE_EXAM_DOCTOR_OPHTHALM > ROLE_EXAM_DOCTOR\n" +
            "ROLE_EXAM_DOCTOR_ENT > ROLE_EXAM_DOCTOR\n" +
            "ROLE_EXAM_DOCTOR_DENTAL > ROLE_EXAM_DOCTOR\n" +
            "ROLE_EXAM_DOCTOR_ECG > ROLE_EXAM_DOCTOR\n" +
            "ROLE_EXAM_DOCTOR_ULTRASOUND > ROLE_EXAM_DOCTOR\n" +
            "ROLE_EXAM_DOCTOR_BMD > ROLE_EXAM_DOCTOR\n" +
            "ROLE_EXAM_DOCTOR_BLOOD > ROLE_EXAM_DOCTOR"
        );
        return hierarchy;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                .accessDeniedHandler((request, response, accessDeniedException) -> response.sendError(HttpServletResponse.SC_FORBIDDEN))
            )
            .authorizeHttpRequests(auth -> auth
                // ---------- 完全公开的接口（无需认证） ----------
                .requestMatchers(
                    "/api/v1/auth/register",
                    "/api/v1/auth/login",
                    "/api/v1/auth/refresh",
                    "/api/v1/auth/reset-password",
                    "/api/v1/packages/**",
                    "/api/v1/branches/**",
                    "/api/v1/departments/**",
                    "/api/v1/content/**",
                    "/api/v1/contact",
                    "/api/v1/slots/**",
                    "/api/v1/notifications/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/h2-console/**"
                ).permitAll()
                // ---------- 用户端公开接口（允许匿名访问用户自己的数据）----------
                .requestMatchers(
                    "/api/v1/profiles/**",
                    "/api/v1/appointments/**",
                    "/api/v1/questionnaire/**",
                    "/api/v1/orders/**",
                    "/api/v1/results/public/**",
                    "/api/v1/reports/**",
                    "/api/v1/exams/my-progress/**"
                ).permitAll()
                // ---------- 需要登录认证 ----------
                .requestMatchers("/api/v1/meals/claim/**").authenticated()
                // ---------- 管理员接口 ----------
                .requestMatchers("/api/v1/admin/**").hasAnyRole("SYSTEM_ADMIN", "OPERATIONS_ADMIN", "REVIEW_DOCTOR")
                .requestMatchers("/api/v1/results/critical", "/api/v1/results/pending-review", "/api/v1/results/pending-review/**")
                    .hasAnyRole("REVIEW_DOCTOR", "SYSTEM_ADMIN")
                // ---------- 其余所有接口需认证 ----------
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}

# Health Check Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first runnable backend for 熙心健康体检管理系统 as a Spring Boot modular monolith that follows the approved design and source requirement documents.

**Architecture:** The backend is a single Spring Boot application with package-level business boundaries matching future microservice domains. Controllers expose `/api/v1/**`, services hold business rules, repositories isolate persistence, and adapters isolate simulated external services such as payment and notification. Security is centralized with JWT-style stateless authentication and RBAC method/web authorization.

**Tech Stack:** Java 17 target, Spring Boot 3.3.x, Spring Security, Spring Data JPA for the first scaffold, H2 test profile, MySQL runtime profile, Flyway migrations, JUnit 5, springdoc-openapi, Docker Compose for MySQL and Redis.

---

## Environment Notes

The workspace at `D:\work\课设` is not a Git repository, and this machine has Java 21 but no global Maven or Gradle. Use `mvn test` after Maven is installed or after adding a valid Maven Wrapper. During execution, create tests before production code and treat missing Maven as a verification blocker, not a passing result.

## File Structure

- Create `backend/pom.xml`: Maven build, dependencies, Java 17 release target.
- Create `backend/docker-compose.yml`: local MySQL and Redis services.
- Create `backend/src/main/resources/application.yml`: default H2-backed dev/test-friendly configuration.
- Create `backend/src/main/resources/db/migration/V1__init_schema.sql`: core schema and seed roles.
- Create `backend/src/main/java/com/xixin/health/HealthCheckBackendApplication.java`: Spring Boot entry point.
- Create `backend/src/main/java/com/xixin/health/common/**`: API response, errors, auditing fields, enums.
- Create `backend/src/main/java/com/xixin/health/security/**`: password encoder, token service, current user model, web security.
- Create `backend/src/main/java/com/xixin/health/auth/**`: registration, login, refresh token endpoints.
- Create `backend/src/main/java/com/xixin/health/iam/**`: user, role, permission entities and account/role services.
- Create `backend/src/main/java/com/xixin/health/profile/**`: health profile and family member API.
- Create `backend/src/main/java/com/xixin/health/configuration/**`: branch, department, exam item, package, rule, report template models.
- Create `backend/src/main/java/com/xixin/health/appointment/**`: appointment creation, cancellation, rescheduling.
- Create `backend/src/main/java/com/xixin/health/order/**`: order, simulated payment, refund records.
- Create `backend/src/main/java/com/xixin/health/exam/**`: check-in, exam record, guide sheet, queue, progress.
- Create `backend/src/main/java/com/xixin/health/result/**`: check result entry, abnormal detection, review.
- Create `backend/src/main/java/com/xixin/health/report/**`: report generation records and query API.
- Create `backend/src/main/java/com/xixin/health/analysis/**`: health analysis and trend records.
- Create `backend/src/main/java/com/xixin/health/notification/**`: station-message notification records.
- Create `backend/src/main/java/com/xixin/health/audit/**`: operation log entity and audit service.
- Create `backend/src/test/java/com/xixin/health/**`: behavior tests for auth, appointment, result/report, audit.

## Task 1: Project Scaffold

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/xixin/health/HealthCheckBackendApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/docker-compose.yml`
- Test: `backend/src/test/java/com/xixin/health/HealthCheckBackendApplicationTests.java`

- [ ] **Step 1: Write the failing context-load test**

```java
package com.xixin.health;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HealthCheckBackendApplicationTests {
    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=HealthCheckBackendApplicationTests`

Expected: FAIL because `pom.xml` and `HealthCheckBackendApplication` do not exist yet, or Maven is unavailable in this environment.

- [ ] **Step 3: Add the Maven build**

Use `spring-boot-starter-parent` `3.3.5`, Java release `17`, and dependencies for web, validation, security, data-jpa, flyway, h2, mysql, lombok, springdoc, and test. The artifact id is `xixin-health-backend`.

- [ ] **Step 4: Add the application entry point and configuration**

Create `HealthCheckBackendApplication` in package `com.xixin.health`. Configure H2 for default local startup, expose OpenAPI at `/swagger-ui.html`, and set `server.servlet.context-path` empty so controllers own `/api/v1`.

- [ ] **Step 5: Run test to verify it passes**

Run: `cd backend && mvn test -Dtest=HealthCheckBackendApplicationTests`

Expected: PASS when Maven dependencies are available.

## Task 2: Common API, Error, and Audit Foundation

**Files:**
- Create: `backend/src/main/java/com/xixin/health/common/api/ApiResponse.java`
- Create: `backend/src/main/java/com/xixin/health/common/error/BusinessException.java`
- Create: `backend/src/main/java/com/xixin/health/common/error/ErrorCode.java`
- Create: `backend/src/main/java/com/xixin/health/common/error/GlobalExceptionHandler.java`
- Create: `backend/src/main/java/com/xixin/health/common/model/BaseEntity.java`
- Create: `backend/src/main/java/com/xixin/health/audit/OperationLog.java`
- Create: `backend/src/main/java/com/xixin/health/audit/OperationLogRepository.java`
- Create: `backend/src/main/java/com/xixin/health/audit/AuditService.java`
- Test: `backend/src/test/java/com/xixin/health/common/ApiResponseTest.java`
- Test: `backend/src/test/java/com/xixin/health/audit/AuditServiceTest.java`

- [ ] **Step 1: Write failing API response test**

```java
package com.xixin.health.common;

import com.xixin.health.common.api.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {
    @Test
    void successWrapsDataWithCodeZero() {
        ApiResponse<String> response = ApiResponse.success("ok");
        assertThat(response.code()).isEqualTo("0");
        assertThat(response.message()).isEqualTo("success");
        assertThat(response.data()).isEqualTo("ok");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=ApiResponseTest`

Expected: FAIL because `ApiResponse` is missing.

- [ ] **Step 3: Implement common response and exception classes**

Create immutable records/classes with explicit error codes: `AUTH_INVALID_CREDENTIALS`, `AUTH_UNAUTHORIZED`, `AUTH_FORBIDDEN`, `RESOURCE_NOT_FOUND`, `BUSINESS_CONFLICT`, `VALIDATION_ERROR`, `SYSTEM_ERROR`.

- [ ] **Step 4: Write failing audit service test**

```java
package com.xixin.health.audit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(AuditService.class)
class AuditServiceTest {
    @Autowired AuditService auditService;
    @Autowired OperationLogRepository operationLogRepository;

    @Test
    void recordsOperationLogWithTraceId() {
        auditService.record("u-1", "LOGIN", "sys_user", "u-1", "SUCCESS", "127.0.0.1", "JUnit", "trace-1");
        assertThat(operationLogRepository.findAll()).hasSize(1);
        OperationLog log = operationLogRepository.findAll().get(0);
        assertThat(log.getOperationType()).isEqualTo("LOGIN");
        assertThat(log.getTraceId()).isEqualTo("trace-1");
    }
}
```

- [ ] **Step 5: Implement audit entity, repository, and service**

Use `OperationLog` fields matching the SRS: operation time, operator id, operation type, target type, target id, result, IP, User-Agent, TraceID.

- [ ] **Step 6: Run tests to verify they pass**

Run: `cd backend && mvn test -Dtest=ApiResponseTest,AuditServiceTest`

Expected: PASS when Maven dependencies are available.

## Task 3: Authentication, JWT-Style Token, and RBAC Seed Model

**Files:**
- Create: `backend/src/main/java/com/xixin/health/iam/UserAccount.java`
- Create: `backend/src/main/java/com/xixin/health/iam/Role.java`
- Create: `backend/src/main/java/com/xixin/health/iam/Permission.java`
- Create: `backend/src/main/java/com/xixin/health/iam/UserAccountRepository.java`
- Create: `backend/src/main/java/com/xixin/health/iam/RoleRepository.java`
- Create: `backend/src/main/java/com/xixin/health/security/SecurityConfig.java`
- Create: `backend/src/main/java/com/xixin/health/security/TokenService.java`
- Create: `backend/src/main/java/com/xixin/health/auth/AuthController.java`
- Create: `backend/src/main/java/com/xixin/health/auth/AuthService.java`
- Create: `backend/src/main/java/com/xixin/health/auth/dto/RegisterRequest.java`
- Create: `backend/src/main/java/com/xixin/health/auth/dto/LoginRequest.java`
- Create: `backend/src/main/java/com/xixin/health/auth/dto/AuthTokenResponse.java`
- Test: `backend/src/test/java/com/xixin/health/auth/AuthServiceTest.java`

- [ ] **Step 1: Write failing auth service test**

```java
package com.xixin.health.auth;

import com.xixin.health.auth.dto.AuthTokenResponse;
import com.xixin.health.auth.dto.LoginRequest;
import com.xixin.health.auth.dto.RegisterRequest;
import com.xixin.health.iam.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AuthServiceTest {
    @Autowired AuthService authService;
    @Autowired UserAccountRepository userAccountRepository;

    @Test
    void registersDefaultNormalUserAndLogsIn() {
        authService.register(new RegisterRequest("13800000001", "alice@example.com", "Alice", "Passw0rd!"));
        assertThat(userAccountRepository.findByPhone("13800000001")).isPresent();

        AuthTokenResponse token = authService.login(new LoginRequest("13800000001", "Passw0rd!"), "127.0.0.1", "JUnit");

        assertThat(token.accessToken()).isNotBlank();
        assertThat(token.refreshToken()).isNotBlank();
        assertThat(token.tokenType()).isEqualTo("Bearer");
        assertThat(token.expiresInSeconds()).isEqualTo(7200);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=AuthServiceTest`

Expected: FAIL because auth and IAM classes are missing.

- [ ] **Step 3: Implement IAM entities and repositories**

Model `UserAccount` with phone, email, display name, password hash, enabled flag, deleted flag, and many-to-many roles. Seed roles include `NORMAL_USER`, `EXAM_DOCTOR`, `REVIEW_DOCTOR`, `FRONT_DESK`, `OPERATIONS_ADMIN`, `SYSTEM_ADMIN`.

- [ ] **Step 4: Implement password hashing and token service**

Use BCrypt for passwords. Use a signed HMAC token service for access and refresh tokens with explicit `expiresInSeconds = 7200` for access tokens and seven-day refresh token metadata.

- [ ] **Step 5: Implement auth service and controller**

Expose `/api/v1/auth/register`, `/api/v1/auth/login`, `/api/v1/auth/refresh`, `/api/v1/auth/logout`, and `/api/v1/auth/password`.

- [ ] **Step 6: Run auth test to verify it passes**

Run: `cd backend && mvn test -Dtest=AuthServiceTest`

Expected: PASS when Maven dependencies are available.

## Task 4: Database Migration and Seed Data

**Files:**
- Create: `backend/src/main/resources/db/migration/V1__init_schema.sql`
- Test: `backend/src/test/java/com/xixin/health/db/FlywayMigrationTest.java`

- [ ] **Step 1: Write failing migration test**

```java
package com.xixin.health.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FlywayMigrationTest {
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void seedsRequiredRoles() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from sys_role", Integer.class);
        assertThat(count).isGreaterThanOrEqualTo(6);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=FlywayMigrationTest`

Expected: FAIL because migration is missing.

- [ ] **Step 3: Add migration**

Create tables listed in the design doc: `sys_user`, `sys_role`, `sys_permission`, `sys_user_role`, `sys_role_permission`, `t_health_profile`, `t_family_member`, `t_branch`, `t_department`, `t_exam_item`, `t_exam_package`, `t_exam_package_item`, `t_business_rule`, `t_report_template`, `t_appointment`, `t_order`, `t_payment_record`, `t_refund_record`, `t_exam_record`, `t_guide_sheet`, `t_department_queue`, `t_exam_progress`, `t_check_result`, `t_result_review`, `t_exam_report`, `t_report_history_link`, `t_health_analysis`, `t_health_management`, `t_notification`, `t_operation_log`.

- [ ] **Step 4: Seed basic data**

Insert required roles, core permissions, one default branch, two departments, three exam items, one starter package, and one default report template.

- [ ] **Step 5: Run migration test to verify it passes**

Run: `cd backend && mvn test -Dtest=FlywayMigrationTest`

Expected: PASS when Maven dependencies are available.

## Task 5: Profiles, Configuration, and Package Browsing

**Files:**
- Create: `backend/src/main/java/com/xixin/health/profile/ProfileController.java`
- Create: `backend/src/main/java/com/xixin/health/profile/ProfileService.java`
- Create: `backend/src/main/java/com/xixin/health/profile/HealthProfile.java`
- Create: `backend/src/main/java/com/xixin/health/profile/FamilyMember.java`
- Create: `backend/src/main/java/com/xixin/health/profile/HealthProfileRepository.java`
- Create: `backend/src/main/java/com/xixin/health/configuration/ExamPackage.java`
- Create: `backend/src/main/java/com/xixin/health/configuration/Branch.java`
- Create: `backend/src/main/java/com/xixin/health/configuration/Department.java`
- Create: `backend/src/main/java/com/xixin/health/configuration/ExamItem.java`
- Create: `backend/src/main/java/com/xixin/health/configuration/PackageCatalogController.java`
- Test: `backend/src/test/java/com/xixin/health/profile/ProfileServiceTest.java`

- [ ] **Step 1: Write failing profile test**

```java
package com.xixin.health.profile;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProfileServiceTest {
    @Autowired ProfileService profileService;

    @Test
    void createsPersonalProfileForUser() {
        HealthProfile profile = profileService.createOrUpdatePersonalProfile(
            1L, "张三", "110101199001010011", "13800000001", "青霉素", "无", "李四", "13900000001"
        );

        assertThat(profile.getOwnerUserId()).isEqualTo(1L);
        assertThat(profile.getName()).isEqualTo("张三");
        assertThat(profile.getProfileType()).isEqualTo(ProfileType.SELF);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=ProfileServiceTest`

Expected: FAIL because profile classes are missing.

- [ ] **Step 3: Implement profiles and catalog browsing**

Implement profile CRUD for self and family members. Implement read-only package and branch catalog endpoints backed by seeded data.

- [ ] **Step 4: Run profile test to verify it passes**

Run: `cd backend && mvn test -Dtest=ProfileServiceTest`

Expected: PASS when Maven dependencies are available.

## Task 6: Appointment and Order Flow

**Files:**
- Create: `backend/src/main/java/com/xixin/health/appointment/Appointment.java`
- Create: `backend/src/main/java/com/xixin/health/appointment/AppointmentStatus.java`
- Create: `backend/src/main/java/com/xixin/health/appointment/AppointmentService.java`
- Create: `backend/src/main/java/com/xixin/health/appointment/AppointmentController.java`
- Create: `backend/src/main/java/com/xixin/health/order/Order.java`
- Create: `backend/src/main/java/com/xixin/health/order/OrderStatus.java`
- Create: `backend/src/main/java/com/xixin/health/order/PaymentRecord.java`
- Create: `backend/src/main/java/com/xixin/health/order/OrderService.java`
- Create: `backend/src/main/java/com/xixin/health/order/OrderController.java`
- Test: `backend/src/test/java/com/xixin/health/appointment/AppointmentOrderFlowTest.java`

- [ ] **Step 1: Write failing appointment-order test**

```java
package com.xixin.health.appointment;

import com.xixin.health.order.Order;
import com.xixin.health.order.OrderService;
import com.xixin.health.order.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AppointmentOrderFlowTest {
    @Autowired AppointmentService appointmentService;
    @Autowired OrderService orderService;

    @Test
    void createsAppointmentAndMarksPaidAfterSimulatedPayment() {
        Appointment appointment = appointmentService.createAppointment(1L, 1L, 1L, 1L, LocalDate.now().plusDays(2), "AM");
        Order order = orderService.createOrderForAppointment(appointment.getId(), 29900);

        orderService.simulatePaymentSuccess(order.getOrderNo(), "PAY-001");

        Order paid = orderService.getByOrderNo(order.getOrderNo());
        Appointment reloaded = appointmentService.getById(appointment.getId());
        assertThat(paid.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(reloaded.getStatus()).isEqualTo(AppointmentStatus.PAID);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=AppointmentOrderFlowTest`

Expected: FAIL because appointment and order classes are missing.

- [ ] **Step 3: Implement appointment and order services**

Use appointment statuses `CREATED`, `PAID`, `CHECKED_IN`, `CANCELLED`, `COMPLETED`. Use order statuses `PENDING_PAYMENT`, `PAID`, `REFUND_REQUESTED`, `REFUNDED`, `CLOSED`. Enforce cancellation and reschedule only when status is `CREATED` or `PAID` and date is in the future.

- [ ] **Step 4: Run flow test to verify it passes**

Run: `cd backend && mvn test -Dtest=AppointmentOrderFlowTest`

Expected: PASS when Maven dependencies are available.

## Task 7: Exam Process, Result Review, Report, and Analysis

**Files:**
- Create: `backend/src/main/java/com/xixin/health/exam/ExamRecord.java`
- Create: `backend/src/main/java/com/xixin/health/exam/ExamService.java`
- Create: `backend/src/main/java/com/xixin/health/exam/ExamController.java`
- Create: `backend/src/main/java/com/xixin/health/result/CheckResult.java`
- Create: `backend/src/main/java/com/xixin/health/result/ResultReview.java`
- Create: `backend/src/main/java/com/xixin/health/result/ResultService.java`
- Create: `backend/src/main/java/com/xixin/health/result/ResultController.java`
- Create: `backend/src/main/java/com/xixin/health/report/ExamReport.java`
- Create: `backend/src/main/java/com/xixin/health/report/ReportService.java`
- Create: `backend/src/main/java/com/xixin/health/report/ReportController.java`
- Create: `backend/src/main/java/com/xixin/health/analysis/HealthAnalysis.java`
- Test: `backend/src/test/java/com/xixin/health/result/ResultReportFlowTest.java`

- [ ] **Step 1: Write failing result-report test**

```java
package com.xixin.health.result;

import com.xixin.health.report.ExamReport;
import com.xixin.health.report.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ResultReportFlowTest {
    @Autowired ResultService resultService;
    @Autowired ReportService reportService;

    @Test
    void marksAbnormalResultAndGeneratesReportAfterReview() {
        CheckResult result = resultService.recordNumericResult(1L, 1L, "血糖", "mmol/L", 8.2, 3.9, 6.1, 11.1, 2L);
        assertThat(result.getAbnormalLevel()).isEqualTo(AbnormalLevel.ABNORMAL);

        resultService.reviewResult(result.getId(), 3L, true, "确认异常");
        ExamReport report = reportService.generateReport(1L, 3L);

        assertThat(report.getReportStatus()).isEqualTo(ReportStatus.CONFIRMED);
        assertThat(report.getAbnormalStatus()).isEqualTo(AbnormalLevel.ABNORMAL);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=ResultReportFlowTest`

Expected: FAIL because exam, result, and report classes are missing.

- [ ] **Step 3: Implement exam check-in and result/review/report flow**

Implement check-in from paid appointment to exam record. Implement numeric result comparison: above reference range is `ABNORMAL`, above critical threshold is `CRITICAL`, otherwise `NORMAL`. Report generation requires all results for an exam record to be reviewed.

- [ ] **Step 4: Run result-report test to verify it passes**

Run: `cd backend && mvn test -Dtest=ResultReportFlowTest`

Expected: PASS when Maven dependencies are available.

## Task 8: Admin, Notifications, OpenAPI, and Final Verification

**Files:**
- Create: `backend/src/main/java/com/xixin/health/iam/AdminAccountController.java`
- Create: `backend/src/main/java/com/xixin/health/iam/RoleController.java`
- Create: `backend/src/main/java/com/xixin/health/notification/Notification.java`
- Create: `backend/src/main/java/com/xixin/health/notification/NotificationService.java`
- Create: `backend/src/main/java/com/xixin/health/notification/NotificationController.java`
- Create: `backend/README.md`
- Test: `backend/src/test/java/com/xixin/health/security/SecurityAccessTest.java`

- [ ] **Step 1: Write failing security access test**

```java
package com.xixin.health.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAccessTest {
    @Autowired MockMvc mockMvc;

    @Test
    void adminEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/audit-logs"))
            .andExpect(status().isUnauthorized());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=SecurityAccessTest`

Expected: FAIL because admin/security endpoints are incomplete.

- [ ] **Step 3: Implement admin and notification endpoints**

Expose role/account read APIs, audit log query API, notification list API, and mark-read API. Protect `/api/v1/admin/**` with system-admin or operations-admin authority.

- [ ] **Step 4: Add README**

Document startup, Maven requirement, Docker Compose, OpenAPI URL, default seeded roles, and the limitation that payment/notification/HIS/LIS/PACS/AI are simulated adapters in the first backend.

- [ ] **Step 5: Run final verification**

Run: `cd backend && mvn test`

Expected: PASS when Maven dependencies are available.

Run: `cd backend && docker compose up -d mysql redis`

Expected: MySQL and Redis containers start if Docker is available.

Run: `cd backend && mvn spring-boot:run`

Expected: Application starts and OpenAPI is available at `http://localhost:8080/swagger-ui.html`.

## Self-Review Checklist

- Spec coverage: Tasks cover scaffold, common errors, audit, auth, RBAC, profile, configuration, appointment, order, exam process, result review, report, analysis, notifications, admin, Docker, and OpenAPI.
- Scope containment: First backend uses simulated adapters for third-party payment, SMS, HIS/LIS/PACS, object storage, and AI while preserving replaceable interfaces.
- Verification caveat: `mvn test` cannot run until Maven or a valid Maven Wrapper is available in the environment.

# Admin Spec Compliance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bring the management-side implementation into strict alignment with `文档/第04组-需求规约-熙心健康体检管理系统_修改版.docx` and `文档/08.第04组-项目功能结构表-熙心健康管理系统.xlsx`.

**Architecture:** Keep the existing Spring Boot modular monolith, but split management capabilities by boundary: IAM/security, base configuration, medical workflow, result/report workflow, health intervention, audit, statistics, and admin frontend. Enforce requirements with contract tests first, then implement only the minimum production code needed for each requirement.

**Tech Stack:** Spring Boot 3.3, Spring Security JWT/RBAC, Spring MVC, Spring Data JPA, Flyway, H2/MySQL-compatible SQL, JUnit 5, MockMvc, React, Ant Design Pro.

---

## Requirement Sources

- `文档/第04组-需求规约-熙心健康体检管理系统_修改版.docx`
- `文档/08.第04组-项目功能结构表-熙心健康管理系统.xlsx`

## File Structure

- `backend/src/main/java/com/xixin/health/security/`
  JWT authentication, role/permission authority mapping, request authorization.
- `backend/src/main/java/com/xixin/health/iam/`
  Role, account, permission, data-scope management and safe admin DTOs.
- `backend/src/main/java/com/xixin/health/configuration/`
  Exam items, packages, branches, departments, rules, report templates, flow nodes, and medical config review lifecycle.
- `backend/src/main/java/com/xixin/health/exam/`
  Front-desk check-in, guide sheet, department queue, and exam progress tracking.
- `backend/src/main/java/com/xixin/health/result/`
  Result entry, result query, single review, batch review, and review records.
- `backend/src/main/java/com/xixin/health/report/`
  Report generation, template binding, abnormal warning, report query, publish, preview, and export metadata.
- `backend/src/main/java/com/xixin/health/management/`
  High-risk user identification, follow-up plans, and intervention plans.
- `backend/src/main/java/com/xixin/health/audit/`
  Append-only audit record creation, query, and hash-chain integrity.
- `backend/src/main/java/com/xixin/health/statistics/`
  Business statistics, health statistics, desensitized aggregate exports.
- `backend/src/test/java/com/xixin/health/admin/`
  Management-side contract and compliance tests.
- `admin/`
  React + Ant Design Pro management frontend.
- `docs/compliance/admin-requirements-traceability.md`
  Human-readable traceability matrix from requirements to code/tests.

---

### Task 1: Establish Admin Compliance Traceability

**Files:**
- Create: `docs/compliance/admin-requirements-traceability.md`
- Create: `backend/src/test/java/com/xixin/health/admin/AdminComplianceSmokeTest.java`

- [ ] **Step 1: Create the traceability matrix**

Write `docs/compliance/admin-requirements-traceability.md` with this table:

```markdown
# 管理端需求追踪矩阵

| 需求项 | 来源 | 必选性 | 后端实现 | 前端实现 | 测试 |
| --- | --- | --- | --- | --- | --- |
| 基础配置-体检项目管理 | 功能结构表 R54 / 规约 3.2.11 | 必选 | `AdminConfigurationController` | `admin/src/pages/config/ExamItemPage.tsx` | `AdminConfigurationComplianceTest.examItemRequiresConfigPermissionAndAudit()` |
| 基础配置-套餐配置 | 功能结构表 R55 / 规约 3.2.11 | 必选 | `AdminConfigurationController` | `admin/src/pages/config/PackagePage.tsx` | `AdminConfigurationComplianceTest.packageDisableDoesNotPhysicalDelete()` |
| 基础配置-规则配置审核 | 功能结构表 R56 / 规约 3.2.11 | 必选 | `ConfigReviewController` | `admin/src/pages/config/RulePage.tsx` | `AdminConfigurationComplianceTest.medicalRuleChangeRequiresReviewBeforeEffective()` |
| 基础配置-分院管理 | 功能结构表 R57 / 规约 3.2.11 | 必选 | `AdminConfigurationController` | `admin/src/pages/config/BranchPage.tsx` | `AdminConfigurationComplianceTest.branchCrudRequiresConfigPermission()` |
| 基础配置-科室管理 | 功能结构表 R58 / 规约附录 | 必选 | `AdminDepartmentController` | `admin/src/pages/config/DepartmentPage.tsx` | `AdminConfigurationComplianceTest.departmentCrudRequiresDepartmentPermission()` |
| 权限-角色管理 | 功能结构表 R60 / 规约 3.2.12 | 必选 | `RoleController` | `admin/src/pages/iam/RolePage.tsx` | `AdminIamComplianceTest.builtInRoleCannotBeModifiedOrDeleted()` |
| 权限-账号管理 | 功能结构表 R61 / 规约 3.2.12 | 必选 | `AdminAccountController` | `admin/src/pages/iam/AccountPage.tsx` | `AdminIamComplianceTest.accountResponseDoesNotExposePasswordHash()` |
| 审计-操作日志查询 | 功能结构表 R62 / 规约 3.3.4 | 必选 | `AuditLogController` | `admin/src/pages/audit/AuditLogPage.tsx` | `AuditComplianceTest.auditHashChainDetectsTampering()` |
| 医务端-前台签到登记 | 功能结构表 R37 / 规约 3.2.7 | 必选 | `ExamWorkflowController` | `admin/src/pages/exam/CheckInPage.tsx` | `MedicalWorkflowComplianceTest.checkInCreatesExamRecordGuideSheetAndProgress()` |
| 医务端-指引单管理 | 功能结构表 R38 / 规约 3.2.7 | 必选 | `ExamWorkflowController` | `admin/src/pages/exam/GuideSheetPage.tsx` | `MedicalWorkflowComplianceTest.guideSheetContainsOrderedDepartments()` |
| 医务端-科室队列管理 | 功能结构表 R39 / 规约 3.2.7 | 必选 | `DepartmentQueueController` | `admin/src/pages/exam/DepartmentQueuePage.tsx` | `MedicalWorkflowComplianceTest.departmentQueueSupportsCallSkipAndComplete()` |
| 医务端-体检进度跟踪 | 功能结构表 R40 / 规约 3.2.7 | 必选 | `ExamProgressController` | `admin/src/pages/exam/ProgressPage.tsx` | `MedicalWorkflowComplianceTest.progressBlocksReportUntilRequiredItemsCompleted()` |
| 结果-结果录入 | 功能结构表 R43 / 规约 3.2.8 | 必选 | `ResultController` | `admin/src/pages/result/ResultEntryPage.tsx` | `ResultReviewComplianceTest.recordNumericResultDetectsAbnormalLevel()` |
| 结果-结果审核 | 功能结构表 R44 / 规约 3.2.8 | 必选 | `ResultReviewController` | `admin/src/pages/result/ReviewPage.tsx` | `ResultReviewComplianceTest.reviewEndpointApprovesAndRejectsResult()` |
| 结果-批量审核 | 功能结构表 R45 / 规约 3.2.8 | 可选但当前保留 | `ResultReviewController` | `admin/src/pages/result/BatchReviewPage.tsx` | `ResultReviewComplianceTest.batchReviewOnlyApprovesNormalPendingResults()` |
| 报告-模板配置 | 功能结构表 R47 / 规约 3.2.9 | 必选 | `AdminReportTemplateController` | `admin/src/pages/report/TemplatePage.tsx` | `ReportComplianceTest.templatePreviewUsesConfiguredContent()` |
| 报告-异常报告预警 | 功能结构表 R48 / 规约 3.2.9 | 必选 | `AdminReportController` | `admin/src/pages/report/ReportWarningPage.tsx` | `ReportComplianceTest.criticalReportIsMarkedAndRequiresDoctorPublish()` |
| 健康干预-高危人群管理 | 功能结构表 R50 / 规约 3.2.10 | 当前保留 | `HealthManagementController` | `admin/src/pages/management/RiskUserPage.tsx` | `HealthManagementComplianceTest.criticalReportCreatesHighRiskManagementRecord()` |
| 健康干预-随访与干预方案 | 功能结构表 R51 / 规约 3.2.10 | 当前保留 | `HealthManagementController` | `admin/src/pages/management/InterventionPage.tsx` | `HealthManagementComplianceTest.followUpPlanCreatesNotification()` |
| 统计-业务数据统计 | 功能结构表 R64 / 规约 3.2.13 | 当前保留 | `StatisticsController` | `admin/src/pages/statistics/BusinessStatisticsPage.tsx` | `StatisticsComplianceTest.businessStatisticsAreAggregatedAndAudited()` |
| 统计-健康数据统计 | 功能结构表 R65 / 规约 3.2.13 | 当前保留 | `StatisticsController` | `admin/src/pages/statistics/HealthStatisticsPage.tsx` | `StatisticsComplianceTest.healthStatisticsAreDesensitized()` |
| 统计-报表导出 | 功能结构表 R66 / 规约 3.2.13 | 当前保留 | `StatisticsController` | `admin/src/pages/statistics/ExportPage.tsx` | `StatisticsComplianceTest.exportSupportsBusinessAndHealthExcelPdf()` |
```

- [ ] **Step 2: Add a smoke test that protects the compliance file**

Create `backend/src/test/java/com/xixin/health/admin/AdminComplianceSmokeTest.java`:

```java
package com.xixin.health.admin;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AdminComplianceSmokeTest {

    @Test
    void traceabilityMatrixListsRequiredManagementModules() throws Exception {
        String matrix = Files.readString(Path.of("../docs/compliance/admin-requirements-traceability.md"));

        assertThat(matrix)
            .contains("基础配置-体检项目管理")
            .contains("权限-角色管理")
            .contains("医务端-前台签到登记")
            .contains("结果-批量审核")
            .contains("报告-异常报告预警")
            .contains("健康干预-高危人群管理")
            .contains("统计-报表导出");
    }
}
```

- [ ] **Step 3: Run the smoke test**

Run:

```powershell
mvn -q -Dtest=AdminComplianceSmokeTest test
```

Expected: PASS after the file and test are created.

- [ ] **Step 4: Commit**

```powershell
git add docs/compliance/admin-requirements-traceability.md backend/src/test/java/com/xixin/health/admin/AdminComplianceSmokeTest.java
git commit -m "docs: add admin compliance traceability matrix"
```

---

### Task 2: Enforce Fine-Grained RBAC And Hide Password Hashes

**Files:**
- Modify: `backend/src/main/java/com/xixin/health/security/JwtAuthenticationFilter.java`
- Modify: `backend/src/main/java/com/xixin/health/security/SecurityConfig.java`
- Modify: `backend/src/main/java/com/xixin/health/iam/AdminAccountController.java`
- Modify: `backend/src/main/java/com/xixin/health/iam/RoleController.java`
- Modify: `backend/src/main/java/com/xixin/health/iam/UserAccount.java`
- Create: `backend/src/main/java/com/xixin/health/iam/dto/AdminAccountResponse.java`
- Create: `backend/src/main/java/com/xixin/health/iam/dto/RoleResponse.java`
- Create: `backend/src/test/java/com/xixin/health/admin/AdminIamComplianceTest.java`

- [ ] **Step 1: Write failing security and redaction tests**

Create `backend/src/test/java/com/xixin/health/admin/AdminIamComplianceTest.java`:

```java
package com.xixin.health.admin;

import com.xixin.health.iam.Role;
import com.xixin.health.iam.RoleRepository;
import com.xixin.health.iam.UserAccount;
import com.xixin.health.iam.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminIamComplianceTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserAccountRepository userAccountRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void accountResponseDoesNotExposePasswordHash() throws Exception {
        mockMvc.perform(get("/api/v1/admin/accounts").with(user("admin").roles("SYSTEM_ADMIN").authorities(() -> "IAM_MANAGE")))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("passwordHash"))));
    }

    @Test
    void operationsAdminCannotManageIamWithoutIamPermission() throws Exception {
        mockMvc.perform(get("/api/v1/admin/accounts").with(user("ops").roles("OPERATIONS_ADMIN").authorities(() -> "CONFIG_MANAGE")))
            .andExpect(status().isForbidden());
    }

    @Test
    void systemAdminWithIamPermissionCanCreateRole() throws Exception {
        mockMvc.perform(post("/api/v1/admin/roles")
                .with(user("admin").roles("SYSTEM_ADMIN").authorities(() -> "IAM_MANAGE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"code":"NURSE_GUIDE","name":"导诊护士","roleType":"MEDICAL","permissionCodes":["FRONT_DESK_CHECKIN"]}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.code").value("NURSE_GUIDE"));
    }

    @Test
    void builtInRoleCannotBeModifiedOrDeleted() throws Exception {
        long systemRoleId = roleRepository.findByCode("SYSTEM_ADMIN").orElseThrow().getId();

        mockMvc.perform(put("/api/v1/admin/roles/{id}", systemRoleId)
                .with(user("admin").roles("SYSTEM_ADMIN").authorities(() -> "IAM_MANAGE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"code":"SYSTEM_ADMIN","name":"Changed","roleType":"ADMIN","permissionCodes":["AUTH_LOGIN"]}
                    """))
            .andExpect(status().isForbidden());
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:

```powershell
mvn -q -Dtest=AdminIamComplianceTest test
```

Expected: FAIL because current `/api/v1/admin/**` authorization is role-only and `UserAccount` responses include `passwordHash`.

- [ ] **Step 3: Attach permission authorities from the database**

Modify `JwtAuthenticationFilter` so it loads the user account by token subject and grants both roles and permissions:

```java
var authorities = account.getRoles().stream()
    .flatMap(role -> java.util.stream.Stream.concat(
        java.util.stream.Stream.of("ROLE_" + role.getCode()),
        role.getPermissions().stream().map(permission -> permission.getCode())
    ))
    .distinct()
    .map(SimpleGrantedAuthority::new)
    .toList();
```

Reject deleted or disabled accounts before creating the authentication.

- [ ] **Step 4: Replace broad admin role access with authenticated access plus method permissions**

Modify `SecurityConfig`:

```java
.requestMatchers("/api/v1/admin/**", "/api/v1/system/**", "/api/v1/statistics/**").authenticated()
```

Add controller annotations:

```java
@PreAuthorize("hasAuthority('IAM_MANAGE')")
```

for role/account endpoints, and matching authorities for config, department, template, flow, audit, and statistics endpoints.

- [ ] **Step 5: Return DTOs from IAM controllers**

Create `AdminAccountResponse`:

```java
package com.xixin.health.iam.dto;

import com.xixin.health.iam.UserAccount;

import java.util.List;

public record AdminAccountResponse(
    Long id,
    String phone,
    String email,
    String displayName,
    boolean enabled,
    List<String> roleCodes
) {
    public static AdminAccountResponse from(UserAccount account) {
        return new AdminAccountResponse(
            account.getId(),
            account.getPhone(),
            account.getEmail(),
            account.getDisplayName(),
            account.isEnabled(),
            account.getRoles().stream().map(role -> role.getCode()).sorted().toList()
        );
    }
}
```

Use the DTO in `AdminAccountController` list/create/update/status/password responses.

- [ ] **Step 6: Run the IAM tests**

Run:

```powershell
mvn -q -Dtest=AdminIamComplianceTest,SecurityAccessTest test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```powershell
git add backend/src/main/java/com/xixin/health/security backend/src/main/java/com/xixin/health/iam backend/src/test/java/com/xixin/health/admin/AdminIamComplianceTest.java
git commit -m "fix: enforce fine grained admin permissions"
```

---

### Task 3: Implement Medical Workflow Management

**Files:**
- Modify: `backend/src/main/java/com/xixin/health/exam/ExamController.java`
- Modify: `backend/src/main/java/com/xixin/health/exam/ExamService.java`
- Create: `backend/src/main/java/com/xixin/health/exam/GuideSheet.java`
- Create: `backend/src/main/java/com/xixin/health/exam/GuideSheetRepository.java`
- Create: `backend/src/main/java/com/xixin/health/exam/DepartmentQueue.java`
- Create: `backend/src/main/java/com/xixin/health/exam/DepartmentQueueRepository.java`
- Create: `backend/src/main/java/com/xixin/health/exam/ExamProgress.java`
- Create: `backend/src/main/java/com/xixin/health/exam/ExamProgressRepository.java`
- Create: `backend/src/test/java/com/xixin/health/admin/MedicalWorkflowComplianceTest.java`

- [ ] **Step 1: Write failing workflow tests**

Create `MedicalWorkflowComplianceTest` with:

```java
@Test
void checkInCreatesExamRecordGuideSheetAndProgress() throws Exception {
    mockMvc.perform(post("/api/v1/admin/exams/check-in")
            .with(user("front").roles("FRONT_DESK").authorities(() -> "FRONT_DESK_CHECKIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"appointmentId":1,"userId":1,"profileId":1,"packageId":1,"branchId":1}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.examNo").exists())
        .andExpect(jsonPath("$.data.guideNo").exists())
        .andExpect(jsonPath("$.data.progressItems.length()").value(3));
}

@Test
void departmentQueueSupportsCallSkipAndComplete() throws Exception {
    long queueId = 1L;

    mockMvc.perform(patch("/api/v1/admin/department-queues/{id}/status", queueId)
            .with(user("doctor").roles("EXAM_DOCTOR").authorities(() -> "RESULT_RECORD"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"queueStatus\":\"IN_PROGRESS\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.queueStatus").value("IN_PROGRESS"));
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:

```powershell
mvn -q -Dtest=MedicalWorkflowComplianceTest test
```

Expected: FAIL with missing endpoints/entities.

- [ ] **Step 3: Add workflow entities for existing tables**

Map `t_guide_sheet`, `t_department_queue`, and `t_exam_progress` with JPA entities. Use enum values:

```java
public enum DepartmentQueueStatus {
    WAITING, CALLED, SKIPPED, IN_PROGRESS, COMPLETED
}

public enum ExamProgressStatus {
    PENDING, IN_PROGRESS, COMPLETED
}
```

- [ ] **Step 4: Implement check-in**

`ExamService.checkIn(...)` must:

1. Create `ExamRecord` with a unique `examNo`.
2. Read package items from `ExamPackageItemRepository`.
3. Create `ExamProgress` rows for each package item.
4. Create one `DepartmentQueue` row per department.
5. Create one `GuideSheet` containing ordered department route text.
6. Record audit type `EXAM_CHECKIN`.

- [ ] **Step 5: Add queue and progress endpoints**

Add endpoints:

```java
@PostMapping({"/api/v1/admin/exams/check-in", "/api/v1/medical/exams/check-in"})
@PreAuthorize("hasAuthority('FRONT_DESK_CHECKIN')")

@GetMapping({"/api/v1/admin/exams/{examRecordId}/progress", "/api/v1/medical/exams/{examRecordId}/progress"})
@PreAuthorize("hasAnyAuthority('FRONT_DESK_CHECKIN','RESULT_RECORD','REPORT_REVIEW')")

@GetMapping({"/api/v1/admin/departments/{departmentId}/queue", "/api/v1/medical/departments/{departmentId}/queue"})
@PreAuthorize("hasAnyAuthority('FRONT_DESK_CHECKIN','RESULT_RECORD')")

@PatchMapping({"/api/v1/admin/department-queues/{id}/status", "/api/v1/medical/department-queues/{id}/status"})
@PreAuthorize("hasAnyAuthority('FRONT_DESK_CHECKIN','RESULT_RECORD')")
```

- [ ] **Step 6: Run workflow tests**

Run:

```powershell
mvn -q -Dtest=MedicalWorkflowComplianceTest test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```powershell
git add backend/src/main/java/com/xixin/health/exam backend/src/test/java/com/xixin/health/admin/MedicalWorkflowComplianceTest.java
git commit -m "feat: add medical workflow management"
```

---

### Task 4: Expose Result Review And Batch Review APIs

**Files:**
- Modify: `backend/src/main/java/com/xixin/health/result/ResultController.java`
- Modify: `backend/src/main/java/com/xixin/health/result/ResultService.java`
- Create: `backend/src/main/java/com/xixin/health/result/ResultReviewController.java`
- Create: `backend/src/test/java/com/xixin/health/admin/ResultReviewComplianceTest.java`

- [ ] **Step 1: Write failing result review tests**

Create tests:

```java
@Test
void reviewEndpointApprovesAndRejectsResult() throws Exception {
    CheckResult result = resultService.recordNumericResult(1L, 1L, "Blood Glucose", "mmol/L", 8.2, 3.9, 6.1, 11.1, 2L);

    mockMvc.perform(post("/api/v1/admin/results/{id}/review", result.getId())
            .with(user("reviewer").roles("REVIEW_DOCTOR").authorities(() -> "REPORT_REVIEW"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"approved\":true,\"comment\":\"confirmed\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.reviewStatus").value("APPROVED"));
}

@Test
void batchReviewOnlyApprovesNormalPendingResults() throws Exception {
    CheckResult normal = resultService.recordNumericResult(1L, 1L, "ALT", "U/L", 20, 0, 40, 400, 2L);
    CheckResult abnormal = resultService.recordNumericResult(1L, 2L, "Blood Glucose", "mmol/L", 8.2, 3.9, 6.1, 11.1, 2L);

    mockMvc.perform(post("/api/v1/admin/results/batch-review")
            .with(user("reviewer").roles("REVIEW_DOCTOR").authorities(() -> "REPORT_REVIEW"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"resultIds\":[" + normal.getId() + "," + abnormal.getId() + "],\"comment\":\"batch\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.approvedCount").value(1))
        .andExpect(jsonPath("$.data.skippedCount").value(1));
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:

```powershell
mvn -q -Dtest=ResultReviewComplianceTest test
```

Expected: FAIL because review endpoints do not exist.

- [ ] **Step 3: Add result query/review endpoints**

Add endpoints:

```java
@GetMapping({"/api/v1/admin/results", "/api/v1/medical/results"})
@PreAuthorize("hasAnyAuthority('RESULT_RECORD','REPORT_REVIEW')")

@PostMapping({"/api/v1/admin/results/{id}/review", "/api/v1/medical/results/{id}/review"})
@PreAuthorize("hasAuthority('REPORT_REVIEW')")

@PostMapping({"/api/v1/admin/results/batch-review", "/api/v1/medical/results/batch-review"})
@PreAuthorize("hasAuthority('REPORT_REVIEW')")
```

Batch review must approve only results where `abnormalLevel == NORMAL` and `reviewStatus == PENDING`; abnormal or critical results must be skipped for manual review.

- [ ] **Step 4: Add audit records**

Record:

- `RESULT_RECORD` for new result entry.
- `RESULT_REVIEW` for single review.
- `RESULT_BATCH_REVIEW` for batch review.

- [ ] **Step 5: Run result tests**

Run:

```powershell
mvn -q -Dtest=ResultReviewComplianceTest,ResultReportFlowTest test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```powershell
git add backend/src/main/java/com/xixin/health/result backend/src/test/java/com/xixin/health/admin/ResultReviewComplianceTest.java
git commit -m "feat: expose result review workflows"
```

---

### Task 5: Complete Report Management And Abnormal Warning

**Files:**
- Modify: `backend/src/main/java/com/xixin/health/report/ReportController.java`
- Modify: `backend/src/main/java/com/xixin/health/report/ReportService.java`
- Create: `backend/src/main/java/com/xixin/health/report/AdminReportController.java`
- Modify: `backend/src/main/java/com/xixin/health/report/ExamReport.java`
- Create: `backend/src/test/java/com/xixin/health/admin/ReportComplianceTest.java`

- [ ] **Step 1: Write failing report management tests**

Create tests:

```java
@Test
void reportGenerationUsesConfiguredTemplateAndRequiresApprovedResults() throws Exception {
    resultService.recordNumericResult(1L, 1L, "ALT", "U/L", 20, 0, 40, 400, 2L);

    mockMvc.perform(post("/api/v1/admin/reports/generate")
            .with(user("reviewer").roles("REVIEW_DOCTOR").authorities(() -> "REPORT_REVIEW"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"examRecordId\":1,\"reviewerId\":3}"))
        .andExpect(status().isConflict());
}

@Test
void criticalReportIsMarkedAndRequiresDoctorPublish() throws Exception {
    CheckResult result = resultService.recordNumericResult(1L, 1L, "Blood Glucose", "mmol/L", 12.2, 3.9, 6.1, 11.1, 2L);
    resultService.reviewResult(result.getId(), 3L, true, "critical confirmed");
    ExamReport report = reportService.generateReport(1L, 3L);

    mockMvc.perform(patch("/api/v1/admin/reports/{id}/publish", report.getId())
            .with(user("reviewer").roles("REVIEW_DOCTOR").authorities(() -> "REPORT_REVIEW"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"doctorConfirmed\":true}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.reportStatus").value("PUBLISHED"))
        .andExpect(jsonPath("$.data.abnormalStatus").value("CRITICAL"));
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:

```powershell
mvn -q -Dtest=ReportComplianceTest test
```

Expected: FAIL because admin report endpoints and publish flow do not exist.

- [ ] **Step 3: Add admin report endpoints**

Add:

```java
@GetMapping({"/api/v1/admin/reports", "/api/v1/medical/reports"})
@PreAuthorize("hasAuthority('REPORT_REVIEW')")

@PostMapping({"/api/v1/admin/reports/generate", "/api/v1/medical/reports/generate"})
@PreAuthorize("hasAuthority('REPORT_REVIEW')")

@PatchMapping({"/api/v1/admin/reports/{id}/publish", "/api/v1/medical/reports/{id}/publish"})
@PreAuthorize("hasAuthority('REPORT_REVIEW')")
```

- [ ] **Step 4: Remove hard-coded template ID**

Replace `new ExamReport(examRecordId, 1L, reviewerId, abnormalLevel)` with package/template lookup:

```java
ReportTemplate template = reportTemplateRepository.findFirstByPackageIdAndEnabledTrueOrderByVersionNoDesc(packageId)
    .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_CONFLICT, "No enabled report template for package"));
```

- [ ] **Step 5: Enforce abnormal publish rule**

If `abnormalStatus` is `ABNORMAL` or `CRITICAL`, `publishReport` must require `doctorConfirmed == true`; otherwise return `BusinessException(ErrorCode.BUSINESS_CONFLICT, "Abnormal report requires doctor confirmation")`.

- [ ] **Step 6: Run report tests**

Run:

```powershell
mvn -q -Dtest=ReportComplianceTest,ResultReportFlowTest test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```powershell
git add backend/src/main/java/com/xixin/health/report backend/src/test/java/com/xixin/health/admin/ReportComplianceTest.java
git commit -m "feat: complete admin report management"
```

---

### Task 6: Add Health Intervention Management

**Files:**
- Create: `backend/src/main/java/com/xixin/health/management/HealthManagement.java`
- Create: `backend/src/main/java/com/xixin/health/management/HealthManagementRepository.java`
- Create: `backend/src/main/java/com/xixin/health/management/HealthManagementService.java`
- Create: `backend/src/main/java/com/xixin/health/management/HealthManagementController.java`
- Modify: `backend/src/main/java/com/xixin/health/report/ReportService.java`
- Create: `backend/src/test/java/com/xixin/health/admin/HealthManagementComplianceTest.java`

- [ ] **Step 1: Write failing health management tests**

Create:

```java
@Test
void criticalReportCreatesHighRiskManagementRecord() {
    CheckResult result = resultService.recordNumericResult(1L, 1L, "Blood Glucose", "mmol/L", 12.2, 3.9, 6.1, 11.1, 2L);
    resultService.reviewResult(result.getId(), 3L, true, "critical confirmed");

    ExamReport report = reportService.generateReport(1L, 3L);

    assertThat(healthManagementRepository.findByReportId(report.getId()))
        .isPresent()
        .get()
        .extracting(HealthManagement::getRiskLevel)
        .isEqualTo("HIGH");
}

@Test
void followUpPlanCreatesNotification() throws Exception {
    mockMvc.perform(post("/api/v1/admin/health-management/1/follow-up")
            .with(user("reviewer").roles("REVIEW_DOCTOR").authorities(() -> "REPORT_REVIEW"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"followUpPlan":"2026-07-23 复查空腹血糖","interventionPlan":"低糖饮食，每周运动三次"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.managementStatus").value("FOLLOW_UP_PLANNED"));
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:

```powershell
mvn -q -Dtest=HealthManagementComplianceTest test
```

Expected: FAIL because health management entity/controller do not exist.

- [ ] **Step 3: Implement entity and repository**

Map `t_health_management` with fields:

- `userId`
- `reportId`
- `riskLevel`
- `managementStatus`
- `followUpPlan`
- `interventionPlan`

Use status values: `OPEN`, `FOLLOW_UP_PLANNED`, `INTERVENTION_ACTIVE`, `CLOSED`.

- [ ] **Step 4: Create high-risk record after report generation**

In `ReportService.generateReport`, if abnormal level is `CRITICAL`, call:

```java
healthManagementService.openHighRiskRecord(report.getUserId(), report.getId(), "HIGH");
```

If abnormal level is `ABNORMAL`, create risk level `MEDIUM`.

- [ ] **Step 5: Add follow-up endpoints**

Add:

```java
@GetMapping("/api/v1/admin/health-management")
@PreAuthorize("hasAuthority('REPORT_REVIEW')")

@PostMapping("/api/v1/admin/health-management/{id}/follow-up")
@PreAuthorize("hasAuthority('REPORT_REVIEW')")
```

The follow-up endpoint must update the record and create a notification with title `健康随访提醒`.

- [ ] **Step 6: Run health management tests**

Run:

```powershell
mvn -q -Dtest=HealthManagementComplianceTest test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```powershell
git add backend/src/main/java/com/xixin/health/management backend/src/main/java/com/xixin/health/report backend/src/test/java/com/xixin/health/admin/HealthManagementComplianceTest.java
git commit -m "feat: add high risk health intervention management"
```

---

### Task 7: Add Medical Configuration Review Lifecycle

**Files:**
- Create: `backend/src/main/resources/db/migration/V2__admin_compliance_review.sql`
- Modify: `backend/src/main/java/com/xixin/health/configuration/ExamItem.java`
- Modify: `backend/src/main/java/com/xixin/health/configuration/BusinessRule.java`
- Modify: `backend/src/main/java/com/xixin/health/configuration/ReportTemplate.java`
- Create: `backend/src/main/java/com/xixin/health/configuration/ConfigReviewController.java`
- Create: `backend/src/test/java/com/xixin/health/admin/AdminConfigurationComplianceTest.java`

- [ ] **Step 1: Write failing config review test**

Create:

```java
@Test
void medicalRuleChangeRequiresReviewBeforeEffective() throws Exception {
    long ruleId = postRule("RULE_CRITICAL_GLU", "危急值规则", "MEDICAL_RESULT", "{\"criticalMax\":11.1}");

    mockMvc.perform(put("/api/v1/admin/rules/{id}", ruleId)
            .with(user("ops").roles("OPERATIONS_ADMIN").authorities(() -> "CONFIG_MANAGE"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"ruleCode":"RULE_CRITICAL_GLU","ruleName":"危急值规则","ruleType":"MEDICAL_RESULT","ruleContent":"{\\"criticalMax\\":10.5}","enabled":true}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.reviewStatus").value("PENDING_REVIEW"))
        .andExpect(jsonPath("$.data.enabled").value(false));

    mockMvc.perform(post("/api/v1/admin/config-reviews/rules/{id}", ruleId)
            .with(user("doctor").roles("REVIEW_DOCTOR").authorities(() -> "REPORT_REVIEW"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"approved\":true,\"comment\":\"专业审核通过\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.reviewStatus").value("APPROVED"))
        .andExpect(jsonPath("$.data.enabled").value(true));
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:

```powershell
mvn -q -Dtest=AdminConfigurationComplianceTest test
```

Expected: FAIL because review status fields and review endpoint do not exist.

- [ ] **Step 3: Add migration**

Create `V2__admin_compliance_review.sql`:

```sql
ALTER TABLE t_exam_item ADD COLUMN review_status VARCHAR(32) NOT NULL DEFAULT 'APPROVED';
ALTER TABLE t_exam_item ADD COLUMN reviewer_id BIGINT;
ALTER TABLE t_exam_item ADD COLUMN reviewed_at TIMESTAMP;

ALTER TABLE t_business_rule ADD COLUMN review_status VARCHAR(32) NOT NULL DEFAULT 'APPROVED';
ALTER TABLE t_business_rule ADD COLUMN reviewer_id BIGINT;
ALTER TABLE t_business_rule ADD COLUMN reviewed_at TIMESTAMP;

ALTER TABLE t_report_template ADD COLUMN review_status VARCHAR(32) NOT NULL DEFAULT 'APPROVED';
ALTER TABLE t_report_template ADD COLUMN reviewer_id BIGINT;
ALTER TABLE t_report_template ADD COLUMN reviewed_at TIMESTAMP;
```

- [ ] **Step 4: Implement review status behavior**

When rule type starts with `MEDICAL_` or item reference/critical values change:

```java
entity.markPendingReview();
entity.setEnabled(false);
```

When reviewer approves:

```java
entity.approveReview(reviewerId);
entity.setEnabled(true);
```

When reviewer rejects:

```java
entity.rejectReview(reviewerId);
entity.setEnabled(false);
```

- [ ] **Step 5: Add review endpoints**

Add:

```java
@PostMapping("/api/v1/admin/config-reviews/exam-items/{id}")
@PreAuthorize("hasAuthority('REPORT_REVIEW')")

@PostMapping("/api/v1/admin/config-reviews/rules/{id}")
@PreAuthorize("hasAuthority('REPORT_REVIEW')")

@PostMapping("/api/v1/admin/config-reviews/report-templates/{id}")
@PreAuthorize("hasAuthority('REPORT_REVIEW')")
```

- [ ] **Step 6: Run configuration tests**

Run:

```powershell
mvn -q -Dtest=AdminConfigurationComplianceTest test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```powershell
git add backend/src/main/resources/db/migration/V2__admin_compliance_review.sql backend/src/main/java/com/xixin/health/configuration backend/src/test/java/com/xixin/health/admin/AdminConfigurationComplianceTest.java
git commit -m "feat: require review for medical configuration changes"
```

---

### Task 8: Harden Audit Logs With Append-Only Hash Chain

**Files:**
- Create: `backend/src/main/resources/db/migration/V3__audit_hash_chain.sql`
- Modify: `backend/src/main/java/com/xixin/health/audit/OperationLog.java`
- Modify: `backend/src/main/java/com/xixin/health/audit/AuditService.java`
- Create: `backend/src/main/java/com/xixin/health/audit/AuditIntegrityService.java`
- Create: `backend/src/test/java/com/xixin/health/admin/AuditComplianceTest.java`

- [x] **Step 1: Write failing audit integrity test**

Create:

```java
@Test
void auditHashChainDetectsTampering() {
    OperationLog first = auditService.record("1", "LOGIN", "sys_user", "1", "SUCCESS", "127.0.0.1", "JUnit", "trace-1");
    OperationLog second = auditService.record("1", "CONFIG_UPDATE", "t_business_rule", "2", "SUCCESS", "127.0.0.1", "JUnit", "trace-2");

    assertThat(second.getPreviousHash()).isEqualTo(first.getEntryHash());
    assertThat(auditIntegrityService.verify()).isTrue();

    operationLogRepository.findById(second.getId()).orElseThrow().forceTamperForTest("FAILED");

    assertThat(auditIntegrityService.verify()).isFalse();
}
```

- [x] **Step 2: Run the test to verify it fails**

Run:

```powershell
mvn -q -Dtest=AuditComplianceTest test
```

Expected: FAIL because hash fields and verification do not exist.

- [x] **Step 3: Add migration**

Create:

```sql
ALTER TABLE t_operation_log ADD COLUMN previous_hash VARCHAR(128);
ALTER TABLE t_operation_log ADD COLUMN entry_hash VARCHAR(128) NOT NULL DEFAULT '';
CREATE INDEX idx_operation_log_time ON t_operation_log(operation_time);
CREATE INDEX idx_operation_log_hash ON t_operation_log(entry_hash);
```

- [x] **Step 4: Compute hash on write**

In `AuditService.record`, load latest log by time/id, set `previousHash`, and compute:

```java
String payload = operatorId + "|" + operationType + "|" + targetType + "|" + targetId + "|" + result + "|" + ipAddress + "|" + userAgent + "|" + traceId + "|" + previousHash;
String entryHash = sha256(payload);
```

- [x] **Step 5: Add verification service**

`AuditIntegrityService.verify()` must read logs ordered by `id`, recompute each hash, and return false if either `previousHash` or `entryHash` differs.

- [x] **Step 6: Run audit tests**

Run:

```powershell
mvn -q -Dtest=AuditComplianceTest,AuditServiceTest test
```

Expected: PASS.

- [x] **Step 7: Commit**

```powershell
git add backend/src/main/resources/db/migration/V3__audit_hash_chain.sql backend/src/main/java/com/xixin/health/audit backend/src/test/java/com/xixin/health/admin/AuditComplianceTest.java
git commit -m "feat: add audit hash chain integrity"
```

---

### Task 9: Complete Statistics Compliance

**Files:**
- Modify: `backend/src/main/java/com/xixin/health/statistics/StatisticsController.java`
- Modify: `backend/src/main/java/com/xixin/health/statistics/StatisticsService.java`
- Create: `backend/src/test/java/com/xixin/health/admin/StatisticsComplianceTest.java`

- [x] **Step 1: Write failing statistics tests**

Create:

```java
@Test
void healthStatisticsAreDesensitized() throws Exception {
    mockMvc.perform(get("/api/v1/admin/statistics/health")
            .param("startDate", "2026-01-01")
            .param("endDate", "2026-12-31")
            .with(user("ops").roles("OPERATIONS_ADMIN").authorities(() -> "STATISTICS_QUERY")))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("phone"))))
        .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("idCard"))));
}

@Test
void exportSupportsBusinessAndHealthExcelPdf() throws Exception {
    mockMvc.perform(get("/api/v1/admin/statistics/export")
            .param("type", "health")
            .param("format", "PDF")
            .with(user("ops").roles("OPERATIONS_ADMIN").authorities(() -> "STATISTICS_EXPORT")))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/pdf")));
}

@Test
void statisticsRangeCannotExceedOneYear() throws Exception {
    mockMvc.perform(get("/api/v1/admin/statistics/business")
            .param("startDate", "2025-01-01")
            .param("endDate", "2026-12-31")
            .with(user("ops").roles("OPERATIONS_ADMIN").authorities(() -> "STATISTICS_QUERY")))
        .andExpect(status().isBadRequest());
}
```

- [x] **Step 2: Run the test to verify it fails**

Run:

```powershell
mvn -q -Dtest=StatisticsComplianceTest test
```

Expected: FAIL because health export currently reuses business export and permissions are not fine-grained before Task 2.

- [x] **Step 3: Split business and health export paths**

`StatisticsService.export` must branch:

```java
if ("health".equalsIgnoreCase(type)) {
    HealthStatistics health = health(startDate, endDate, branchId);
    return exportHealth(normalizedFormat, health);
}
BusinessStatistics business = business(startDate, endDate, branchId, "TOTAL");
return exportBusiness(normalizedFormat, business);
```

- [x] **Step 4: Ensure aggregate-only health statistics**

Health statistics DTOs must include only:

- total reports
- abnormal report count
- critical report count
- abnormal level distribution
- risk distribution
- branch ID filter value

Do not include names, phone numbers, ID cards, profile IDs, or report contents.

- [x] **Step 5: Run statistics tests**

Run:

```powershell
mvn -q -Dtest=StatisticsComplianceTest test
```

Expected: PASS.

- [x] **Step 6: Commit**

```powershell
git add backend/src/main/java/com/xixin/health/statistics backend/src/test/java/com/xixin/health/admin/StatisticsComplianceTest.java
git commit -m "feat: complete desensitized statistics export"
```

---

### Task 10: Replace Static Admin Page With React + Ant Design Pro

**Files:**
- Create: `admin/package.json`
- Create: `admin/src/app.tsx`
- Create: `admin/src/services/request.ts`
- Create: `admin/src/pages/config/ExamItemPage.tsx`
- Create: `admin/src/pages/config/PackagePage.tsx`
- Create: `admin/src/pages/config/BranchPage.tsx`
- Create: `admin/src/pages/config/DepartmentPage.tsx`
- Create: `admin/src/pages/config/RulePage.tsx`
- Create: `admin/src/pages/iam/RolePage.tsx`
- Create: `admin/src/pages/iam/AccountPage.tsx`
- Create: `admin/src/pages/exam/CheckInPage.tsx`
- Create: `admin/src/pages/exam/DepartmentQueuePage.tsx`
- Create: `admin/src/pages/result/ReviewPage.tsx`
- Create: `admin/src/pages/report/ReportWarningPage.tsx`
- Create: `admin/src/pages/management/RiskUserPage.tsx`
- Create: `admin/src/pages/statistics/BusinessStatisticsPage.tsx`
- Create: `admin/src/pages/audit/AuditLogPage.tsx`
- Modify: `backend/src/main/resources/static/admin.html`

- [x] **Step 1: Create frontend package**

Create `admin/package.json`:

```json
{
  "scripts": {
    "dev": "vite --host 0.0.0.0 --port 5173",
    "build": "tsc -b && vite build",
    "test": "vitest run"
  },
  "dependencies": {
    "@ant-design/icons": "^5.4.0",
    "@ant-design/pro-components": "^2.8.0",
    "antd": "^5.21.0",
    "react": "^18.3.1",
    "react-dom": "^18.3.1",
    "vite": "^5.4.0"
  },
  "devDependencies": {
    "@types/react": "^18.3.5",
    "@types/react-dom": "^18.3.0",
    "typescript": "^5.6.2",
    "vitest": "^2.1.1"
  }
}
```

- [x] **Step 2: Implement shared request client**

Create `admin/src/services/request.ts`:

```ts
const API_BASE = '/api/v1';

export async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('xixin_admin_access_token');
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers ?? {})
    }
  });
  if (!response.ok) {
    throw new Error(await response.text());
  }
  const body = await response.json();
  return body.data as T;
}
```

- [x] **Step 3: Build Ant Design Pro layout**

`admin/src/app.tsx` must render routes for:

- 总览
- 基础配置
- 权限与账号
- 体检过程
- 结果审核
- 报告管理
- 健康干预
- 数据统计
- 操作审计

Use `ProLayout`, `ProTable`, `ProForm`, `ModalForm`, and `PageContainer`.

- [x] **Step 4: Keep old static page as redirect**

Modify `backend/src/main/resources/static/admin.html`:

```html
<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8">
  <meta http-equiv="refresh" content="0; url=/admin/index.html">
  <title>熙心健康管理系统</title>
</head>
<body>
  <a href="/admin/index.html">进入熙心健康管理系统</a>
</body>
</html>
```

- [x] **Step 5: Run frontend build**

Run:

```powershell
cd admin-web
pnpm install
pnpm build
```

Expected: build succeeds and outputs `admin/dist`.

- [x] **Step 6: Commit**

```powershell
git add admin-web backend/src/main/resources/static/admin.html
git commit -m "feat: add React Ant Design Pro admin console"
```

---

### Task 11: Final Verification Gate

**Files:**
- Modify: `README.md`
- Modify: `backend/README.md`
- Modify: `docs/compliance/admin-requirements-traceability.md`

- [x] **Step 1: Run backend tests**

Run:

```powershell
cd backend
mvn -q test
```

Expected: all tests pass.

- [x] **Step 2: Run frontend tests and build**

Run:

```powershell
cd admin-web
pnpm test
pnpm build
```

Expected: tests and build pass.

- [x] **Step 3: Update documentation**

Add a section to `backend/README.md`:

```markdown
## 管理端合规开发约束

- 所有 `/api/v1/admin/**` 接口必须使用 `@PreAuthorize` 绑定具体权限码。
- 管理端账号响应禁止返回 `passwordHash`、加密密钥、身份证号等敏感字段。
- 医疗相关配置变更必须进入 `PENDING_REVIEW`，审核通过后才可启用。
- 报告生成必须等待必检项目完成且检查结果审核通过。
- 异常和危急值报告发布必须经过医生确认。
- 统计接口只返回脱敏聚合数据。
- 所有管理端新增、修改、删除、导出、审核操作必须写入审计日志。
```

- [x] **Step 4: Re-check traceability matrix**

For each row in `docs/compliance/admin-requirements-traceability.md`, verify the referenced backend file, frontend file, and test file exists.

Run:

```powershell
Test-Path docs/compliance/admin-requirements-traceability.md
Test-Path backend/src/test/java/com/xixin/health/admin/AdminComplianceSmokeTest.java
Test-Path admin/src/app.tsx
```

Expected: all commands print `True`.

- [x] **Step 5: Commit**

```powershell
git add README.md backend/README.md docs/compliance/admin-requirements-traceability.md
git commit -m "docs: document admin compliance development rules"
```

---

## Self-Review Checklist

- [ ] Every required management-side module in the function structure table maps to a backend implementation, frontend page, and test.
- [ ] Existing partial capabilities are strengthened instead of duplicated.
- [ ] No admin endpoint relies only on broad role matching when a concrete permission code exists.
- [ ] No account response exposes `passwordHash`.
- [ ] Medical workflow is testable from check-in through report publication.
- [ ] Health intervention is not only a database table; it has service, API, notification, and test coverage.
- [ ] Audit logs are append-only in application behavior and protected by hash-chain verification.
- [ ] Statistics exports support both business and health data without personal identifiers.
- [ ] The management frontend satisfies the React + Ant Design Pro requirement.

# Admin Backend Interfaces Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete the backend API surface needed by the administrator端 pages described in `06.第四组-概要设计说明书-熙心健康管理系统_修改版.docx`.

**Architecture:** Keep the existing Spring Boot modular monolith and add narrowly scoped admin controllers/services/repositories. Expose `/api/v1/admin/**` endpoints for the frontend while also keeping compatible `/api/v1/system/**` and `/api/v1/statistics/**` aliases where the概要设计 table names them.

**Tech Stack:** Spring Boot 3.3, Spring MVC, Spring Security JWT/RBAC, Spring Data JPA, Flyway/H2/MySQL-compatible schema.

---

### Task 1: Admin API Contract Tests

**Files:**
- Create: `backend/src/test/java/com/xixin/health/admin/AdminBackendApiTest.java`

- [ ] **Step 1: Write failing MockMvc tests**

Cover default admin login, exam item CRUD, package CRUD, branch CRUD, rule CRUD, role CRUD, account CRUD/status/reset-password, audit-log filtering, statistics, and export endpoints.

- [ ] **Step 2: Run targeted test to verify it fails**

Run: `mvn -q -Dtest=AdminBackendApiTest test`

Expected: compile or 404 failures because the new controllers/services do not exist yet.

### Task 2: Configuration CRUD APIs

**Files:**
- Modify: `backend/src/main/java/com/xixin/health/configuration/ExamItem.java`
- Modify: `backend/src/main/java/com/xixin/health/configuration/ExamPackage.java`
- Modify: `backend/src/main/java/com/xixin/health/configuration/Branch.java`
- Create: `backend/src/main/java/com/xixin/health/configuration/BusinessRule.java`
- Create/modify repositories and controllers under `backend/src/main/java/com/xixin/health/configuration/`

- [ ] **Step 1: Add entity update methods and missing repositories**
- [ ] **Step 2: Add admin configuration controller endpoints**
- [ ] **Step 3: Run targeted tests**

### Task 3: IAM Admin APIs And Bootstrap Admin

**Files:**
- Modify: `backend/src/main/java/com/xixin/health/iam/Role.java`
- Modify: `backend/src/main/java/com/xixin/health/iam/UserAccount.java`
- Modify: `backend/src/main/java/com/xixin/health/iam/RoleController.java`
- Modify: `backend/src/main/java/com/xixin/health/iam/AdminAccountController.java`
- Create: `backend/src/main/java/com/xixin/health/iam/AdminBootstrapConfig.java`

- [ ] **Step 1: Seed default system administrator account**
- [ ] **Step 2: Add role/account create-update-delete/status/reset-password endpoints**
- [ ] **Step 3: Run targeted tests**

### Task 4: Audit Filtering And Statistics APIs

**Files:**
- Modify: `backend/src/main/java/com/xixin/health/audit/AuditLogController.java`
- Create: `backend/src/main/java/com/xixin/health/common/api/PageResult.java`
- Create: `backend/src/main/java/com/xixin/health/statistics/StatisticsController.java`
- Create: `backend/src/main/java/com/xixin/health/statistics/StatisticsService.java`

- [ ] **Step 1: Add paged/filterable audit-log response**
- [ ] **Step 2: Add business/health statistics JSON endpoints**
- [ ] **Step 3: Add CSV export endpoint with audit logging**
- [ ] **Step 4: Run targeted tests**

### Task 5: Full Verification

- [ ] **Step 1: Run `mvn -q test`**
- [ ] **Step 2: Report any intentionally limited behavior**

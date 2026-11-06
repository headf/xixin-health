# Test Client Frontend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a same-origin static test frontend for calling the Spring Boot backend APIs.

**Architecture:** Add one static HTML file under Spring Boot resources, backed by a focused MockMvc static-resource test. The frontend uses browser `fetch`, local token persistence, and a shared response panel.

**Tech Stack:** Spring Boot static resources, MockMvc, HTML, CSS, vanilla JavaScript.

---

### Task 1: Static Resource Contract

**Files:**
- Create: `backend/src/test/java/com/xixin/health/staticclient/TestClientStaticPageTest.java`
- Create: `backend/src/main/resources/static/test-client.html`

- [ ] **Step 1: Write the failing test**

Create a MockMvc test that requests `/test-client.html` and expects the page shell and API base marker.

- [ ] **Step 2: Run the test to verify it fails**

Run: `mvn -q -Dtest=TestClientStaticPageTest test`

Expected: 404 because the static HTML file does not exist yet.

- [ ] **Step 3: Add the static test client**

Add `test-client.html` with compact panels for auth, catalogs, workflow requests, admin checks, and custom endpoint calls.

- [ ] **Step 4: Run the targeted test**

Run: `mvn -q -Dtest=TestClientStaticPageTest test`

Expected: pass.

- [ ] **Step 5: Run full backend tests**

Run: `mvn -q test`

Expected: pass.

- [ ] **Step 6: Refresh running backend and verify browser entry**

Copy resources to `target/classes` through Maven, restart or refresh the running backend if needed, and verify `http://localhost:8080/test-client.html` returns HTTP 200.

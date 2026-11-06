# Test Client Frontend Design

## Goal

Create a simple browser test client for the Xixin Health backend so backend APIs can be exercised without Postman or a separate frontend build.

## Approach

Use a single static HTML file served by Spring Boot from `src/main/resources/static/test-client.html`. The page calls same-origin `/api/v1` endpoints, stores the login access token in `localStorage`, and attaches `Authorization: Bearer <token>` for protected endpoints.

## Scope

- Register and login.
- Show and reuse the current access token.
- Fetch public package and branch catalogs.
- Create health profile, appointment, order, and simulated payment requests.
- Test admin role and audit-log endpoints.
- Provide a custom request panel for any backend endpoint.

## UI Direction

The page is an internal operations test console: compact, clear, and utility-first. It avoids marketing layout and focuses on fast form entry, visible request status, and readable JSON responses.

## Out Of Scope

- Formal user-facing frontend.
- CORS support for a separate domain.
- Persistent business workflow state beyond local token storage and copied IDs.

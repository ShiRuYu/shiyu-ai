# Cross-repository repair implementation plan

> Follow `docs/superpowers/specs/2026-10-05-cross-repo-repairs-design.md`. The user has already authorized moving directly from this plan into code.

**Goal:** Make the audited education and conversation journeys truthful and usable, then close the checks that let frontend/backend drift go undetected.

**Architecture:** Add narrow student-facing backend endpoints and persistent exam attempts; update Vue clients to consume the real contracts and authenticated identity. Keep administrative CRUD separate from student paper/result access. Use existing tenant-aware data objects, repositories, request helpers, and router conventions.

**Tech Stack:** Java 17/Spring Boot/MyBatis-Plus/Maven/JUnit; Vue 3/TypeScript/Naive UI/pnpm/Vitest/Playwright; H2 baseline plus supported database migration paths.

**Spec:** `docs/superpowers/specs/2026-10-05-cross-repo-repairs-design.md`

**Global constraints:** Work only in the two isolated worktrees; preserve tenant isolation; never send answer keys in an unsubmitted paper; avoid paid provider dependencies in default tests; write a failing meaningful test before substantive behavior changes; run each relevant gate after implementation.

## Task 1 — Student exam backend (P0)

1. Inspect exam/question repositories, actor/student identity APIs, persistence migration conventions, and role permissions. Locate the exact H2/other database schema sources.
2. Add failing service/controller tests for an ordered, answer-free paper; active time window; actor-to-student lookup; unknown answers; objective scoring; duplicate attempt; tenant and result ownership.
3. Add request/response DTOs and student paper/submission/result endpoints. Assemble sections/questions server-side; normalize option labels; derive student from actor, not a client-sent ID.
4. Persist attempts and answers with uniqueness and tenant constraints. Update fresh schema and upgrade scripts. Add a supported way to attach ordered questions to an exam, and create coherent test fixture data.
5. Run targeted Maven tests and module compilation.

## Task 2 — Student exam frontend (P0)

1. Add tests for paper/result API types and exam availability decisions. Make the API model match backend DTOs.
2. Update exam list to use server pagination and real subject/status/time filters; only takeable rows navigate to the paper.
3. Update take page to load the safe paper, track answers by question ID, submit once, handle timeout/error, and navigate using returned submission ID.
4. Update result page to fetch the persisted result and show scored/pending status accurately.
5. Run Vitest, typecheck, and production build for the relevant app.

## Task 3 — Conversation correctness (P1)

1. Add failing tests with real `GenerationEvent` SSE examples for COMPLETED, FAILED, CANCELLED, and incomplete streams.
2. Parse the event envelope and reject unsuccessful terminal states. Keep partial text only as partial UI state, never as successful response.
3. Guard conversation switching with a request token and capture one ID per send; make local/persisted IDs explicit for edit and retry.
4. Add focused UI/composable tests for switching and retry, then run Vitest/typecheck.

## Task 4 — Student identity and learning actions (P1)

1. Add failing backend tests for current-student lookup under tenant/actor context; add `GET /api/education/students/me` with a student-safe permission route.
2. Remove `studentId: 1` and the fallback `1` across tutor, practice, planner, report and related pages. Surface missing enrollment explicitly; use selected student in staff-only views.
3. Add course detail and question practice routes that consume IDs from the list; connect answer or checking behavior to an existing or new narrow backend contract.
4. Verify role grants, route permissions, and frontend test coverage.

## Task 5 — Contract and journey gates (P1)

1. Extend the UI contract checker to validate bodies and important response schemas, especially student paper, submission, and generation SSE event fields.
2. Add a non-provider dependent browser journey against a disposable backend. Wire it into CI with paired backend/UI revisions or a clearly pinned artifact instead of an uncontrolled latest-main snapshot.
3. Run contract, boundary, browser smoke, and both repositories' normal CI-equivalent commands.

## Task 6 — Pagination, resumable upload, and onboarding (P2/P3)

1. Make course lists server-paginated and filter-aware; test a second page and empty page.
2. Add resumable upload state reuse and missing-chunk recovery; test interrupted upload and retry without starting a second session.
3. Update frontend README with actual workspace setup, development, tests, and paired-backend instructions.
4. Run relevant unit/type/build gates and inspect both worktree diffs for accidental contract drift.

## Sequencing and review

Tasks 1 and 3 can proceed in parallel because they edit disjoint repos. Task 2 follows the backend exam DTO; Task 4 can run alongside after identity endpoints are defined. Task 5 follows contract stabilization. Task 6 is independent after core flows. Review code and tests at each task boundary; record any externally blocked integration prerequisite explicitly instead of silently claiming coverage.

## Development checkpoint (2026-10-05)

- Started and verified: Task 3 conversation SSE event/error handling and stable conversation/message identity; backend runtime metadata event projection. Frontend conversation tests and backend generation controller tests pass.
- Started and verified: Task 4 authenticated `/students/me` lookup, frontend removal of the ID `1` fallback, and explicit staff student selection in affected views. Focused student tests and frontend typecheck pass.
- Started and verified: Task 6 large-document session recovery and explicit cancellation. Interrupted upload tests pass.
- Remaining P0: Tasks 1 and 2 require the full paper/attempt persistence and results contract. A draft-only exam change was removed because it would have left exams unpublishable; implement the whole contract before changing exam status behavior.
- Remaining P1/P2/P3: learning/practice destination flows, deeper request/response contract and browser CI gates, course pagination/filtering, and README. The current API contract checker still verifies route shapes only; its passing result does not establish response-schema compatibility.

Checkpoint commands: UI Vitest `25 files / 79 tests`, typecheck, API route contract, feature boundaries, production build; backend targeted `CurrentStudentControllerTest` and `GenerationControllerRuntimeTest` all passed. Repeat these after future exam and learning changes.

## Completion checkpoint (2026-10-06)

- Tasks 1–6 are implemented in the paired worktrees. The student exam flow now stores tenant-scoped attempts and answers, publishes validated papers, scores objective answers, and returns only the owner's result. Student learning and question practice, current-student binding, conversation state, course pagination, resumable uploads, contract checks, browser CI, and frontend onboarding are implemented.
- Backend `mvn --batch-mode --no-transfer-progress -q -Pstrict-warnings verify '-Ddependency-check.skip=true'` passed across the entire reactor after the temporary local browser backend was stopped. The first run's tests passed, but Windows jar replacement was blocked by that still-running local backend; the clean rerun passed.
- Frontend: 25 Vitest files / 80 tests, typecheck, production build, `test:contract`, `test:openapi`, and `check:boundaries` passed. The provider-free Playwright exam browser journey passed against a disposable H2 backend. Workflow YAML parsed successfully.
- PostgreSQL migration was reviewed alongside the baseline but was not exercised against a live PostgreSQL instance. The GitHub Actions paired-repository job is present only in the local worktree and cannot run remotely until both branches are pushed.

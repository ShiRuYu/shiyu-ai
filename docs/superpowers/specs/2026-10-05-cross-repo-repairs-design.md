# Cross-repository education and conversation repairs

Date: 2026-10-05

## Context and boundaries

The backend is `shiyu-ai` (Spring Boot, Java, Maven); the frontend is `shiyu-ui` (Vue 3, TypeScript, pnpm). The existing education and conversation screens advertise flows that the two APIs do not yet complete. Keep tenant and actor identity on the server, preserve existing CRUD contracts, and add student-facing contracts explicitly. Development happens in the two `fix/2026-10-05-cross-repo-repairs` worktrees.

## Priority and intended behavior

| Priority | Flow | Behavior to deliver |
| --- | --- | --- |
| P0 | Student exam | The paper endpoint returns ordered sections and questions without answers or analysis. A student can answer, submit once, persist the attempt, and open an actual result. Draft, deleted, unavailable, or expired exams cannot be started. |
| P1 | Conversation | The browser interprets the real `GenerationEvent` envelope and surfaces FAILED/CANCELLED as failure. Selection, creation, editing, and retry use stable conversation and message identities. |
| P1 | Student binding | Student features resolve the current student by authenticated actor and tenant, never by hardcoded ID `1` or a silent fallback. Administrative views require explicit selected student context where applicable. |
| P1 | Learning and practice | Course and question actions navigate to a usable detail or answer flow that consumes the selected identifier. |
| P1 | Contract and journey checks | Check actual request and response shapes, including SSE event envelopes, and add a non-provider dependent browser journey to CI. |
| P2 | Course and exam lists | Pagination and filters use server data; exam actions reflect availability, time window, and real subject data. |
| P2 | Large document upload | Reuse a recoverable upload session, ask the server which chunks exist, upload missing chunks, and keep retry state after a transient failure. |
| P3 | Repository onboarding | Replace the generic frontend template README with accurate development and test commands. |

## Exam contract

- Add a distinct student paper DTO and `GET /api/education/exam/{id}/paper`. `GET /api/education/exam/available` lists takeable exams. `POST /api/education/exam/{id}/attempts` starts or resumes an attempt and returns the safe paper with attempt ID and absolute deadline. Never serialize canonical answer or analysis in this response.
- `POST /api/education/exam/attempts/{attemptId}/submit` accepts answers keyed by question ID. Resolve student ID from the authenticated actor in the current tenant. Reject unknown or duplicate question IDs, invalid question formats, and unavailable exams. Repeat submission by the same owner returns the saved result.
- Persist attempts and per-question answers in tenant-scoped tables with a unique exam/student attempt constraint. Snapshot question content and keys at start, score objective types deterministically, and make subjective answer status explicit rather than presenting an invented score. `GET /api/education/exam/attempts/{id}` returns only the owning student's result.
- Administrative exam creation accepts ordered sections/question IDs and creates a draft. `POST /api/education/exam/{id}/publish` validates composition, time window, and supported grading types before making it takeable. Ensure fresh H2 baseline and existing-database migrations represent the same schema. Add a fixture or test setup with an active in-window exam and coherent scores.

## Client and identity contract

- Treat `GenerationEvent` (`generationRunId`, `sequence`, `type`, `payload`, `createdAt`) as the SSE payload. Terminal FAILED and CANCELLED events reject generation; success requires COMPLETED. Avoid displaying partial content as a successful assistant answer on failure.
- A conversation switch commits only when its messages load; a send captures one conversation ID for the entire request. Local-only messages carry explicit persistence state; edit and retry never infer it from ID length.
- Add a `me` student lookup that resolves the user/tenant on the backend. The frontend removes the fallback `1`, handles absent student records, and passes resolved identity only where existing APIs require it. Requests that access another student's resources must continue to be permission checked by the backend.
- Course and question detail routes read their route parameters; lists use response pagination and filters. A question answer flow should submit to a real backend endpoint or give an explicit answer checking flow that is already supported.
- Large-upload session state is keyed by space plus stable file identity and validated against server status before retry. Cancel a session only when the user requests cancellation; transient errors retain retry state.

## Verification

Meaningful service tests cover exam availability, tenant/actor binding, question secrecy, scoring, duplicate submission, and result ownership. Client tests cover SSE failure, conversation races, pagination, identity resolution, and resumable retry. Run targeted tests first, then Maven and frontend type, unit, API contract, build, and boundary checks. A browser smoke journey should use a disposable local backend and must not require a paid model provider.

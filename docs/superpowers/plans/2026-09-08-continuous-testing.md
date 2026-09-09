# Continuous Testing Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a recoverable Windows continuous-testing scheduler for the backend and frontend repositories.

**Architecture:** A Python standard-library daemon owns SQLite state and writes rebuildable JSON snapshots. It captures immutable repository snapshots, runs bounded commands in isolated worktrees, records task outcomes, and exposes a small CLI for lifecycle control. Later phases add exploration and repair workers without changing the state model.

**Tech Stack:** Python 3.10+, SQLite, subprocess, pathlib, hashlib, JSON; Maven, pnpm/Vitest, and Playwright as external runners.

**Spec:** `C:/Users/87670/.codex/attachments/257f6927-15fb-440b-9820-7c18d4f52d09/goal-objective.md`

## Global Constraints

- SQLite is the authoritative state source; JSON is a rebuildable snapshot.
- Runtime data lives under `.testing/` and is ignored by Git.
- Every run records backend/frontend commit SHAs and environment metadata.
- Test processes are bounded, owned, and stoppable; no global process killing.
- PASS, SKIPPED, BLOCKED, TIMEOUT, and infrastructure failures remain distinct.
- The main development branches are never automatically merged or pushed.

### Task 1: Recoverable state store and lifecycle CLI

**Files:**
- Create: `scripts/continuous-testing/__init__.py`
- Create: `scripts/continuous-testing/store.py`
- Create: `scripts/continuous-testing/snapshot.py`
- Create: `scripts/continuous-testing/cli.py`
- Create: `scripts/continuous-testing/tests/test_store.py`
- Modify: `.gitignore`

**Interfaces:**
- `StateStore(path).initialize()` creates schema and is idempotent.
- `StateStore.create_run(version, metadata)` and `StateStore.update_run(run_id, status, **fields)` persist lifecycle state.
- `StateStore.add_task(run_id, kind, command, status)` and `StateStore.list_tasks(run_id=None)` persist bounded jobs.
- `Snapshot.capture(backend, frontend)` returns commit and dirty-tree hashes.
- CLI commands: `start`, `status`, `pause`, `resume`, `stop`, `replay <failure-id>`.

- [ ] Write failing tests for schema initialization, run recovery, and status transitions.
- [ ] Implement SQLite store with transactions and JSON snapshot generation.
- [ ] Implement CLI lifecycle commands and safe filesystem defaults.
- [ ] Verify tests and CLI help/status output.

### Task 2: Owned process execution and baseline gates

**Files:**
- Create: `scripts/continuous-testing/runner.py`
- Create: `scripts/continuous-testing/gates.py`
- Create: `scripts/continuous-testing/tests/test_runner.py`
- Modify: `scripts/continuous-testing/cli.py`

- [ ] Test timeout, captured output, and process ownership metadata.
- [ ] Implement bounded subprocess runner and backend/frontend gate definitions.
- [ ] Record distinct task outcomes and artifacts.

### Task 3: Snapshot isolation and scheduler loop

**Files:**
- Create: `scripts/continuous-testing/scheduler.py`
- Create: `scripts/continuous-testing/worktree.py`
- Create: `scripts/continuous-testing/tests/test_scheduler.py`

- [ ] Test stable-change detection and pause/resume recovery.
- [ ] Implement isolated worktree snapshots and 60-second polling.
- [ ] Persist checkpoints and restart without duplicate task ownership.

### Task 4: Exploration, replay, and repair queues

**Files:**
- Create: `scripts/continuous-testing/exploration.py`
- Create: `scripts/continuous-testing/repair.py`
- Create: `scripts/continuous-testing/tests/test_exploration.py`

- [ ] Add semantic scenario deduplication and failure evidence records.
- [ ] Add replay by failure id and repair adapter boundary.
- [ ] Keep unresolved repairs queued without changing main branches.

### Task 5: Operational documentation and Windows startup

**Files:**
- Create: `scripts/continuous-testing/README.md`
- Create: `scripts/continuous-testing/install-task.ps1`
- Modify: `README.md`

- [ ] Document lifecycle, state layout, resource limits, and recovery.
- [ ] Provide Task Scheduler installation/removal commands.
- [ ] Verify a clean-machine dry run and restore rehearsal.

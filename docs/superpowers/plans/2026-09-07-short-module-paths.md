# Short Module Paths Implementation Plan (superseded)

> This historical plan records the concise-path layout that was later replaced by the artifactId-matching layout. Current module paths use the Maven artifactId as the leaf directory name.

> **For agentic workers:** REQUIRED SUB-SKILL: Use `subagent-driven-development` (recommended) or `executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace redundant filesystem module names with concise role names while preserving Java package names and Maven coordinates.

**Architecture:** The root POM continues to aggregate every leaf module directly. Classification folders remain non-Maven folders; only the filesystem paths change. Maven `groupId`, `artifactId`, Java packages, resource classpaths, and module dependencies remain unchanged.

**Tech Stack:** Maven multi-module build, Java 21, Python and PowerShell maintenance scripts.

**Spec:** `docs/superpowers/plans/2026-09-06-module-layout.md` and the approved target directory structure in the Codex conversation.

## Global Constraints

- Preserve every Maven artifact coordinate and Java package name.
- Do not add POM files to classification folders.
- Update every tracked filesystem-path reference.
- Keep runtime data ignored and move only ignored local runtime directories.
- Verify the root reactor, architecture checks, documentation checks, and fresh packaged startup.

---

### Task 1: Rename leaf module directories

**Files:**
- Move: `modules/application/shiyu-ai-bootstrap` to `modules/applications/bootstrap`
- Move: `modules/application/shiyu-ai-web` to `modules/applications/web`
- Move: `modules/application/shiyu-application` to `modules/applications/composition`
- Move: each `modules/domains/<domain>/shiyu-<domain>-contract` to `modules/domains/<domain>/contract`
- Move: each `modules/domains/<domain>/shiyu-<domain>-implementation` to `modules/domains/<domain>/implementation`
- Move: `modules/infrastructure/shiyu-common-*` to `modules/infrastructure/<role>`
- Move: `modules/shared/shiyu-shared-kernel` to `modules/shared/kernel`
- Move: `tests/shiyu-architecture-tests` to `tests/architecture`

**Interfaces:**
- Produces: the concise module filesystem paths used by the root reactor and tools.

- [x] Move only verified existing directories to their exact target locations.
- [x] Preserve each leaf module's `pom.xml` and its relative root-parent path.
- [x] Verify there are no leaf module directories with the old `shiyu-` filesystem prefix.

### Task 2: Rewrite tracked path references

**Files:**
- Modify: `pom.xml`
- Modify: `scripts/**/*.py`, `scripts/**/*.ps1`, `scripts/**/*.sh`
- Modify: `docs/**/*.md`, `.github/**/*.yml`
- Modify: source and test files containing filesystem-path assertions

**Interfaces:**
- Consumes: concise paths from Task 1.
- Produces: Maven reactor, scripts, checks, and documentation resolving every module.

- [x] Replace every old filesystem path with the matching concise path.
- [x] Keep Maven artifact IDs and Java import/package names unchanged.
- [x] Search for stale old paths and inspect intentional historical references before removing them.

### Task 3: Relocate ignored runtime data

**Files:**
- Move: ignored `data/` to `runtime/dev/data/`
- Move: ignored `runtime-smoke-*` directories to `runtime/integration/`
- Modify: `.gitignore`

**Interfaces:**
- Produces: a single ignored `runtime/` root for local state.

- [x] Verify all runtime paths are ignored before moving them.
- [x] Move only explicitly named local runtime directories.
- [x] Update ignore rules to cover `runtime/` without ignoring source files.

### Task 4: Verify and commit

**Files:**
- Verify: root Maven reactor, architecture and documentation scripts, fresh packaged startup

- [x] Run `mvn --batch-mode --no-transfer-progress verify -Ddependency-check.skip=true`.
- [x] Run `python scripts/architecture/check_domain_coverage.py`.
- [x] Run `python scripts/docs/verify_documentation.py`.
- [x] Build the bootstrap package and run `python scripts/verify_fresh_startup.py` against the new path.
- [x] Review `git diff --check`, verify the final directory tree, and commit the migration.

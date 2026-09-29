# Replace NVD Dependency Scan with OSV-Scanner Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the backend CI job that hard-fails without `NVD_API_KEY` with an OSV-Scanner dependency scan that requires no repository secret.

**Architecture:** Keep dependency scanning as a separate blocking job in the backend quality workflow. Remove the unused NVD environment and Maven Dependency-Check invocation, then call the official Google OSV-Scanner reusable workflow pinned to the immutable `v2.6.0` commit. The scanner will recursively inspect the repository's supported Maven and pnpm manifests without attempting to resolve private reactor artifacts, and publish SARIF results through GitHub code scanning permissions.

**Tech Stack:** GitHub Actions, Google OSV-Scanner Action v2.6.0, Maven multi-module project, Python documentation verifier.

**Spec:** User request in chat: “使用其他漏洞扫描器”.

## Global Constraints

- Do not store or expose any API key in source, workflow YAML, or chat.
- Preserve the backend quality gate as blocking when OSV-Scanner reports a vulnerability.
- Keep the scan separate from the compile/test job so failures remain attributable.
- Pin the reusable workflow to commit `a345acffa64b0eaede81a3d9aae6141214d9c8fc` (`v2.6.0`).
- Use `--no-resolve` so private Maven reactor modules do not create false scan failures; keep nested pnpm manifests in scope so real frontend dependency vulnerabilities are visible.
- Verify supported Maven manifest scanning and document the remaining coverage trade-off: OSV uses OSV.dev/deps.dev data and is not an NVD result-equivalent.

### Task 1: Replace the backend dependency-scan workflow job

**Files:**
- Modify: `.github/workflows/ci.yml:14-15,99-141`

**Interfaces:**
- Consumes: Backend repository contents and Maven `pom.xml` manifests.
- Produces: A blocking `OSV 依赖漏洞扫描` job with SARIF upload and no `NVD_API_KEY` dependency.

- [x] **Step 1: Remove the unused NVD environment declaration**

Delete the top-level `NVD_API_KEY` environment mapping so the workflow no longer implies that the secret is required.

- [x] **Step 2: Replace the NVD job with the pinned OSV reusable workflow**

Use this job shape:

```yaml
  dependency-scan:
    name: OSV 依赖漏洞扫描
    permissions:
      actions: read
      contents: read
      security-events: write
    uses: google/osv-scanner-action/.github/workflows/osv-scanner-reusable.yml@a345acffa64b0eaede81a3d9aae6141214d9c8fc # v2.6.0
    with:
      fail-on-vuln: true
      scan-args: |-
        --recursive
        --no-resolve
        ./
```

- [x] **Step 3: Remove all NVD-specific steps and options**

Remove the Java setup/cache/key validation/Maven Dependency-Check steps from the replaced job. Keep the compile job's existing `-Ddependency-check.skip=true` flags because they only disable the old Maven plugin during compilation and do not affect OSV scanning.

### Task 2: Validate the workflow and project documentation

**Files:**
- Modify: `.github/workflows/ci.yml` only if validation finds a syntax or permission error.
- Test: `docs/质量门禁循环记录.md` and generated documentation checks.

**Interfaces:**
- Consumes: Workflow YAML and repository documentation.
- Produces: Locally validated YAML and a clear record of the scanner policy change.

- [x] **Step 1: Confirm no NVD gate references remain in the workflow**

Run:

```powershell
rg -n "NVD_API_KEY|dependency-check|OWASP NVD" .github/workflows/ci.yml
```

Expected: no matches in the dependency-scan job; existing compile-time skip flags may remain only if intentionally documented.

- [x] **Step 2: Run repository documentation verification**

Run:

```powershell
python scripts/docs/verify_documentation.py
git diff --check
```

Expected: documentation verification passes and the diff has no whitespace errors.

- [ ] **Step 3: Verify the remote workflow**

Push the workflow change and inspect the resulting backend run. Expected results: the OSV job starts without `NVD_API_KEY`, the compile/test job remains unchanged, and any vulnerability result is reported by OSV rather than an NVD-key preflight failure.

### Task 3: Commit and hand off the external verification

**Files:**
- Commit: `.github/workflows/ci.yml` and this plan.

- [x] **Step 1: Review the final diff**

Confirm that no secrets, unrelated business code, or frontend behavior changed.

- [x] **Step 2: Commit the scanner migration**

```powershell
git add .github/workflows/ci.yml docs/superpowers/plans/2026-09-29-replace-nvd-with-osv-scanner.md
git commit -m "ci: replace NVD scan with OSV scanner"
```

- [ ] **Step 3: Push and inspect CI**

Push `master`, record the workflow run ID and conclusion, and leave any OSV findings as the next actionable vulnerability-remediation item.

"""Repository baseline gate definitions."""
from __future__ import annotations
from pathlib import Path

def backend_gate(root: Path) -> list[str]:
    return ["mvn", "--batch-mode", "--no-transfer-progress", "-Pstrict-warnings", "verify", "-Ddependency-check.skip=true"]

def frontend_gate(root: Path) -> list[str]:
    return ["pnpm", "test:unit"]

def all_gates(backend: Path, frontend: Path) -> list[tuple[str, Path, list[str]]]:
    return [("backend-gate", backend, backend_gate(backend)), ("frontend-gate", frontend, frontend_gate(frontend))]

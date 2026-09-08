"""Constrained repair adapter; never mutates the main development worktree."""
from __future__ import annotations
import subprocess
from dataclasses import dataclass
from pathlib import Path

@dataclass(frozen=True)
class RepairAttempt:
    failure_id: str
    parent_sha: str
    worktree: Path
    command: tuple[str, ...]
    attempt: int

class RepairPolicy:
    max_attempts = 3
    timeout_seconds = 3600

    @classmethod
    def allowed(cls, attempts: int) -> bool:
        return 0 <= attempts < cls.max_attempts

class RepairAdapter:
    def __init__(self, policy: type[RepairPolicy] = RepairPolicy): self.policy = policy

    def execute(self, request: RepairAttempt) -> subprocess.CompletedProcess[str]:
        if not self.policy.allowed(request.attempt):
            raise RuntimeError("repair attempt limit reached")
        if not request.worktree.is_dir():
            raise FileNotFoundError(request.worktree)
        return subprocess.run(list(request.command), cwd=request.worktree, text=True,
                              capture_output=True, timeout=self.policy.timeout_seconds,
                              check=False)

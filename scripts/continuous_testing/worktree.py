"""Immutable worktree snapshots and stable-change detection."""
from __future__ import annotations
import hashlib, subprocess
from pathlib import Path

def repository_fingerprint(root: Path) -> str:
    head = subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=root, text=True).strip()
    status = subprocess.check_output(["git", "status", "--porcelain=v1", "--untracked-files=all"], cwd=root, text=True)
    return hashlib.sha256((head + "\0" + status).encode()).hexdigest()

def create_worktree(root: Path, target: Path) -> Path:
    target.parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(["git", "worktree", "add", "--detach", str(target), "HEAD"], cwd=root, check=True, text=True)
    return target

def remove_worktree(root: Path, target: Path) -> None:
    subprocess.run(["git", "worktree", "remove", "--force", str(target)], cwd=root, check=True, text=True)

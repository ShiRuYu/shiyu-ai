"""snapshot 脚本，执行项目架构与工程校验。"""
from __future__ import annotations
import hashlib, subprocess
from pathlib import Path

class Snapshot:
    @staticmethod
    def _git(root: Path, *args: str) -> str:
        return subprocess.check_output(["git", *args], cwd=root, text=True).strip()

    @classmethod
    def capture(cls, backend: Path, frontend: Path) -> dict:
        result = {}
        for name, root in (("backend", backend), ("frontend", frontend)):
            sha = cls._git(root, "rev-parse", "HEAD")
            status = cls._git(root, "status", "--porcelain=v1")
            result[f"{name}_sha"] = sha
            result[f"{name}_dirty"] = bool(status)
            result[f"{name}_tree_hash"] = hashlib.sha256(status.encode()).hexdigest()
        return result

"""retention 脚本，执行项目架构与工程校验。"""
from __future__ import annotations
import time
from pathlib import Path

def prune(root: Path, now: float | None = None, success_days: int = 7, failure_days: int = 30) -> list[Path]:
    now = time.time() if now is None else now; removed = []
    rules = [(root / "runs", success_days), (root / "reports", success_days), (root / "failures", failure_days)]
    for directory, days in rules:
        if not directory.exists(): continue
        cutoff = now - days * 86400
        for path in directory.iterdir():
            if path.is_file() and path.stat().st_mtime < cutoff:
                path.unlink(); removed.append(path)
    return removed

"""report 脚本，执行项目架构与工程校验。"""
from __future__ import annotations
import json
from collections import Counter
from pathlib import Path
from .store import StateStore

STATUSES = ("PASS", "SKIPPED", "BLOCKED", "TIMEOUT", "INFRA_FAILURE", "FAIL")

def build_report(store: StateStore, run_id: str) -> dict:
    run = store.get_run(run_id); tasks = store.list_tasks(run_id)
    counts = Counter(task["status"] for task in tasks)
    for status in STATUSES: counts.setdefault(status, 0)
    return {"run_id": run_id, "version": run["version"], "status": run["status"],
            "executed": len(tasks), "outcomes": dict(counts),
            "next_target": "explore uncovered semantic combinations",
            "statement": "本周期已验证这些场景，发现这些问题；尚有这些缺口，下一轮继续探索这些状态。"}

def write_report(store: StateStore, run_id: str, target: Path) -> dict:
    report = build_report(store, run_id); target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    return report

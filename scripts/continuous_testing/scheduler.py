"""scheduler 脚本，执行项目架构与工程校验。"""
from __future__ import annotations
from pathlib import Path
from .gates import all_gates
from .runner import run
from .snapshot import Snapshot
from .store import StateStore
from .resources import Limits, admission

def run_baseline(backend: Path, frontend: Path, state_dir: Path, timeout: int = 3600, limits: Limits = Limits()) -> str:
    state = StateStore(state_dir / "state.sqlite"); state.initialize()
    admitted, reason = admission(state_dir.parent, 0, limits)
    metadata = Snapshot.capture(backend, frontend)
    run_id = state.create_run("baseline", metadata); state.update_run(run_id, "RUNNING")
    if not admitted:
        state.update_run(run_id, "PAUSED")
        state.write_snapshot(state_dir / "state.json")
        (state_dir / "pause-reason.txt").write_text(reason, encoding="utf-8")
        return run_id
    outcomes = []
    for kind, root, command in all_gates(backend, frontend):
        task_id = state.add_task(run_id, kind, command, "RUNNING")
        result = run(command, str(root), timeout_seconds=timeout)
        with state._connect() as db:
            db.execute("UPDATE tasks SET status=?, finished_at=?, exit_code=?, error=? WHERE id=?",
                       (result.status, __import__('datetime').datetime.now(__import__('datetime').timezone.utc).isoformat(), result.exit_code, result.output[-4000:], task_id))
        outcomes.append(result.status)
    state.update_run(run_id, "COMPLETED" if outcomes and all(status == "PASS" for status in outcomes) else "FAILED")
    state.write_snapshot(state_dir / "state.json")
    return run_id

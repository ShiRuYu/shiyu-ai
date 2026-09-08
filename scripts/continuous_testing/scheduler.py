"""Single scheduler tick; safe to invoke repeatedly after process restart."""
from __future__ import annotations
from pathlib import Path
from .gates import all_gates
from .runner import run
from .snapshot import Snapshot
from .store import StateStore

def run_baseline(backend: Path, frontend: Path, state_dir: Path, timeout: int = 3600) -> str:
    state = StateStore(state_dir / "state.sqlite"); state.initialize()
    metadata = Snapshot.capture(backend, frontend)
    run_id = state.create_run("baseline", metadata); state.update_run(run_id, "RUNNING")
    for kind, root, command in all_gates(backend, frontend):
        task_id = state.add_task(run_id, kind, command, "RUNNING")
        result = run(command, str(root), timeout_seconds=timeout)
        with state._connect() as db:
            db.execute("UPDATE tasks SET status=?, finished_at=?, exit_code=?, error=? WHERE id=?",
                       (result.status, __import__('datetime').datetime.now(__import__('datetime').timezone.utc).isoformat(), result.exit_code, result.output[-4000:], task_id))
    state.update_run(run_id, "COMPLETED")
    state.write_snapshot(state_dir / "state.json")
    return run_id

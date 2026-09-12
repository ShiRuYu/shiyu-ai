"""store 脚本，执行项目架构与工程校验。"""
from __future__ import annotations

import json
import sqlite3
import uuid
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Iterable

RUN_STATUSES = {"CREATED", "RUNNING", "PAUSED", "STOPPED", "FAILED", "COMPLETED"}
TASK_STATUSES = {"QUEUED", "RUNNING", "PASS", "FAIL", "SKIPPED", "BLOCKED", "TIMEOUT", "INFRA_FAILURE"}

def _now() -> str:
    return datetime.now(timezone.utc).isoformat()

class StateStore:
    def __init__(self, path: Path):
        self.path = Path(path)

    def _connect(self) -> sqlite3.Connection:
        self.path.parent.mkdir(parents=True, exist_ok=True)
        conn = sqlite3.connect(self.path)
        conn.row_factory = sqlite3.Row
        conn.execute("PRAGMA journal_mode=WAL")
        return conn

    def initialize(self) -> None:
        with self._connect() as db:
            db.executescript("""
            CREATE TABLE IF NOT EXISTS runs (
              id TEXT PRIMARY KEY, version TEXT NOT NULL, status TEXT NOT NULL,
              metadata TEXT NOT NULL, created_at TEXT NOT NULL, updated_at TEXT NOT NULL
            );
            CREATE TABLE IF NOT EXISTS tasks (
              id TEXT PRIMARY KEY, run_id TEXT NOT NULL REFERENCES runs(id), kind TEXT NOT NULL,
              command TEXT NOT NULL, status TEXT NOT NULL, started_at TEXT, finished_at TEXT,
              exit_code INTEGER, output_path TEXT, error TEXT
            );
            CREATE TABLE IF NOT EXISTS failures (
              id TEXT PRIMARY KEY, task_id TEXT NOT NULL REFERENCES tasks(id), signature TEXT NOT NULL,
              evidence_path TEXT NOT NULL, created_at TEXT NOT NULL
            );
            """)

    def create_run(self, version: str, metadata: dict[str, Any]) -> str:
        run_id = uuid.uuid4().hex
        now = _now()
        with self._connect() as db:
            db.execute("INSERT INTO runs VALUES (?, ?, ?, ?, ?, ?)",
                       (run_id, version, "CREATED", json.dumps(metadata), now, now))
        return run_id

    def update_run(self, run_id: str, status: str, **fields: Any) -> None:
        if status not in RUN_STATUSES:
            raise ValueError(f"invalid run status: {status}")
        with self._connect() as db:
            if not db.execute("SELECT 1 FROM runs WHERE id=?", (run_id,)).fetchone():
                raise KeyError(run_id)
            db.execute("UPDATE runs SET status=?, updated_at=? WHERE id=?", (status, _now(), run_id))

    @staticmethod
    def _run(row: sqlite3.Row) -> dict[str, Any]:
        result = dict(row)
        result["metadata"] = json.loads(result["metadata"])
        return result

    def get_run(self, run_id: str) -> dict[str, Any]:
        with self._connect() as db:
            row = db.execute("SELECT * FROM runs WHERE id=?", (run_id,)).fetchone()
        if row is None:
            raise KeyError(run_id)
        return self._run(row)

    def list_runs(self) -> list[dict[str, Any]]:
        with self._connect() as db:
            return [self._run(r) for r in db.execute("SELECT * FROM runs ORDER BY created_at DESC")]

    def add_task(self, run_id: str, kind: str, command: Iterable[str], status: str) -> str:
        if status not in TASK_STATUSES:
            raise ValueError(f"invalid task status: {status}")
        task_id = uuid.uuid4().hex
        with self._connect() as db:
            db.execute("INSERT INTO tasks(id,run_id,kind,command,status) VALUES(?,?,?,?,?)",
                       (task_id, run_id, kind, json.dumps(list(command)), status))
        return task_id

    def list_tasks(self, run_id: str | None = None) -> list[dict[str, Any]]:
        with self._connect() as db:
            rows = db.execute("SELECT * FROM tasks WHERE run_id=? ORDER BY rowid", (run_id,)) if run_id else db.execute("SELECT * FROM tasks ORDER BY rowid")
            result = []
            for row in rows:
                item = dict(row)
                item["command"] = json.loads(item["command"])
                result.append(item)
            return result

    def update_task(self, task_id: str, status: str, **fields: Any) -> None:
        if status not in TASK_STATUSES:
            raise ValueError(f"invalid task status: {status}")
        allowed = {k: v for k, v in fields.items() if k in {"started_at", "finished_at", "exit_code", "output_path", "error"}}
        sets = ["status=?"]; values: list[Any] = [status]
        for key, value in allowed.items(): sets.append(f"{key}=?"); values.append(value)
        values.append(task_id)
        with self._connect() as db:
            if not db.execute("SELECT 1 FROM tasks WHERE id=?", (task_id,)).fetchone(): raise KeyError(task_id)
            db.execute(f"UPDATE tasks SET {', '.join(sets)} WHERE id=?", values)

    def add_failure(self, task_id: str, signature: str, evidence_path: str) -> str:
        failure_id = uuid.uuid4().hex
        with self._connect() as db:
            db.execute("INSERT INTO failures VALUES (?, ?, ?, ?, ?)",
                       (failure_id, task_id, signature, evidence_path, _now()))
        return failure_id

    def get_failure(self, failure_id: str) -> dict[str, Any]:
        with self._connect() as db:
            row = db.execute("SELECT * FROM failures WHERE id=?", (failure_id,)).fetchone()
        if row is None:
            raise KeyError(failure_id)
        return dict(row)

    def write_snapshot(self, target: Path) -> None:
        payload = {"runs": self.list_runs(), "tasks": self.list_tasks()}
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")

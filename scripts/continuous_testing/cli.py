"""Command line lifecycle controller for continuous testing."""
from __future__ import annotations
import argparse, json, os, platform, sys
from datetime import datetime, timezone
from pathlib import Path
from .store import StateStore
from .snapshot import Snapshot
from .report import build_report
from .process_control import stop_owned
from .queue import VersionQueue
from .gates import all_gates
from .runner import run

def _root() -> Path:
    return Path(os.environ.get("SHIYU_TESTING_ROOT", Path.cwd() / ".testing"))

def main(argv=None) -> int:
    parser = argparse.ArgumentParser(prog="continuous-testing")
    parser.add_argument("command", choices=["start", "status", "pause", "resume", "stop", "replay", "run-baseline"])
    parser.add_argument("failure_id", nargs="?")
    args = parser.parse_args(argv)
    root = _root(); store = StateStore(root / "state.sqlite"); store.initialize()
    if args.command == "start":
        run_id = store.create_run("foundation", {"platform": platform.platform(), "pid": os.getpid()})
        store.update_run(run_id, "RUNNING")
        store.write_snapshot(root / "state.json")
        print(json.dumps({"run_id": run_id, "status": "RUNNING"}))
    elif args.command == "replay":
        if not args.failure_id:
            parser.error("replay requires failure-id")
        try:
            print(json.dumps(store.get_failure(args.failure_id)))
        except KeyError:
            print(json.dumps({"failure_id": args.failure_id, "status": "BLOCKED", "error": "failure not found"}))
            return 1
    elif args.command == "status":
        store.write_snapshot(root / "state.json")
        payload = {"runs": store.list_runs(), "tasks": store.list_tasks()}
        for name in ("pending-version.json", "running-version.json"):
            file = root / name
            if file.exists(): payload[name.removesuffix(".json")] = json.loads(file.read_text(encoding="utf-8"))
        if payload["runs"]:
            payload["latest_report"] = build_report(store, payload["runs"][0]["id"])
        print(json.dumps(payload, ensure_ascii=False, indent=2))
    elif args.command == "run-baseline":
        item = VersionQueue(root).claim()
        if not item:
            print(json.dumps({"status": "IDLE"})); return 0
        version = item["version"]; run_id = store.create_run(version["backend_sha"], {"version": version, "purpose": "CHANGE"}); store.update_run(run_id, "RUNNING")
        backend = Path(os.environ.get("SHIYU_BACKEND_ROOT", Path.cwd())); frontend = Path(os.environ.get("SHIYU_FRONTEND_ROOT", backend.parent / "shiyu-ui")); reports = root / "runs" / run_id; reports.mkdir(parents=True, exist_ok=True)
        results = []
        for kind, cwd, command in all_gates(backend, frontend):
            task = store.add_task(run_id, kind, command, "RUNNING"); started = datetime.now(timezone.utc).isoformat()
            result = run(command, str(cwd), 3600); output = reports / f"{task}.log"; output.write_text(result.output, encoding="utf-8", errors="replace")
            store.update_task(task, result.status, started_at=started, finished_at=datetime.now(timezone.utc).isoformat(), exit_code=result.exit_code, output_path=str(output)); results.append({"task_id": task, "kind": kind, "status": result.status})
        final = "COMPLETED" if all(r["status"] == "PASS" for r in results) else "FAILED"; store.update_run(run_id, final); VersionQueue(root).complete(final); store.write_snapshot(root / "state.json"); print(json.dumps({"run_id": run_id, "status": final, "results": results}))
    else:
        runs = store.list_runs()
        if not runs:
            print("no runs")
            return 0
        status = {"pause": "PAUSED", "resume": "RUNNING", "stop": "STOPPED"}[args.command]
        stopped = stop_owned(root / "scheduler.lock") if args.command == "stop" else False
        store.update_run(runs[0]["id"], status)
        store.write_snapshot(root / "state.json")
        print(json.dumps({"run_id": runs[0]["id"], "status": status, "process_stop_requested": stopped}))
    return 0

if __name__ == "__main__":
    sys.exit(main())

"""Command line lifecycle controller for continuous testing."""
from __future__ import annotations
import argparse, json, os, platform, sys
from pathlib import Path
from .store import StateStore
from .snapshot import Snapshot
from .report import build_report
from .process_control import stop_owned

def _root() -> Path:
    return Path(os.environ.get("SHIYU_TESTING_ROOT", Path.cwd() / ".testing"))

def main(argv=None) -> int:
    parser = argparse.ArgumentParser(prog="continuous-testing")
    parser.add_argument("command", choices=["start", "status", "pause", "resume", "stop", "replay"])
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

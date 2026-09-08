"""Command line lifecycle controller for continuous testing."""
from __future__ import annotations
import argparse, json, os, platform, sys
from pathlib import Path
from .store import StateStore
from .snapshot import Snapshot

def _root() -> Path:
    return Path(os.environ.get("SHIYU_TESTING_ROOT", Path.cwd() / ".testing"))

def main(argv=None) -> int:
    parser = argparse.ArgumentParser(prog="continuous-testing")
    parser.add_argument("command", choices=["start", "status", "pause", "resume", "stop"])
    args = parser.parse_args(argv)
    root = _root(); store = StateStore(root / "state.sqlite"); store.initialize()
    if args.command == "start":
        run_id = store.create_run("foundation", {"platform": platform.platform(), "pid": os.getpid()})
        store.update_run(run_id, "RUNNING")
        store.write_snapshot(root / "state.json")
        print(json.dumps({"run_id": run_id, "status": "RUNNING"}))
    elif args.command == "status":
        store.write_snapshot(root / "state.json")
        print(json.dumps({"runs": store.list_runs(), "tasks": store.list_tasks()}, ensure_ascii=False, indent=2))
    else:
        runs = store.list_runs()
        if not runs:
            print("no runs")
            return 0
        status = {"pause": "PAUSED", "resume": "RUNNING", "stop": "STOPPED"}[args.command]
        store.update_run(runs[0]["id"], status)
        store.write_snapshot(root / "state.json")
        print(json.dumps({"run_id": runs[0]["id"], "status": status}))
    return 0

if __name__ == "__main__":
    sys.exit(main())

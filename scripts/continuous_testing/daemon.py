"""Restart-safe polling daemon for the continuous testing controller."""
from __future__ import annotations
import json, os, signal, sys, subprocess, time
from pathlib import Path
from .snapshot import Snapshot
from .store import StateStore
from .lease import Lease
from .exploration import ScenarioLedger, generate_batch

class StableChangeDetector:
    def __init__(self, stable_seconds: int = 30, clock=time.monotonic):
        self.stable_seconds = stable_seconds; self.clock = clock; self.fingerprint = None; self.started = None
    def observe(self, fingerprint: str) -> bool:
        now = self.clock()
        if fingerprint != self.fingerprint:
            self.fingerprint, self.started = fingerprint, now
            return False
        return self.started is not None and now - self.started >= self.stable_seconds

class Daemon:
    def __init__(self, backend: Path, frontend: Path, state_dir: Path, interval: int = 60, auto_run: bool = False):
        self.backend, self.frontend, self.state_dir = backend, frontend, state_dir
        self.interval = interval; self.stop_requested = False
        self.state = StateStore(state_dir / "state.sqlite"); self.state.initialize(); self.detector = StableChangeDetector()
        self.exploration_ticks = 0
        self.auto_run = auto_run
        self.baseline_process = None
        self.baseline_log = None
    def _consume_pending(self) -> None:
        if not self.auto_run:
            return
        if self.baseline_process is not None:
            if self.baseline_process.poll() is None:
                return
            self.baseline_process = None
        pending = self.state_dir / "pending-version.json"
        if not pending.exists() or (self.state_dir / "running-version.json").exists():
            return
        log_dir = self.state_dir / "runs" / "daemon-baseline"
        log_dir.mkdir(parents=True, exist_ok=True)
        log = (log_dir / "launcher.log").open("a", encoding="utf-8")
        self.baseline_log = log
        self.baseline_process = subprocess.Popen(
            [sys.executable, "-m", "scripts.continuous_testing.cli", "run-baseline"],
            cwd=str(self.backend), stdout=log, stderr=subprocess.STDOUT,
            creationflags=getattr(subprocess, "CREATE_NEW_PROCESS_GROUP", 0),
        )
    def stop(self, *_):
        self.stop_requested = True
        if self.baseline_process is not None and self.baseline_process.poll() is None:
            self.baseline_process.terminate()
        if self.baseline_log is not None:
            self.baseline_log.close(); self.baseline_log = None
    def tick(self) -> bool:
        current = Snapshot.capture(self.backend, self.frontend)
        self.state_dir.mkdir(parents=True, exist_ok=True)
        (self.state_dir / "heartbeat.json").write_text(json.dumps({"pid": os.getpid(), "observed": time.time()}), encoding="utf-8")
        self.exploration_ticks += 1
        if self.exploration_ticks % 20 == 0:
            try:
                ledger = ScenarioLedger(self.state_dir / "explored-space.json")
                generate_batch(ledger)
            except RuntimeError as exc:
                (self.state_dir / "pause-reason.txt").write_text(str(exc), encoding="utf-8")
        marker = self.state_dir / "version.json"
        previous = json.loads(marker.read_text()) if marker.exists() else None
        fingerprint = json.dumps(current, sort_keys=True)
        stable = self.detector.observe(fingerprint)
        changed = stable and previous != current
        if changed:
            marker.parent.mkdir(parents=True, exist_ok=True)
            marker.write_text(json.dumps(current, indent=2), encoding="utf-8")
            pending = marker.with_name("pending-version.json")
            # A queued snapshot has not started and may be replaced by the
            # newest stable snapshot. Once execution claims it, the running
            # snapshot is immutable so long-running work never changes version.
            running = marker.with_name("running-version.json")
            if not running.exists():
                temp = pending.with_suffix(".tmp")
                temp.write_text(json.dumps({"status": "QUEUED", "version": current}, indent=2), encoding="utf-8")
                os.replace(temp, pending)
        self._consume_pending()
        return changed
    def run(self) -> None:
        signal.signal(signal.SIGINT, self.stop); signal.signal(signal.SIGTERM, self.stop)
        with Lease(self.state_dir / "scheduler.lock"):
            try:
                while not self.stop_requested:
                    self.tick(); time.sleep(self.interval)
            finally:
                self.stop()

def main() -> int:
    root = Path(os.environ.get("SHIYU_BACKEND_ROOT", Path.cwd()))
    frontend = Path(os.environ.get("SHIYU_FRONTEND_ROOT", root.parent / "shiyu-ui"))
    Daemon(root, frontend, root / ".testing", auto_run=True).run(); return 0

if __name__ == "__main__": raise SystemExit(main())

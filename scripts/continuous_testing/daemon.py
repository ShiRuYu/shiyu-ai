"""Restart-safe polling daemon for the continuous testing controller."""
from __future__ import annotations
import json, os, signal, time
from pathlib import Path
from .snapshot import Snapshot
from .store import StateStore
from .lease import Lease

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
    def __init__(self, backend: Path, frontend: Path, state_dir: Path, interval: int = 60):
        self.backend, self.frontend, self.state_dir = backend, frontend, state_dir
        self.interval = interval; self.stop_requested = False
        self.state = StateStore(state_dir / "state.sqlite"); self.state.initialize(); self.detector = StableChangeDetector()
    def stop(self, *_): self.stop_requested = True
    def tick(self) -> bool:
        current = Snapshot.capture(self.backend, self.frontend)
        self.state_dir.mkdir(parents=True, exist_ok=True)
        (self.state_dir / "heartbeat.json").write_text(json.dumps({"pid": os.getpid(), "observed": time.time()}), encoding="utf-8")
        marker = self.state_dir / "version.json"
        previous = json.loads(marker.read_text()) if marker.exists() else None
        fingerprint = json.dumps(current, sort_keys=True)
        stable = self.detector.observe(fingerprint)
        changed = stable and previous != current
        if changed:
            marker.parent.mkdir(parents=True, exist_ok=True)
            marker.write_text(json.dumps(current, indent=2), encoding="utf-8")
            pending = marker.with_name("pending-version.json")
            pending.write_text(json.dumps({"status": "QUEUED", "version": current}, indent=2), encoding="utf-8")
        return changed
    def run(self) -> None:
        signal.signal(signal.SIGINT, self.stop); signal.signal(signal.SIGTERM, self.stop)
        with Lease(self.state_dir / "scheduler.lock"):
            while not self.stop_requested:
                self.tick(); time.sleep(self.interval)

def main() -> int:
    root = Path(os.environ.get("SHIYU_BACKEND_ROOT", Path.cwd()))
    frontend = Path(os.environ.get("SHIYU_FRONTEND_ROOT", root.parent / "shiyu-ui"))
    Daemon(root, frontend, root / ".testing").run(); return 0

if __name__ == "__main__": raise SystemExit(main())

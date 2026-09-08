"""Process lease preventing duplicate schedulers."""
from __future__ import annotations
import os, json
from pathlib import Path

class Lease:
    def __init__(self, path: Path): self.path = Path(path); self.held = False
    def acquire(self) -> bool:
        self.path.parent.mkdir(parents=True, exist_ok=True)
        try:
            fd = os.open(self.path, os.O_CREAT | os.O_EXCL | os.O_WRONLY)
            with os.fdopen(fd, "w", encoding="utf-8") as handle: json.dump({"pid": os.getpid()}, handle)
            self.held = True; return True
        except FileExistsError:
            return False
    def release(self) -> None:
        if self.held:
            try: self.path.unlink()
            except FileNotFoundError: pass
            self.held = False
    def __enter__(self):
        if not self.acquire(): raise RuntimeError("scheduler lease already held")
        return self
    def __exit__(self, *_): self.release()

"""Atomic frontend/backend test-version pointer."""
from __future__ import annotations
import json, os
from pathlib import Path
from .snapshot import Snapshot

class VersionPair:
    def __init__(self, backend: Path, frontend: Path, pointer: Path): self.backend, self.frontend, self.pointer = backend, frontend, pointer
    def capture(self) -> dict:
        first = Snapshot.capture(self.backend, self.frontend)
        second = Snapshot.capture(self.backend, self.frontend)
        if first != second: raise RuntimeError("repositories changed during version capture")
        return first
    def promote(self) -> dict:
        version = self.capture(); self.pointer.parent.mkdir(parents=True, exist_ok=True)
        temp = self.pointer.with_suffix(self.pointer.suffix + ".tmp")
        temp.write_text(json.dumps(version, sort_keys=True, indent=2), encoding="utf-8")
        os.replace(temp, self.pointer)
        return version

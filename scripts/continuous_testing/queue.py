"""queue 脚本，执行项目架构与工程校验。"""
from __future__ import annotations
import json, os
from pathlib import Path

class VersionQueue:
    def __init__(self, directory: Path): self.directory = Path(directory); self.pending = self.directory / "pending-version.json"; self.running = self.directory / "running-version.json"
    def claim(self) -> dict | None:
        if self.running.exists():
            try:
                if json.loads(self.running.read_text(encoding="utf-8")).get("status") == "RUNNING":
                    return None
            except (OSError, json.JSONDecodeError):
                return None
        if not self.pending.exists(): return None
        data = json.loads(self.pending.read_text(encoding="utf-8")); data["status"] = "RUNNING"
        temp = self.running.with_suffix(".tmp"); temp.write_text(json.dumps(data, indent=2), encoding="utf-8"); os.replace(temp, self.running)
        self.pending.unlink(missing_ok=True); return data
    def complete(self, status: str = "COMPLETED") -> None:
        if not self.running.exists(): return
        data = json.loads(self.running.read_text(encoding="utf-8")); data["status"] = status
        self.running.write_text(json.dumps(data, indent=2), encoding="utf-8")

"""Conservative shutdown of the scheduler process recorded in its lease."""
from __future__ import annotations
import json, os, signal
from pathlib import Path

def stop_owned(lock_path: Path) -> bool:
    if not lock_path.exists(): return False
    try: pid = int(json.loads(lock_path.read_text(encoding="utf-8"))["pid"])
    except (ValueError, KeyError, json.JSONDecodeError): return False
    if pid == os.getpid(): return False
    try: os.kill(pid, signal.SIGTERM)
    except (ProcessLookupError, PermissionError): return False
    return True

"""Resource admission checks for safe continuous scheduling."""
from __future__ import annotations
import ctypes, shutil
from dataclasses import dataclass
from pathlib import Path

@dataclass(frozen=True)
class Limits:
    max_heavy_tasks: int = 2
    min_free_memory_bytes: int = 6 * 1024**3
    min_free_disk_bytes: int = 20 * 1024**3
    max_runtime_bytes: int = 30 * 1024**3

def available_memory_bytes() -> int:
    class Status(ctypes.Structure):
        _fields_ = [("length", ctypes.c_uint32), ("memory_load", ctypes.c_uint32), ("total", ctypes.c_ulonglong), ("available", ctypes.c_ulonglong), ("total_page", ctypes.c_ulonglong), ("available_page", ctypes.c_ulonglong), ("total_virtual", ctypes.c_ulonglong), ("available_virtual", ctypes.c_ulonglong), ("available_extended", ctypes.c_ulonglong)]
    status = Status(); status.length = ctypes.sizeof(Status)
    try:
        ctypes.windll.kernel32.GlobalMemoryStatusEx(ctypes.byref(status)); return int(status.available)
    except AttributeError:
        return 2**63 - 1

def directory_size(path: Path) -> int:
    return sum(p.stat().st_size for p in path.rglob("*") if p.is_file()) if path.exists() else 0

def admission(root: Path, active_heavy: int, limits: Limits = Limits()) -> tuple[bool, str]:
    if active_heavy >= limits.max_heavy_tasks: return False, "heavy-task-limit"
    if available_memory_bytes() < limits.min_free_memory_bytes: return False, "low-memory"
    if shutil.disk_usage(root).free < limits.min_free_disk_bytes: return False, "low-disk"
    if directory_size(root / ".testing") >= limits.max_runtime_bytes: return False, "runtime-budget"
    return True, "ok"

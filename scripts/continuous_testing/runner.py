"""Owned, bounded subprocess execution."""
from __future__ import annotations
import os, subprocess, time
from dataclasses import dataclass

@dataclass(frozen=True)
class Result:
    status: str
    exit_code: int | None
    output: str
    duration_seconds: float
    pid: int

def run(command: list[str], cwd: str, timeout_seconds: int = 900, env: dict[str, str] | None = None) -> Result:
    started = time.monotonic()
    process = subprocess.Popen(command, cwd=cwd, env=env, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    try:
        output, _ = process.communicate(timeout=timeout_seconds)
        status = "PASS" if process.returncode == 0 else "FAIL"
    except subprocess.TimeoutExpired as exc:
        process.kill()
        output, _ = process.communicate()
        output = (exc.output or "") + output
        status = "TIMEOUT"
    return Result(status, process.returncode, output, time.monotonic() - started, process.pid)

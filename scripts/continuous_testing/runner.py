"""Owned, bounded subprocess execution."""
from __future__ import annotations
import os, shutil, subprocess, time
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
    resolved = list(command)
    executable = shutil.which(resolved[0])
    if executable:
        resolved[0] = executable
    try:
        process = subprocess.Popen(resolved, cwd=cwd, env=env, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    except OSError as exc:
        return Result("INFRA_FAILURE", getattr(exc, "errno", None), str(exc), time.monotonic() - started, 0)
    try:
        output, _ = process.communicate(timeout=timeout_seconds)
        status = "PASS" if process.returncode == 0 else "FAIL"
    except subprocess.TimeoutExpired as exc:
        process.kill()
        output, _ = process.communicate()
        output = (exc.output or "") + output
        status = "TIMEOUT"
    return Result(status, process.returncode, output, time.monotonic() - started, process.pid)

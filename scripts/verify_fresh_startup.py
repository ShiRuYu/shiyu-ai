#!/usr/bin/env python3
"""Start the packaged application against a fresh APP_HOME and probe OpenAPI."""

from __future__ import annotations

import argparse
import os
import shutil
import subprocess
import sys
import tempfile
import time
import urllib.error
import urllib.request
from pathlib import Path


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--jar",
        type=Path,
        default=Path("infrastructure/shiyu-ai-bootstrap/target/shiyu-ai-bootstrap-1.0.0.jar"),
        help="packaged Spring Boot jar",
    )
    parser.add_argument("--port", type=int, default=19090)
    parser.add_argument("--timeout", type=float, default=90.0)
    return parser.parse_args()


def probe(url: str) -> bool:
    try:
        with urllib.request.urlopen(url, timeout=2) as response:
            return response.status == 200
    except (urllib.error.URLError, TimeoutError, OSError):
        return False


def main() -> int:
    args = parse_args()
    jar = args.jar.resolve()
    if not jar.is_file():
        print(f"fresh startup failed: jar not found: {jar}", file=sys.stderr)
        return 2

    app_home = Path(tempfile.mkdtemp(prefix="shiyu-fresh-startup-"))
    env = os.environ.copy()
    env["APP_HOME"] = str(app_home)
    command = [
        "java",
        "-jar",
        str(jar),
        f"--server.port={args.port}",
        "--spring.profiles.active=dev",
    ]
    log_path = app_home / "startup.log"
    log_file = log_path.open("w", encoding="utf-8")
    process = subprocess.Popen(
        command,
        cwd=jar.parent,
        env=env,
        stdout=log_file,
        stderr=subprocess.STDOUT,
        text=True,
    )
    log_file.close()

    def read_log() -> str:
        try:
            return log_path.read_text(encoding="utf-8", errors="replace")[-8000:]
        except OSError:
            return ""

    deadline = time.monotonic() + args.timeout
    try:
        url = f"http://127.0.0.1:{args.port}/v3/api-docs"
        while time.monotonic() < deadline:
            if process.poll() is not None:
                output = read_log()
                print("fresh startup failed: process exited before OpenAPI became ready", file=sys.stderr)
                if output:
                    print(output, file=sys.stderr)
                return 1
            if probe(url):
                data_dir = app_home / "data"
                data_files = [path for path in data_dir.rglob("*") if path.is_file()] if data_dir.exists() else []
                if not data_files:
                    print("fresh startup failed: no files were created under APP_HOME/data", file=sys.stderr)
                    return 1
                print(f"Fresh startup passed: OpenAPI 200, data files={len(data_files)}")
                return 0
            time.sleep(1)

        output = read_log()
        print("fresh startup failed: timed out waiting for OpenAPI", file=sys.stderr)
        if output:
            print(output, file=sys.stderr)
        return 1
    finally:
        if process.poll() is None:
            process.terminate()
            try:
                process.wait(timeout=10)
            except subprocess.TimeoutExpired:
                process.kill()
                process.wait(timeout=5)
        shutil.rmtree(app_home, ignore_errors=True)


if __name__ == "__main__":
    raise SystemExit(main())

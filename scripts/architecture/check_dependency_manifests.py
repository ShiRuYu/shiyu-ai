"""检查 JAR 清单中的 Class-Path 是否指向存在的同目录文件。"""

from __future__ import annotations

import argparse
import sys
import zipfile
from pathlib import Path


def _read_class_path(jar_path: Path) -> list[str]:
    """读取清单并展开 RFC 822 风格的续行。"""
    with zipfile.ZipFile(jar_path) as archive:
        try:
            raw_manifest = archive.read("META-INF/MANIFEST.MF")
        except KeyError:
            return []

    lines: list[str] = []
    for raw_line in raw_manifest.decode("utf-8", errors="replace").splitlines():
        if raw_line.startswith(" ") and lines:
            lines[-1] += raw_line[1:]
        else:
            lines.append(raw_line)

    for line in lines:
        if line.lower().startswith("class-path:"):
            return line.split(":", 1)[1].strip().split()
    return []


def find_missing_entries(jar_path: Path) -> list[tuple[str, Path]]:
    """返回清单中缺失的相对目标及其解析后的路径。"""
    missing: list[tuple[str, Path]] = []
    for entry in _read_class_path(jar_path):
        target = (jar_path.parent / entry).resolve()
        if not target.exists():
            missing.append((entry, target))
    return missing


def run(raw_paths: list[str], allowed_entries: set[str] | None = None) -> int:
    """检查指定 JAR，并返回适合命令行的退出码。"""
    allowed_entries = allowed_entries or set()
    if not raw_paths:
        print("错误：至少需要一个 JAR 文件路径。")
        return 2

    failed = False
    for raw_path in raw_paths:
        jar_path = Path(raw_path).expanduser().resolve()
        if not jar_path.is_file():
            print(f"错误：JAR 文件不存在：{jar_path}")
            failed = True
            continue
        try:
            missing = find_missing_entries(jar_path)
        except (OSError, zipfile.BadZipFile, UnicodeError) as error:
            print(f"错误：无法读取 JAR 清单：{jar_path}（{error}）")
            failed = True
            continue
        for entry, target in missing:
            if entry in allowed_entries:
                print(f"已登记的第三方清单路径：{jar_path} -> {entry}（解析为 {target}）")
            else:
                print(f"清单路径缺失：{jar_path} -> {entry}（解析为 {target}）")
        failed = failed or any(entry not in allowed_entries for entry, _ in missing)

    if not failed:
        print(f"依赖清单预检通过：已检查 {len(raw_paths)} 个 JAR。")
    return 1 if failed else 0


def main() -> int:
    parser = argparse.ArgumentParser(description="检查 JAR 清单中的 Class-Path 目标")
    parser.add_argument(
        "--allow-missing",
        action="append",
        default=[],
        help="显式登记可接受的缺失清单文件名；未登记项仍会失败",
    )
    parser.add_argument("jars", nargs="+", help="待检查的 JAR 文件")
    args = parser.parse_args()
    return run(args.jars, set(args.allow_missing))


if __name__ == "__main__":
    sys.exit(main())

#!/usr/bin/env python3
"""Enforce JaCoCo thresholds independently for every domain implementation.

The aggregate reactor report is intentionally not used here: a highly tested
domain must not hide an untested domain. Run after Maven's ``verify`` phase so
that each implementation module has a fresh ``jacoco.xml`` report.
"""

from __future__ import annotations

import argparse
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def coverage(report: Path, source_root: Path, counter_type: str) -> float:
    """Calculate coverage from authoritative source files only.

    JaCoCo's module counter includes class files that are generated during
    annotation processing (for example MapStruct ``*MapperImpl`` classes).
    Those classes do not have a corresponding file under ``src/main/java``
    and are not part of the non-generated-code contract.  Summing the line
    elements from source files that actually exist keeps the gate deterministic
    and avoids stale generated classes from previous builds.
    """

    root = ET.parse(report).getroot()
    missed = covered = 0
    source_files = 0
    for package in root.findall("package"):
        package_dir = source_root.joinpath(*package.get("name", "").split("/"))
        for source_file in package.findall("sourcefile"):
            candidate = package_dir / source_file.get("name", "")
            if not candidate.is_file():
                continue
            source_files += 1
            for line in source_file.findall("line"):
                if counter_type == "LINE":
                    # JaCoCo's line element exposes instruction counters as
                    # ``mi``/``ci``.  A line is covered when at least one
                    # instruction on that source line ran; summing those
                    # instruction counts would report instruction coverage
                    # while labeling it as line coverage.
                    missed_instructions = int(line.get("mi", "0"))
                    covered_instructions = int(line.get("ci", "0"))
                    # JaCoCo can emit source declarations with no executable
                    # instructions (for example an empty interface).  They
                    # are not coverage obligations and must not become a
                    # synthetic missed line.
                    if missed_instructions + covered_instructions == 0:
                        continue
                    if covered_instructions > 0:
                        covered += 1
                    else:
                        missed += 1
                else:
                    missed += int(line.get("mb", "0"))
                    covered += int(line.get("cb", "0"))
    if source_files == 0:
        raise ValueError("no authoritative source files found")
    total = missed + covered
    return 100.0 if total == 0 else covered * 100.0 / total


def implementation_modules(root: Path) -> list[Path]:
    return sorted(
        module
        for module in (root / "shiyu-domains").glob("*/*-implementation")
        if (module / "src" / "main" / "java").is_dir()
    )


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--line", type=float, default=90.0, help="minimum line coverage percentage")
    parser.add_argument("--branch", type=float, default=80.0, help="minimum branch coverage percentage")
    parser.add_argument("--allow-missing", action="store_true", help="report missing XML without failing")
    args = parser.parse_args()

    failures: list[str] = []
    rows: list[tuple[str, float | None, float | None, str]] = []
    for module in implementation_modules(args.root):
        report = module / "target" / "site" / "jacoco" / "jacoco.xml"
        name = module.relative_to(args.root).as_posix()
        if not report.is_file():
            rows.append((name, None, None, "missing report"))
            if not args.allow_missing:
                failures.append(f"{name}: missing {report}")
            continue
        try:
            source_root = module / "src" / "main" / "java"
            line = coverage(report, source_root, "LINE")
            branch = coverage(report, source_root, "BRANCH")
        except (ET.ParseError, OSError, ValueError) as exc:
            rows.append((name, None, None, f"invalid report: {exc}"))
            failures.append(f"{name}: invalid JaCoCo report ({exc})")
            continue
        status = "ok" if line >= args.line and branch >= args.branch else "below threshold"
        rows.append((name, line, branch, status))
        if status != "ok":
            failures.append(
                f"{name}: line {line:.1f}% (required {args.line:.1f}%), "
                f"branch {branch:.1f}% (required {args.branch:.1f}%)"
            )

    print("domain implementation | line | branch | status")
    print("--- | ---: | ---: | ---")
    for name, line, branch, status in rows:
        line_text = "-" if line is None else f"{line:.1f}%"
        branch_text = "-" if branch is None else f"{branch:.1f}%"
        print(f"{name} | {line_text} | {branch_text} | {status}")

    if failures:
        print("\nDomain coverage gate failed:", file=sys.stderr)
        for failure in failures:
            print(f"- {failure}", file=sys.stderr)
        return 1
    print(f"\nDomain coverage gate passed ({args.line:.1f}% line / {args.branch:.1f}% branch).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

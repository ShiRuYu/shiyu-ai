"""Check that mutable state objects use Lombok accessors."""

from __future__ import annotations

import argparse
import re
import sys
from dataclasses import dataclass
from pathlib import Path


TARGET_SUFFIXES = (
    "Properties",
    "Request",
    "Response",
    "DTO",
    "VO",
    "Options",
    "Settings",
)
EXCLUDED_ANNOTATIONS = {
    "AutoConfiguration",
    "Component",
    "Configuration",
    "Controller",
    "Repository",
    "RestController",
    "Service",
}
CLASS_RE = re.compile(
    r"(?P<prefix>(?:@[A-Za-z_$][\w$]*(?:\([^)]*\))?\s*)*)"
    r"(?:public\s+|protected\s+|private\s+|abstract\s+|final\s+|static\s+)*"
    r"class\s+(?P<name>[A-Za-z_$][\w$]*)"
)


@dataclass(frozen=True)
class Violation:
    path: Path
    line: int
    type_name: str
    reason: str


def _is_target(type_name: str) -> bool:
    return type_name.endswith(TARGET_SUFFIXES)


def _annotations(prefix: str) -> set[str]:
    return set(re.findall(r"@([A-Za-z_$][\w$]*)", prefix))


def scan_file(path: Path, allowlist: set[str] | None = None) -> list[Violation]:
    source = path.read_text(encoding="utf-8")
    allowed = allowlist or set()
    violations: list[Violation] = []
    for match in CLASS_RE.finditer(source):
        type_name = match.group("name")
        if not _is_target(type_name) or type_name in allowed:
            continue
        annotations = _annotations(match.group("prefix"))
        if annotations & EXCLUDED_ANNOTATIONS:
            continue
        if re.search(r"\b(record|enum|interface)\s+" + re.escape(type_name), source):
            continue
        if re.search(r"\bprivate\s+" + re.escape(type_name) + r"\s*\(", source):
            continue
        has_lombok_accessors = bool(
            re.search(r"import\s+lombok\.(?:Data|Value|Getter|Setter);", source)
            or re.search(r"@(?:[A-Za-z_$][\w$]*\.)*(?:Data|Value|Getter|Setter)\b", source)
        )
        if not has_lombok_accessors:
            line = source.count("\n", 0, match.start()) + 1
            violations.append(
                Violation(path, line, type_name, "missing Lombok accessor annotation")
            )
    return violations


def scan_paths(paths: list[Path], allowlist: set[str] | None = None) -> list[Violation]:
    violations: list[Violation] = []
    for root in paths:
        candidates = [root] if root.is_file() else root.rglob("*.java")
        for path in candidates:
            if "target" not in path.parts:
                violations.extend(scan_file(path, allowlist))
    return violations


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("paths", nargs="+", type=Path)
    parser.add_argument("--allowlist-file", type=Path)
    args = parser.parse_args()
    allowlist: set[str] = set()
    if args.allowlist_file and args.allowlist_file.is_file():
        allowlist = {
            line.strip()
            for line in args.allowlist_file.read_text(encoding="utf-8").splitlines()
            if line.strip() and not line.lstrip().startswith("#")
        }
    violations = scan_paths(args.paths, allowlist)
    for item in violations:
        print(f"{item.path}:{item.line}: {item.type_name}: {item.reason}")
    if violations:
        print(f"Lombok state object check failed: {len(violations)} violation(s)")
        return 1
    print("Lombok state object check passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

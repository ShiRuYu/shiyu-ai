"""Reject the retired Workspace term from backend identifiers.

The UI may still expose an AI workbench URL, but backend domain/application
code must describe authorization relationships as scopes.  Java identifiers
are scanned case-insensitively; structured seed identifiers such as navigation
codes are scanned for the PascalCase ``Workspace`` token.  Lowercase transport
URLs such as ``/workspace`` remain valid at the HTTP/UI boundary.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
SOURCE_ROOTS = (
    ROOT / "shiyu-domains",
    ROOT / "application",
    ROOT / "infrastructure",
    ROOT / "shared",
)
IDENTIFIER = re.compile(r"\b[A-Za-z_$][A-Za-z0-9_$]*workspace[A-Za-z0-9_$]*\b", re.IGNORECASE)
SQL_IDENTIFIER = re.compile(r"\b[A-Za-z_$][A-Za-z0-9_$]*Workspace[A-Za-z0-9_$]*\b")


def _without_comments_and_strings(text: str) -> str:
    """Keep identifier text while masking comments and string/char literals."""
    pattern = re.compile(
        r"//[^\n]*|/\*.*?\*/|\"(?:\\.|[^\"\\])*\"|'(?:\\.|[^'\\])*'",
        re.DOTALL,
    )
    return pattern.sub(lambda match: " " * len(match.group(0)), text)


def main() -> int:
    violations: list[tuple[Path, int, str]] = []
    for source_root in SOURCE_ROOTS:
        if not source_root.exists():
            continue
        for source in (*source_root.rglob("*.java"), *source_root.rglob("*.sql")):
            if "target" in source.parts:
                continue
            if "src\\test\\" in str(source) or "/src/test/" in str(source).replace("\\", "/"):
                continue
            raw_text = source.read_text(encoding="utf-8")
            text = _without_comments_and_strings(raw_text) if source.suffix == ".java" else raw_text
            pattern = IDENTIFIER if source.suffix == ".java" else SQL_IDENTIFIER
            for match in pattern.finditer(text):
                violations.append((source, text.count("\n", 0, match.start()) + 1, match.group(0)))

    if violations:
        print(f"Retired Workspace backend identifiers found ({len(violations)}):", file=sys.stderr)
        for source, line, identifier in violations:
            print(f"  {source.relative_to(ROOT)}:{line}: {identifier}", file=sys.stderr)
        return 1

    print("Backend terminology valid: no Workspace identifiers in production backend sources.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

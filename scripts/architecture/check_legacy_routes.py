"""check legacy routes 脚本，执行项目架构与工程校验。"""

from __future__ import annotations

import re
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
WEB_SOURCES = (
    ROOT / "modules/applications" / "web" / "src" / "main" / "java",
    ROOT / "modules/domains",
)
MAPPING = re.compile(
    r"@(RequestMapping|GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)"
    r"\s*\([^)]*['\"]/?v1(?:/|['\"])",
    re.IGNORECASE | re.DOTALL,
)


def main() -> int:
    violations: list[tuple[Path, int]] = []
    for source_root in WEB_SOURCES:
        if not source_root.exists():
            continue
        for source in source_root.rglob("*.java"):
            if "src\\test\\" in str(source) or "/src/test/" in str(source).replace("\\", "/"):
                continue
            if "target" in source.parts:
                continue
            text = source.read_text(encoding="utf-8")
            for match in MAPPING.finditer(text):
                violations.append((source, text.count("\n", 0, match.start()) + 1))

    if violations:
        for source, line in violations:
            print(f"legacy /v1 route: {source.relative_to(ROOT)}:{line}", file=sys.stderr)
        return 1

    print("No legacy /v1 Spring controller routes found.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

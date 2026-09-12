"""Generate an apply_patch patch that moves type Javadocs before annotations."""

from __future__ import annotations

import difflib
import re
from pathlib import Path


TYPE_DECLARATION = re.compile(r"\b(?:class|interface|enum|record|@interface)\b")


def process(path: Path) -> str | None:
    original = path.read_text(encoding="utf-8")
    lines = original.splitlines()
    changes: list[tuple[int, int, int]] = []
    index = 0
    while index < len(lines):
        if "/**" not in lines[index]:
            index += 1
            continue
        start = index
        end = index
        while end < len(lines) and "*/" not in lines[end]:
            end += 1
        if end >= len(lines):
            break
        next_line = end + 1
        while next_line < len(lines) and not lines[next_line].strip():
            next_line += 1
        if next_line >= len(lines) or not TYPE_DECLARATION.search(lines[next_line]):
            index = end + 1
            continue
        cursor = start - 1
        segment_start = start
        while cursor >= 0 and lines[cursor].strip():
            value = lines[cursor].strip()
            if (
                value.endswith("*/")
                or value.endswith(";")
                or (value.endswith("{") and not value.startswith("@"))
                or value == "}"
            ):
                break
            segment_start = cursor
            cursor -= 1
        annotation_lines = [pos for pos in range(segment_start, start) if lines[pos].strip().startswith("@")]
        if annotation_lines:
            changes.append((start, end, min(annotation_lines)))
        index = end + 1

    if not changes:
        return None
    for start, end, annotation_start in sorted(changes, reverse=True):
        doc = lines[start : end + 1]
        del lines[start : end + 1]
        lines[annotation_start:annotation_start] = doc
    updated = "\n".join(lines) + "\n"
    return "".join(
        difflib.unified_diff(
            original.splitlines(True),
            updated.splitlines(True),
            fromfile="a/" + path.as_posix(),
            tofile="b/" + path.as_posix(),
            n=2,
        )
    )


def main() -> int:
    chunks: list[str] = []
    for path in Path("modules").rglob("*.java"):
        if "src/main/java" not in path.as_posix() or "target" in path.parts:
            continue
        diff = process(path)
        if diff:
            chunks.extend(diff.splitlines(True))
    print("*** Begin Patch")
    for line in chunks:
        if line.startswith("--- a/"):
            print("*** Update File: E:/Dev/shiyu/shiyu-ai/" + line[6:].rstrip())
        elif line.startswith(("+++ b/", "\\ No newline")):
            continue
        elif line.startswith("@@"):
            print("@@")
        else:
            print(line.rstrip("\r\n"))
    print("*** End Patch")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

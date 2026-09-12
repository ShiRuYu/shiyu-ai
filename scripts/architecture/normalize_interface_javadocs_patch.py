"""Normalize interface method Javadocs and emit an apply_patch patch."""

from __future__ import annotations

import difflib
import re
import sys
from pathlib import Path

from check_interface_javadocs import first_code_line, interface_methods, mask_java, render_method_javadoc


def javadoc_blocks(lines: list[str]):
    index = 0
    while index < len(lines):
        if "/**" not in lines[index]:
            index += 1
            continue
        end = index
        while end < len(lines) and "*/" not in lines[end]:
            end += 1
        if end < len(lines):
            yield index, end
            index = end + 1
        else:
            return


def next_code_line(lines: list[str], start: int) -> int | None:
    """Find the declaration after a Javadoc, skipping annotation blocks."""

    index = start
    while index < len(lines):
        stripped = lines[index].strip()
        if not stripped:
            index += 1
            continue
        if stripped.startswith("//"):
            index += 1
            continue
        if stripped.startswith("/**"):
            while index < len(lines) and "*/" not in lines[index]:
                index += 1
            index += 1
            continue
        if not stripped.startswith("@"):
            return index
        depth = stripped.count("(") - stripped.count(")")
        index += 1
        while index < len(lines) and depth > 0:
            depth += lines[index].count("(") - lines[index].count(")")
            index += 1
    return None


def normalize(path: Path) -> str | None:
    source = path.read_text(encoding="utf-8")
    lines = source.splitlines()
    remove: set[int] = set()
    for start, end in javadoc_blocks(lines):
        following = next_code_line(lines, end + 1)
        if following is not None and "(" in lines[following] and "=" not in lines[following]:
            remove.update(range(start, end + 1))

    cleaned = [line for index, line in enumerate(lines) if index not in remove]
    cleaned_source = "\n".join(cleaned) + "\n"
    methods = interface_methods(cleaned_source)
    insertions: dict[int, list[str]] = {}
    for method in methods:
        line = first_code_line(cleaned_source, method)
        indent = re.match(r"\s*", cleaned[line]).group(0)
        insertions[line] = render_method_javadoc(
            method.method_name,
            method.return_type,
            method.parameter_names,
            indent,
        ).splitlines()

    updated: list[str] = []
    for index, line in enumerate(cleaned):
        updated.extend(insertions.get(index, []))
        updated.append(line)
    original_text = "\n".join(lines) + "\n"
    updated_text = "\n".join(updated) + "\n"
    diff = list(
        difflib.unified_diff(
            original_text.splitlines(True),
            updated_text.splitlines(True),
            fromfile="a/" + path.as_posix(),
            tofile="b/" + path.as_posix(),
            n=10000,
        )
    )
    if len(diff) < 3:
        return None
    body = ["@@" if line.startswith("@@") else line.rstrip("\n") for line in diff[2:]]
    return "\n".join(
        [
            "*** Begin Patch",
            "*** Update File: " + path.resolve().as_posix(),
            *body,
            "*** End Patch",
            "",
        ]
    )


def main() -> int:
    if len(sys.argv) < 2:
        print("usage: normalize_interface_javadocs_patch.py <java-file>...", file=sys.stderr)
        return 2
    for raw_path in sys.argv[1:]:
        patch = normalize(Path(raw_path))
        if patch:
            print(patch, end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

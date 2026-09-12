"""Generate apply_patch input for missing Java interface method Javadocs."""

from __future__ import annotations

import difflib
import re
import sys
from pathlib import Path

from check_interface_javadocs import has_javadoc, interface_methods, mask_java, render_method_javadoc


def insertion_line(source: str, member_offset: int, declaration_offset: int) -> int:
    """Find the first source line containing code for a member.

    ``member_offset`` may point before section comments or an existing Javadoc
    block.  Looking at the masked source lets us skip those comments while
    still placing a new Javadoc before annotations and the declaration itself.
    """

    masked = mask_java(source)
    member_prefix = masked[member_offset:declaration_offset]
    relative = next(
        (index for index, char in enumerate(member_prefix) if not char.isspace()),
        0,
    )
    code_offset = member_offset + relative
    return source.count("\n", 0, code_offset)


def process(path: Path, method_name_filter: str | None = None) -> str | None:
    source = path.read_text(encoding="utf-8")
    lines = source.splitlines()
    insertions: dict[int, list[str]] = {}
    for method in interface_methods(source):
        if method_name_filter and method.method_name != method_name_filter:
            continue
        if has_javadoc(lines, method.line - 1):
            continue
        line = insertion_line(source, method.member_offset, method.declaration_offset)
        indent = re.match(r"\s*", lines[line]).group(0) if line < len(lines) else "    "
        rendered = render_method_javadoc(
            method.method_name,
            method.return_type,
            method.parameter_names,
            indent,
        )
        insertions.setdefault(line, []).extend(rendered.splitlines())

    if not insertions:
        return None
    updated: list[str] = []
    for index, line in enumerate(lines):
        updated.extend(insertions.get(index, []))
        updated.append(line)
    updated.extend(insertions.get(len(lines), []))
    original_text = "\n".join(lines) + "\n"
    updated_text = "\n".join(updated) + "\n"
    context_lines = 20 if method_name_filter else 3
    diff = list(
        difflib.unified_diff(
            original_text.splitlines(True),
            updated_text.splitlines(True),
            fromfile="a/" + path.as_posix(),
            tofile="b/" + path.as_posix(),
            n=context_lines,
        )
    )
    if len(diff) < 3:
        return None
    body = ["@@" if line.startswith("@@") else line.rstrip("\n") for line in diff[2:]]
    patch_lines = [
        "*** Begin Patch",
        "*** Update File: " + path.resolve().as_posix(),
        *body,
        "*** End Patch",
    ]
    return "\n".join(patch_lines) + "\n"


def main() -> int:
    if len(sys.argv) < 2:
        print("usage: generate_interface_javadocs_patch.py [--method NAME] <java-file>...", file=sys.stderr)
        return 2
    method_name_filter = None
    raw_paths = sys.argv[1:]
    if raw_paths[0] == "--method":
        if len(raw_paths) < 3:
            print("--method requires a name and at least one Java file", file=sys.stderr)
            return 2
        method_name_filter = raw_paths[1]
        raw_paths = raw_paths[2:]
    chunks = []
    for raw_path in raw_paths:
        patch = process(Path(raw_path), method_name_filter)
        if patch:
            chunks.append(patch)
    if chunks:
        print("\n".join(chunks), end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

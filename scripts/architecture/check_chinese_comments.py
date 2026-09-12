"""扫描项目注释，确保接口有中文类型说明且自然语言注释使用中文。"""

from __future__ import annotations

import argparse
import re
import sys
from dataclasses import dataclass
from pathlib import Path


SUPPORTED_SUFFIXES = {
    ".java",
    ".js",
    ".jsx",
    ".py",
    ".ps1",
    ".sh",
    ".sql",
    ".ts",
    ".tsx",
    ".xml",
    ".yaml",
    ".yml",
}
EXCLUDED_PARTS = {
    ".git",
    ".idea",
    ".venv",
    "node_modules",
    "target",
    "dist",
    "build",
    "generated",
    "_validation",
    "backup",
    "backups",
    ".testing",
}
ENGLISH_WORDS = {
    "a",
    "an",
    "and",
    "are",
    "as",
    "check",
    "create",
    "does",
    "for",
    "from",
    "if",
    "in",
    "is",
    "load",
    "must",
    "of",
    "on",
    "only",
    "or",
    "return",
    "save",
    "should",
    "that",
    "the",
    "this",
    "to",
    "used",
    "when",
    "with",
    "will",
}
FORBIDDEN_PREFIXES = ("中文接口说明：", "中文说明：")
JAVADOC_TAG = re.compile(r"^\s*\*?\s*@\w+")
URL = re.compile(r"https?://\S+|\{@(?:code|link)\s+[^}]+}")
WORD = re.compile(r"[A-Za-z]{2,}")
INTERFACE = re.compile(r"\b(?:public\s+|protected\s+|private\s+|abstract\s+|sealed\s+|non-sealed\s+|static\s+)*interface\s+[A-Za-z_$][\w$]*")


@dataclass(frozen=True)
class Violation:
    path: Path
    line: int
    message: str


@dataclass(frozen=True)
class Comment:
    line: int
    text: str


def contains_chinese(text: str) -> bool:
    return bool(re.search(r"[\u3400-\u9fff]", text))


def _comment_lines(text: str, suffix: str) -> list[Comment]:
    comments: list[Comment] = []
    line_hash = suffix in {".py", ".ps1", ".sh", ".yaml", ".yml"}
    line_sql = suffix == ".sql"
    block_markers = ("<!--",) if suffix == ".xml" else (() if line_hash or line_sql else ("/*",))
    block_start: int | None = None
    block_buffer: list[str] = []
    block_marker = "*/"
    for number, line in enumerate(text.splitlines(), start=1):
        current = line
        if block_start is not None:
            block_buffer.append(current)
            if block_marker in current:
                comments.append(Comment(block_start, "\n".join(block_buffer)))
                block_start = None
                block_buffer = []
            continue

        for marker in block_markers:
            position = current.find(marker)
            while position >= 0:
                prefix = current[:position]
                if prefix.count('"') % 2 == 0 and prefix.count("'") % 2 == 0:
                    break
                position = current.find(marker, position + len(marker))
            if position >= 0:
                end_marker = "*/" if marker == "/*" else "-->"
                end_position = current.find(end_marker, position + len(marker))
                if end_position >= 0:
                    comments.append(Comment(number, current[position : end_position + len(end_marker)]))
                else:
                    block_start = number
                    block_marker = end_marker
                    block_buffer = [current[position:]]
                current = current[:position]
                break
        if block_start is not None:
            continue

        stripped = current.lstrip()
        if not line_hash and not line_sql and stripped.startswith("//"):
            comments.append(Comment(number, stripped[2:]))
        elif line_hash and stripped.startswith("#") and not stripped.startswith("#!"):
            comments.append(Comment(number, stripped[1:]))
        elif line_sql and stripped.startswith("--"):
            comments.append(Comment(number, stripped[2:]))
    if block_start is not None:
        comments.append(Comment(block_start, "\n".join(block_buffer)))
    return comments


def _is_natural_english(comment: str) -> bool:
    text = URL.sub(" ", comment)
    text = "\n".join(line for line in text.splitlines() if not JAVADOC_TAG.match(line))
    text = re.sub(r"[{}*`<>/@#_.:=+()\[\]{}\"'\\-]", " ", text)
    words = [word.lower() for word in WORD.findall(text)]
    common = [word for word in words if word in ENGLISH_WORDS]
    if contains_chinese(text):
        return len(common) >= 3
    if re.fullmatch(r"\s*class\s+path\s+.*\s+jar\s*", text, flags=re.IGNORECASE):
        return False
    return len(words) >= 2


def _has_type_javadoc(lines: list[str], declaration_line: int) -> bool:
    index = declaration_line - 1
    annotation_balance = 0
    while index >= 0:
        stripped = lines[index].strip()
        if not stripped:
            index -= 1
            continue
        if annotation_balance > 0:
            annotation_balance += stripped.count(")") - stripped.count("(")
            index -= 1
            continue
        if stripped.startswith("@"):
            annotation_balance = max(0, stripped.count(")") - stripped.count("("))
            index -= 1
            continue
        if stripped.startswith(")") or stripped.startswith("})"):
            annotation_balance = max(1, stripped.count(")") - stripped.count("("))
            index -= 1
            continue
        if stripped.endswith("*/"):
            start = index
            while start >= 0 and "/**" not in lines[start]:
                start -= 1
            if start >= 0:
                return contains_chinese("\n".join(lines[start : index + 1]))
        return False
    return False


def scan_file(path: Path) -> list[Violation]:
    if path.suffix.lower() not in SUPPORTED_SUFFIXES or any(part in EXCLUDED_PARTS for part in path.parts):
        return []
    try:
        text = path.read_text(encoding="utf-8")
    except (OSError, UnicodeDecodeError):
        return []

    violations: list[Violation] = []
    lines = text.splitlines()
    if path.suffix.lower() == ".java":
        for match in INTERFACE.finditer(text):
            line = text.count("\n", 0, match.start()) + 1
            if not _has_type_javadoc(lines, line - 1):
                violations.append(Violation(path, line, "接口缺少中文类型说明"))

    for comment in _comment_lines(text, path.suffix.lower()):
        if any(prefix in comment.text for prefix in FORBIDDEN_PREFIXES):
            violations.append(Violation(path, comment.line, "中文注释不应使用模板前缀"))
        if _is_natural_english(comment.text):
            violations.append(Violation(path, comment.line, "英文自然语言注释"))
    return violations


def iter_files(paths: list[Path]):
    seen: set[Path] = set()
    for root in paths:
        root = root.resolve()
        candidates = [root] if root.is_file() else root.rglob("*")
        for candidate in candidates:
            if not candidate.is_file() or candidate in seen:
                continue
            if any(part in EXCLUDED_PARTS for part in candidate.parts):
                continue
            seen.add(candidate)
            yield candidate


def scan_paths(paths: list[Path]) -> list[Violation]:
    violations: list[Violation] = []
    for path in iter_files(paths):
        violations.extend(scan_file(path))
    return violations


def run(raw_paths: list[str]) -> int:
    violations = scan_paths([Path(path) for path in raw_paths])
    for violation in violations:
        print(f"{violation.path}:{violation.line}: {violation.message}")
    if violations:
        print(f"中文注释门禁失败：发现 {len(violations)} 项问题。")
        return 1
    print("中文注释门禁通过：接口类型和自然语言注释均符合规则。")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description="检查接口 JavaDoc 和中文注释")
    parser.add_argument("paths", nargs="+", help="待扫描的文件或目录")
    return run(parser.parse_args().paths)


if __name__ == "__main__":
    sys.exit(main())

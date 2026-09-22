"""扫描各模块生产源码中 Spring 路由注解的旧版 /v1 路径字面量。"""

from __future__ import annotations

import re
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
MAPPINGS = {"RequestMapping", "GetMapping", "PostMapping", "PutMapping", "DeleteMapping", "PatchMapping"}
TOKENS = re.compile(
    r'(?P<comment>//[^\n]*|/\*[\s\S]*?\*/)'
    r'|(?P<string>"""(?:\\[\s\S]|(?!""")[^\\])*"""|"(?:\\[\s\S]|[^"\\])*")'
    r"|(?P<char>'(?:\\[\s\S]|[^'\\])*')"
    r'|(?P<word>[A-Za-z_$][\w$]*)|(?P<symbol>[^\s])'
)
EXCLUDED = {"target", "build", "generated", "generated-sources", "generated-test-sources", "test", "tests"}


def java_sources(root: Path):
    """枚举所有模块的 Java 生产源码，排除测试、构建和生成目录。"""
    for source in sorted((root / "modules").rglob("*.java")):
        parts = source.relative_to(root / "modules").parts
        if any(part in EXCLUDED for part in parts):
            continue
        if any(parts[index:index + 3] == ("src", "main", "java") for index in range(len(parts) - 3)):
            yield source


def legacy_lines(text: str):
    """返回旧版路由注解所在行，仅检查路径字面量，不解析常量或组合表达式。"""
    tokens = [match for match in TOKENS.finditer(text) if match.lastgroup != "comment"]
    for index, token in enumerate(tokens):
        if token.group() != "@":
            continue
        cursor = index + 1
        names = []
        while cursor < len(tokens) and (tokens[cursor].lastgroup == "word" or tokens[cursor].group() == "."):
            names.append(tokens[cursor].group())
            cursor += 1
        if not names or names[-1] not in MAPPINGS or cursor >= len(tokens) or tokens[cursor].group() != "(":
            continue
        cursor += 1
        depth = 0
        arguments = [[]]
        while cursor < len(tokens):
            value = tokens[cursor].group()
            if value == ")" and depth == 0:
                break
            if value == "," and depth == 0:
                arguments.append([])
            else:
                arguments[-1].append(tokens[cursor])
                if value in ("{", "(", "["):
                    depth += 1
                elif value in ("}", ")", "]"):
                    depth -= 1
            cursor += 1
        for argument in arguments:
            if len(argument) >= 2 and argument[1].group() == "=":
                if argument[0].group() not in ("path", "value"):
                    continue
                argument = argument[2:]
            if len(argument) >= 2 and argument[0].group() == "{" and argument[-1].group() == "}":
                argument = argument[1:-1]
            entries = [[]]
            for item in argument:
                if item.group() == ",":
                    entries.append([])
                else:
                    entries[-1].append(item)
            # 仅检查独立路径字面量，常量和组合表达式不在扫描范围内。
            if any(
                len(entry) == 1 and entry[0].lastgroup == "string"
                and re.match(r'^"/?v1(?:/|"$)', entry[0].group(), re.IGNORECASE)
                for entry in entries
            ):
                yield text.count("\n", 0, token.start()) + 1
                break


def main() -> int:
    violations: list[tuple[Path, int]] = []
    for source in java_sources(ROOT):
        text = source.read_text(encoding="utf-8")
        violations.extend((source, line) for line in legacy_lines(text))

    if violations:
        for source, line in violations:
            print(f"legacy /v1 route: {source.relative_to(ROOT)}:{line}", file=sys.stderr)
        return 1

    print("No legacy /v1 Spring controller routes found.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

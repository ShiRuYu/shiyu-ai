"""Generate an apply_patch patch for missing production Java method Javadocs."""

from __future__ import annotations

import difflib
import re
import sys
from pathlib import Path

from generate_java_javadocs_patch import TYPE_RE, has_javadoc, insertion_index, strip_code


MODIFIERS = (
    "public",
    "protected",
    "private",
    "static",
    "final",
    "abstract",
    "synchronized",
    "native",
    "default",
    "strictfp",
)
CONTROL_PREFIXES = ("if ", "for ", "while ", "switch ", "catch ", "try ", "return ", "new ", "throw ")


def split_parameters(text: str) -> list[str]:
    parts: list[str] = []
    current: list[str] = []
    angle = paren = bracket = 0
    for char in text:
        if char == "<":
            angle += 1
        elif char == ">" and angle:
            angle -= 1
        elif char == "(":
            paren += 1
        elif char == ")" and paren:
            paren -= 1
        elif char == "[":
            bracket += 1
        elif char == "]" and bracket:
            bracket -= 1
        if char == "," and angle == paren == bracket == 0:
            parts.append("".join(current))
            current = []
        else:
            current.append(char)
    if current:
        parts.append("".join(current))
    return parts


def parameter_names(text: str) -> list[str]:
    names: list[str] = []
    for part in split_parameters(text):
        identifiers = re.findall(r"[A-Za-z_$][\w$]*", part)
        if identifiers:
            names.append(identifiers[-1])
    return names


def method_description(name: str, constructor: bool) -> str:
    reference = "{@code " + name + "}"
    if constructor:
        return f"{reference} 创建并初始化当前类型实例。"
    if name.startswith(("get", "find", "query", "load", "list", "search", "resolve")):
        return f"{reference} 查询并返回当前操作所需的数据。"
    if name.startswith(("set", "update", "save", "create", "register", "record")):
        return f"{reference} 写入或更新当前模块中的业务数据。"
    if name.startswith(("validate", "check", "is", "has", "can")):
        return f"{reference} 校验当前操作的输入或状态是否满足约束。"
    if name.startswith(("execute", "run", "process", "handle", "dispatch", "publish")):
        return f"{reference} 执行当前模块定义的业务流程。"
    if name.startswith(("close", "release", "remove", "delete")):
        return f"{reference} 释放或移除当前操作涉及的资源。"
    if name.startswith("to"):
        return f"{reference} 将当前对象转换为目标表示形式。"
    return f"{reference} 执行当前类型定义的业务操作。"


def method_match(candidate: str) -> re.Match[str] | None:
    if not candidate or candidate.startswith(CONTROL_PREFIXES) or "(" not in candidate:
        return None
    if "=" in candidate[: candidate.find("(")]:
        return None
    if TYPE_RE.search(candidate):
        return None
    modifiers = r"(?:(?:" + "|".join(MODIFIERS) + r")\s+)*"
    constructor = re.match(modifiers + r"(?P<name>[A-Za-z_$][\w$]*)\s*\((?P<params>.*)\)", candidate)
    if constructor:
        return constructor
    method = re.match(
        modifiers
        + r"(?P<return>[A-Za-z_$][\w$<>, ?\[\].]*?)\s+"
        + r"(?P<name>[A-Za-z_$][\w$]*)\s*\((?P<params>.*)\)",
        candidate,
    )
    return method


def method_javadoc(match: re.Match[str], indent: str) -> list[str]:
    name = match.group("name")
    constructor = "return" not in match.groupdict()
    lines = [indent + "/**", indent + " * " + method_description(name, constructor)]
    params = parameter_names(match.group("params"))
    if params:
        lines.append(indent + " *")
        lines.extend(indent + " * @param " + item + " 参数值，用于执行当前操作。" for item in params)
    if not constructor and match.groupdict().get("return", "").strip() != "void":
        lines.append(indent + " *")
        lines.append(indent + " * @return 返回当前操作产生的结果。")
    lines.append(indent + " */")
    return lines


def process(path: Path) -> str | None:
    original = path.read_text(encoding="utf-8")
    lines = original.splitlines()
    code = strip_code(original).splitlines()
    code.extend([""] * (len(lines) - len(code)))
    depth = 0
    type_stack: list[int] = []
    pending: str | None = None
    pending_start: int | None = None
    inserts: dict[int, list[str]] = {}

    for index, code_line in enumerate(code):
        declarations = list(TYPE_RE.finditer(code_line))
        for declaration in declarations:
            if "{" in code_line:
                type_stack.append(depth + 1)
        if type_stack and depth >= type_stack[-1]:
            candidate = code_line.strip()
            if pending is None:
                if candidate and not candidate.startswith(("@", "*", "/")):
                    if "(" in candidate and not candidate.startswith(CONTROL_PREFIXES):
                        pending = candidate
                        pending_start = index
            else:
                pending += " " + candidate
            if pending and ("{" in pending or ";" in pending):
                match = method_match(pending)
                start = pending_start
                pending = None
                pending_start = None
                if match and start is not None:
                    # 公共和受保护方法构成模块契约，私有辅助方法保留为实现细节。
                    visibility = re.match(r"^(?:public|protected)\b", code[start].strip())
                    if visibility and not has_javadoc(lines, start):
                        target = insertion_index(lines, start)
                        indent = re.match(r"\s*", lines[start]).group(0)
                        inserts.setdefault(target, []).extend(method_javadoc(match, indent))
        depth = max(0, depth + code_line.count("{") - code_line.count("}"))
        while type_stack and depth < type_stack[-1]:
            type_stack.pop()

    if not inserts:
        return None
    updated: list[str] = []
    for index, line in enumerate(lines):
        updated.extend(inserts.get(index, []))
        updated.append(line)
    return "\n".join(updated) + "\n"


def main() -> int:
    chunks: list[str] = []
    prefix = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("modules")
    for path in prefix.rglob("*.java"):
        if "src/main/java" not in path.as_posix() or "target" in path.parts:
            continue
        updated = process(path)
        if updated is not None:
            chunks.extend(
                difflib.unified_diff(
                    path.read_text(encoding="utf-8").splitlines(True),
                    updated.splitlines(True),
                    fromfile="a/" + path.as_posix(),
                    tofile="b/" + path.as_posix(),
                    n=2,
                )
            )
    print("*** Begin Patch")
    for line in chunks:
        if line.startswith("--- a/"):
            print("*** Update File: E:/Dev/shiyu/shiyu-ai/" + line[6:].rstrip())
        elif line.startswith(("+++ b/", "\\ No newline")):
            continue
        elif line.startswith("@@"):
            print("@@")
        else:
            print(line.rstrip("\n"))
    print("*** End Patch")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

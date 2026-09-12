"""Check that Java interface methods have functional Javadocs.

The scanner intentionally checks only methods declared directly by an
interface. Marker interfaces and interfaces that only inherit methods therefore
produce no findings. Annotation declarations, nested interfaces, annotations on
methods, multiline signatures, and default methods are supported.
"""

from __future__ import annotations

import argparse
import re
import sys
from dataclasses import dataclass
from pathlib import Path


EXCLUDED_PARTS = {
    ".git",
    ".idea",
    "target",
    "node_modules",
    "build",
    "dist",
    "generated",
    "_validation",
}
INTERFACE_RE = re.compile(
    r"\b(?:public\s+|protected\s+|private\s+|abstract\s+|sealed\s+|non-sealed\s+|static\s+)*"
    r"interface\s+(?P<name>[A-Za-z_$][\w$]*)"
)
IDENTIFIER_BEFORE_PAREN = re.compile(r"(?P<name>[A-Za-z_$][\w$]*)\s*\(")
CONTROL_NAMES = {"if", "for", "while", "switch", "catch", "synchronized"}
PARAM_DESCRIPTIONS = {
    "actor": "当前操作主体上下文",
    "tenant": "租户上下文",
    "tenantid": "租户标识",
    "userid": "用户标识",
    "studentid": "学生标识",
    "courseid": "课程标识",
    "chapterid": "章节标识",
    "knowledgeid": "知识点标识",
    "id": "目标对象标识",
    "ids": "目标对象标识集合",
    "request": "请求参数",
    "response": "响应对象",
    "config": "配置参数",
    "options": "操作选项",
    "status": "对象状态",
    "type": "对象类型",
    "name": "对象名称",
    "value": "参数值",
    "page": "分页参数",
    "pagesize": "分页大小",
    "pagenum": "页码",
}


@dataclass(frozen=True)
class Violation:
    path: Path
    line: int
    interface_name: str
    method_name: str


@dataclass(frozen=True)
class MethodDeclaration:
    interface_name: str
    method_name: str
    signature: str
    member_offset: int
    declaration_offset: int
    line: int
    return_type: str
    parameter_names: list[str]


def _parameter_description(name: str) -> str:
    return PARAM_DESCRIPTIONS.get(name.lower(), "方法参数")


def _method_description(name: str) -> str:
    lowered = name.lower()
    if lowered.startswith(("get", "find", "query", "select", "load", "search", "list")):
        if lowered.endswith("byid") or lowered in {"get", "find", "query"}:
            return "根据标识查询对应的数据。"
        return "根据条件查询并返回所需数据。"
    if lowered.startswith(("count", "sum", "aggregate")):
        return "统计符合条件的数据。"
    if lowered.startswith(("create", "insert", "add", "register")):
        return "创建并保存业务对象。"
    if lowered.startswith(("save", "store", "upsert")):
        return "保存或更新业务对象。"
    if lowered.startswith(("update", "replace", "modify")):
        return "更新业务对象及其关联数据。"
    if lowered.startswith(("delete", "remove", "clear")):
        return "删除指定业务对象或关联数据。"
    if lowered.startswith(("validate", "check", "verify")):
        return "校验输入参数或当前业务状态。"
    if lowered.startswith(("is", "has", "can", "exists")):
        return "判断当前条件是否满足。"
    if lowered.startswith(("publish", "emit", "send", "dispatch")):
        return "发布或发送业务事件。"
    if lowered.startswith(("execute", "run", "process", "handle")):
        return "执行当前接口定义的业务流程。"
    if lowered.startswith(("start", "stop", "pause", "resume", "release", "settle", "reserve")):
        return "变更当前业务对象的处理状态。"
    return f"执行 {{@code {name}}} 定义的接口操作。"


def _return_description(return_type: str) -> str | None:
    normalized = return_type.replace(" ", "").lower()
    if normalized == "void":
        return None
    if normalized in {"boolean", "bool"}:
        return "条件是否满足。"
    if normalized in {"int", "integer", "long", "short"}:
        return "操作影响的记录数或状态码。"
    if any(token in normalized for token in ("list", "set", "collection", "map", "[]")):
        return "符合条件的结果集合。"
    if "optional" in normalized:
        return "查询到的结果；未找到时为空。"
    return "操作结果。"


def render_method_javadoc(
    method_name_value: str,
    return_type: str,
    parameter_names: list[str],
    indent: str = "",
) -> str:
    """Render a functional Chinese Javadoc block for an interface method."""

    lines = [indent + "/**", indent + " * " + _method_description(method_name_value)]
    if parameter_names:
        lines.append(indent + " *")
        lines.extend(
            indent + " * @param " + name + " " + _parameter_description(name) + "。"
            for name in parameter_names
        )
    return_description = _return_description(return_type)
    if return_description is not None:
        lines.append(indent + " *")
        lines.append(indent + " * @return " + return_description)
    lines.append(indent + " */")
    return "\n".join(lines)


def mask_java(source: str) -> str:
    """Remove comments and literals while preserving source positions."""

    masked = list(source)
    index = 0
    state = "code"
    while index < len(source):
        if state == "code":
            if source.startswith("//", index):
                masked[index] = masked[index + 1] = " "
                index += 2
                state = "line-comment"
                continue
            if source.startswith("/*", index):
                masked[index] = masked[index + 1] = " "
                index += 2
                state = "block-comment"
                continue
            if source[index] == '"':
                masked[index] = " "
                index += 1
                state = "string"
                continue
            if source[index] == "'":
                masked[index] = " "
                index += 1
                state = "character"
                continue
            index += 1
            continue

        if state == "line-comment":
            if source[index] == "\n":
                state = "code"
            elif source[index] != "\r":
                masked[index] = " "
            index += 1
            continue

        if state == "block-comment":
            if source.startswith("*/", index):
                masked[index] = masked[index + 1] = " "
                index += 2
                state = "code"
            else:
                if source[index] not in "\r\n":
                    masked[index] = " "
                index += 1
            continue

        if source[index] == "\\":
            masked[index] = " "
            index += 1
            if index < len(source):
                masked[index] = " "
                index += 1
        elif (state == "string" and source[index] == '"') or (
            state == "character" and source[index] == "'"
        ):
            masked[index] = " "
            index += 1
            state = "code"
        else:
            if source[index] not in "\r\n":
                masked[index] = " "
            index += 1
    return "".join(masked)


def matching_brace(masked: str, opening: int) -> int | None:
    depth = 0
    for index in range(opening, len(masked)):
        if masked[index] == "{":
            depth += 1
        elif masked[index] == "}":
            depth -= 1
            if depth == 0:
                return index
    return None


def strip_annotations(signature: str) -> str:
    """Remove annotation declarations from a member signature."""

    result: list[str] = []
    index = 0
    while index < len(signature):
        if signature[index] == "@":
            index += 1
            while index < len(signature) and (signature[index].isalnum() or signature[index] in "_.$"):
                index += 1
            while index < len(signature) and signature[index].isspace():
                index += 1
            if index < len(signature) and signature[index] == "(":
                depth = 0
                while index < len(signature):
                    if signature[index] == "(":
                        depth += 1
                    elif signature[index] == ")":
                        depth -= 1
                        if depth == 0:
                            index += 1
                            break
                    index += 1
            continue
        result.append(signature[index])
        index += 1
    return "".join(result)


def method_name(signature: str) -> str | None:
    candidate = strip_annotations(signature).strip()
    if "=" in candidate[: candidate.find("(")] if "(" in candidate else False:
        return None
    matches = list(IDENTIFIER_BEFORE_PAREN.finditer(candidate))
    if not matches:
        return None
    name = matches[-1].group("name")
    if name in CONTROL_NAMES:
        return None
    prefix = candidate[: matches[-1].start()].strip()
    if not prefix or any(token in prefix for token in (" class ", " interface ", " enum ", " record ")):
        return None
    return name


def method_offset(signature: str, name: str) -> int:
    """Return the method-name offset within a masked member signature."""

    matches = list(re.finditer(rf"\b{re.escape(name)}\s*\(", signature))
    return matches[-1].start() if matches else 0


def _split_parameters(text: str) -> list[str]:
    parts: list[str] = []
    current: list[str] = []
    angle = parenthesis = bracket = 0
    for char in text:
        if char == "<":
            angle += 1
        elif char == ">" and angle:
            angle -= 1
        elif char == "(":
            parenthesis += 1
        elif char == ")" and parenthesis:
            parenthesis -= 1
        elif char == "[":
            bracket += 1
        elif char == "]" and bracket:
            bracket -= 1
        if char == "," and angle == parenthesis == bracket == 0:
            parts.append("".join(current))
            current = []
        else:
            current.append(char)
    if current:
        parts.append("".join(current))
    return parts


def _parameter_names(text: str) -> list[str]:
    names: list[str] = []
    for parameter in _split_parameters(strip_annotations(text)):
        identifiers = re.findall(r"[A-Za-z_$][\w$]*", parameter)
        if identifiers:
            names.append(identifiers[-1])
    return names


def _method_details(signature: str, name: str) -> tuple[str, list[str]]:
    cleaned = strip_annotations(signature).strip()
    match = list(IDENTIFIER_BEFORE_PAREN.finditer(cleaned))[-1]
    opening = match.end() - 1
    depth = 0
    closing = len(cleaned) - 1
    for index in range(opening, len(cleaned)):
        if cleaned[index] == "(":
            depth += 1
        elif cleaned[index] == ")":
            depth -= 1
            if depth == 0:
                closing = index
                break
    prefix = cleaned[: match.start()].strip()
    prefix = re.sub(
        r"^(?:(?:public|protected|private|static|default|abstract|strictfp|synchronized|final)\s+)+",
        "",
        prefix,
    )
    if prefix.startswith("<"):
        generic_end = prefix.find(">")
        if generic_end >= 0:
            prefix = prefix[generic_end + 1 :].strip()
    return_type = prefix.split()[-1] if prefix.split() else "void"
    return return_type, _parameter_names(cleaned[opening + 1 : closing])


def interface_methods(source: str) -> list[MethodDeclaration]:
    masked = mask_java(source)
    methods: list[MethodDeclaration] = []
    for interface in INTERFACE_RE.finditer(masked):
        opening = masked.find("{", interface.end())
        if opening < 0:
            continue
        closing = matching_brace(masked, opening)
        if closing is None:
            continue
        for signature, offset in direct_member_signatures(masked[opening + 1 : closing], opening + 1):
            name = method_name(signature)
            if name is None:
                continue
            declaration_offset = offset + method_offset(signature, name)
            return_type, parameter_names = _method_details(signature, name)
            methods.append(
                MethodDeclaration(
                    interface_name=interface.group("name"),
                    method_name=name,
                    signature=signature,
                    member_offset=offset,
                    declaration_offset=declaration_offset,
                    line=masked.count("\n", 0, declaration_offset) + 1,
                    return_type=return_type,
                    parameter_names=parameter_names,
                )
            )
    return methods


def has_javadoc(lines: list[str], declaration_line: int) -> bool:
    """Return whether a Javadoc block precedes a declaration or its annotations."""

    index = declaration_line - 1
    while index >= 0:
        stripped = lines[index].strip()
        if not stripped or stripped.startswith("@"):
            index -= 1
            continue
        if stripped.endswith("*/"):
            while index >= 0:
                if "/**" in lines[index]:
                    return True
                if "/*" in lines[index]:
                    return False
                index -= 1
            return False
        return False
    return False


def first_code_line(source: str, method: MethodDeclaration) -> int:
    """Return the zero-based line containing the member's first code token."""

    masked = mask_java(source)
    prefix = masked[method.member_offset : method.declaration_offset]
    relative = next(
        (index for index, char in enumerate(prefix) if not char.isspace()),
        0,
    )
    return source.count("\n", 0, method.member_offset + relative)


def direct_member_signatures(masked_body: str, body_offset: int):
    """Yield direct interface member signatures and their absolute offsets."""

    depth = 0
    segment_start = 0
    index = 0
    while index < len(masked_body):
        char = masked_body[index]
        if char == "{" and depth == 0:
            yield masked_body[segment_start:index], body_offset + segment_start
            depth = 1
            index += 1
            continue
        if char == "{" and depth > 0:
            depth += 1
        elif char == "}" and depth > 0:
            depth -= 1
            if depth == 0:
                segment_start = index + 1
        elif char == ";" and depth == 0:
            yield masked_body[segment_start : index + 1], body_offset + segment_start
            segment_start = index + 1
        index += 1


def scan_file(path: Path) -> list[Violation]:
    if path.suffix.lower() != ".java" or any(part in EXCLUDED_PARTS for part in path.parts):
        return []
    source = path.read_text(encoding="utf-8")
    lines = source.splitlines()
    violations: list[Violation] = []
    for method in interface_methods(source):
        if not (
            has_javadoc(lines, method.line - 1)
            or has_javadoc(lines, first_code_line(source, method))
        ):
            violations.append(Violation(path, method.line, method.interface_name, method.method_name))
    return violations


def iter_files(paths: list[Path]):
    seen: set[Path] = set()
    for root in paths:
        root = root.resolve()
        candidates = [root] if root.is_file() else root.rglob("*.java")
        for path in candidates:
            if not path.is_file() or path in seen:
                continue
            seen.add(path)
            yield path


def scan_paths(paths: list[Path]) -> list[Violation]:
    violations: list[Violation] = []
    for path in iter_files(paths):
        normalized = path.as_posix()
        if "src/main/java" not in normalized and "src/test/java" not in normalized:
            continue
        violations.extend(scan_file(path))
    return violations


def main() -> int:
    parser = argparse.ArgumentParser(description="检查 Java 接口方法功能注释")
    parser.add_argument("paths", nargs="+", help="待扫描的 Java 文件或目录")
    args = parser.parse_args()
    violations = scan_paths([Path(path) for path in args.paths])
    for violation in violations:
        print(f"{violation.path}:{violation.line}: {violation.interface_name}.{violation.method_name} 缺少接口方法功能注释")
    if violations:
        print(f"接口方法注释检查失败：发现 {len(violations)} 项问题。")
        return 1
    print("接口方法注释检查通过：未发现缺少功能注释的接口方法。")
    return 0


if __name__ == "__main__":
    sys.exit(main())

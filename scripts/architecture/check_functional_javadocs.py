"""Detect JavaDoc that does not describe the actual type or method behavior.

The checker is intentionally conservative.  It catches known generated
placeholders and reports them as a reviewable baseline; it does not claim that
natural language is semantically equivalent to an implementation.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from collections import Counter, defaultdict
from dataclasses import asdict, dataclass
from pathlib import Path


EXCLUDED_PARTS = {
    ".git",
    ".idea",
    "target",
    "generated",
    "build",
    "dist",
    "backup",
    "backups",
}
TYPE_RE = re.compile(r"\b(class|interface|enum|record)\s+([A-Za-z_$][\w$]*)")
DOC_RE = re.compile(r"/\*\*.*?\*/", re.DOTALL)
CONTROL_NAMES = {"if", "for", "while", "switch", "catch", "synchronized"}
MODIFIER_WORDS = {
    "public",
    "protected",
    "private",
    "static",
    "final",
    "abstract",
    "sealed",
    "non-sealed",
    "strictfp",
    "default",
    "native",
    "synchronized",
}

TEMPLATE_PATTERNS = {
    "template-type": (
        re.compile(r"^(?:[A-Za-z_$][\w$]*\s+)?承载.+领域状态或协作行为，负责维护本类型的职责边界。?$"),
        re.compile(r"^(?:[A-Za-z_$][\w$]*\s+)?(?:.+模块的(?:Web接口)?适配器)，负责接收请求并转换为应用服务调用。?$"),
        re.compile(r"^(?:[A-Za-z_$][\w$]*\s+)?[^，。]{1,80}(?:服务接口|服务实现)，负责执行[^，。]{1,80}(?:领域)?相关业务操作。?$"),
        re.compile(r"^执行当前类型定义的业务操作。?$"),
    ),
    "template-method": (
        re.compile(r"^执行当前类型定义的业务操作。?$"),
        re.compile(r"^查询并返回当前操作所需的数据。?$"),
        re.compile(r"^写入或更新当前模块中的业务数据。?$"),
        re.compile(r"^判断当前类型定义的业务条件是否满足。?$"),
        re.compile(r"^执行使用备用。?$"),
    ),
    "template-param": (
        re.compile(r"^参数值，用于执行当前操作。?$"),
        re.compile(r"^参数。?$"),
        re.compile(r"^方法参数。?$"),
    ),
    "template-return": (
        re.compile(r"^返回当前操作产生的结果。?$"),
        re.compile(r"^结果列表。?$"),
        re.compile(r"^操作结果。?$"),
    ),
}
TEST_GENERIC = re.compile(r"^验证\s+.+的功能、边界条件和集成行为。?$")
TAG_RE = re.compile(r"(?<!\{)(?:^|\s)@(param|return|throws)\b")
CODE_TAG_RE = re.compile(r"\{@(?:code|link)\s+([^}]+)\}")
WHITESPACE_RE = re.compile(r"\s+")


@dataclass(frozen=True)
class TypeInfo:
    kind: str
    name: str
    owner: str
    declaration_offset: int
    body_open: int
    body_close: int
    line: int


@dataclass(frozen=True)
class MethodInfo:
    owner: str
    name: str
    signature: str
    declaration_offset: int
    line: int
    override: bool


@dataclass(frozen=True)
class DocInfo:
    line: int
    description: str
    params: dict[str, str]
    returns: str | None
    throws: dict[str, str]
    summary: str


@dataclass(frozen=True)
class JavadocMatch:
    """表示位于 Java 源码中的安全 Javadoc 区间。"""

    start_offset: int
    end_offset: int
    block: str

    def start(self) -> int:
        return self.start_offset

    def end(self) -> int:
        return self.end_offset

    def group(self, *_args: int) -> str:
        return self.block


@dataclass(frozen=True)
class Issue:
    path: str
    line: int
    source_set: str
    module: str
    owner: str
    member: str
    category: str
    message: str
    summary: str

    @property
    def key(self) -> str:
        normalized = normalize_text(self.summary)
        return "|".join(
            (self.source_set, self.module, self.owner, self.member, self.category, normalized)
        )

    def to_dict(self) -> dict[str, str | int]:
        value = asdict(self)
        value["key"] = self.key
        return value


def normalize_text(value: str) -> str:
    value = CODE_TAG_RE.sub(r"\1", value)
    return WHITESPACE_RE.sub(" ", value).strip()


def mask_java(source: str) -> str:
    """Mask comments and literals while preserving offsets and line breaks."""

    masked = list(source)
    index = 0
    state = "code"
    while index < len(source):
        char = source[index]
        following = source[index + 1] if index + 1 < len(source) else ""
        if state == "code":
            if source.startswith("//", index):
                masked[index] = masked[index + 1] = " "
                index += 2
                state = "line"
            elif source.startswith("/*", index):
                masked[index] = masked[index + 1] = " "
                index += 2
                state = "block"
            elif char == '"':
                masked[index] = " "
                index += 1
                state = "string"
            elif char == "'":
                masked[index] = " "
                index += 1
                state = "character"
            else:
                index += 1
        elif state == "line":
            if char == "\n":
                state = "code"
            elif char != "\r":
                masked[index] = " "
            index += 1
        elif state == "block":
            if source.startswith("*/", index):
                masked[index] = masked[index + 1] = " "
                index += 2
                state = "code"
            else:
                if char not in "\r\n":
                    masked[index] = " "
                index += 1
        else:
            if char == "\\":
                masked[index] = " "
                index += 1
                if index < len(source):
                    masked[index] = " "
                    index += 1
            elif (state == "string" and char == '"') or (
                state == "character" and char == "'"
            ):
                masked[index] = " "
                index += 1
                state = "code"
            else:
                if char not in "\r\n":
                    masked[index] = " "
                index += 1
    return "".join(masked)


def iter_javadocs(source: str, end: int | None = None):
    """遍历源码中的真实 Javadoc，跳过字符串和普通注释里的 ``/**``。"""

    limit = len(source) if end is None else min(end, len(source))
    index = 0
    state = "code"
    while index < limit:
        char = source[index]
        following = source[index + 1] if index + 1 < limit else ""
        if state == "code":
            if source.startswith("/**", index):
                close = source.find("*/", index + 3, limit)
                if close < 0:
                    return
                finish = close + 2
                yield JavadocMatch(index, finish, source[index:finish])
                index = finish
                continue
            if char == "/" and following == "/":
                state = "line"
                index += 2
                continue
            if char == "/" and following == "*":
                state = "block"
                index += 2
                continue
            if char == '"':
                state = "string"
            elif char == "'":
                state = "character"
            index += 1
            continue
        if state == "line":
            if char == "\n":
                state = "code"
            index += 1
            continue
        if state == "block":
            if char == "*" and following == "/":
                state = "code"
                index += 2
            else:
                index += 1
            continue
        if char == "\\":
            index += 2
            continue
        if (state == "string" and char == '"') or (
            state == "character" and char == "'"
        ):
            state = "code"
        index += 1


def matching(masked: str, opening: int, left: str, right: str) -> int | None:
    depth = 0
    for index in range(opening, len(masked)):
        if masked[index] == left:
            depth += 1
        elif masked[index] == right:
            depth -= 1
            if depth == 0:
                return index
    return None


def line_number(source: str, offset: int) -> int:
    return source.count("\n", 0, offset) + 1


def package_name(source: str) -> str:
    match = re.search(r"^\s*package\s+([\w.]+)\s*;", source, re.MULTILINE)
    return match.group(1) if match else ""


def source_set(path: Path) -> str:
    normalized = path.as_posix()
    return "test" if "/src/test/java/" in normalized else "production"


def module_name(path: Path) -> str:
    parts = path.parts
    for index, part in enumerate(parts):
        if part == "src" and index > 0:
            return parts[index - 1]
    return path.parent.name


def first_code_offset(masked: str, start: int, end: int) -> int | None:
    for index in range(start, end):
        if not masked[index].isspace():
            return index
    return None


def find_type_open(masked: str, start: int) -> int | None:
    parentheses = 0
    brackets = 0
    for index in range(start, len(masked)):
        char = masked[index]
        if char == "(":
            parentheses += 1
        elif char == ")":
            parentheses = max(0, parentheses - 1)
        elif char == "[":
            brackets += 1
        elif char == "]":
            brackets = max(0, brackets - 1)
        elif char == "{" and parentheses == 0 and brackets == 0:
            return index
    return None


def discover_types(source: str, masked: str) -> list[TypeInfo]:
    matches = list(TYPE_RE.finditer(masked))
    raw: list[tuple[re.Match[str], int, int]] = []
    for match in matches:
        body_open = find_type_open(masked, match.end())
        if body_open is None:
            continue
        body_close = matching(masked, body_open, "{", "}")
        if body_close is None:
            continue
        raw.append((match, body_open, body_close))

    package = package_name(source)
    result: list[TypeInfo] = []
    for match, body_open, body_close in raw:
        parent = next(
            (
                item
                for item in result
                if item.body_open < match.start() < item.body_close
                and (item.body_close - item.body_open)
                == min(
                    other.body_close - other.body_open
                    for other in result
                    if other.body_open < match.start() < other.body_close
                )
            ),
            None,
        )
        owner = f"{parent.owner}.{match.group(2)}" if parent else f"{package}.{match.group(2)}"
        line_start = source.rfind("\n", 0, match.start()) + 1
        declaration_offset = first_code_offset(masked, line_start, match.start()) or match.start()
        result.append(
            TypeInfo(
                kind=match.group(1),
                name=match.group(2),
                owner=owner,
                declaration_offset=declaration_offset,
                body_open=body_open,
                body_close=body_close,
                line=line_number(source, match.start()),
            )
        )
    return result


def annotation_lines_only(text: str) -> bool:
    depth = 0
    for raw in text.splitlines():
        line = raw.strip()
        if not line:
            continue
        if depth:
            depth += line.count("(") - line.count(")")
            if depth < 0:
                return False
            continue
        if not line.startswith("@"):
            return False
        depth = line.count("(") - line.count(")")
    return depth == 0


def find_javadoc(source: str, offset: int) -> tuple[int, str] | None:
    candidate = None
    for match in iter_javadocs(source, offset):
        candidate = match
    if candidate is None:
        return None
    if not annotation_lines_only(source[candidate.end() : offset]):
        return None
    return line_number(source, candidate.start()), candidate.group(0)


def clean_doc_lines(block: str) -> str:
    inner = block[3:-2]
    lines = []
    for line in inner.splitlines():
        line = re.sub(r"^\s*\*?\s?", "", line)
        lines.append(line.rstrip())
    return normalize_text(" ".join(lines))


def parse_doc(line: int, block: str) -> DocInfo:
    cleaned = clean_doc_lines(block)
    positions = list(TAG_RE.finditer(cleaned))
    description = normalize_text(cleaned[: positions[0].start()] if positions else cleaned)
    params: dict[str, str] = {}
    returns: str | None = None
    throws: dict[str, str] = {}
    for index, tag in enumerate(positions):
        end = positions[index + 1].start() if index + 1 < len(positions) else len(cleaned)
        value = normalize_text(cleaned[tag.end() : end])
        kind = tag.group(1)
        if kind == "param":
            match = re.match(r"(?:<([A-Za-z_$][\w$]*)>|([A-Za-z_$][\w$]*))\s*(.*)", value)
            if match:
                params[match.group(1) or match.group(2)] = match.group(3)
        elif kind == "return":
            returns = value
        else:
            match = re.match(r"([A-Za-z_$][\w$.$]*)\s*(.*)", value)
            if match:
                throws[match.group(1)] = match.group(2)
    return DocInfo(line, description, params, returns, throws, cleaned)


def normalize_signature(signature: str) -> str:
    value = re.sub(r"/\*.*?\*/", " ", signature, flags=re.DOTALL)
    value = re.sub(r"@\w+(?:\([^)]*\))?", " ", value)
    value = re.sub(r"[;{]\s*$", "", value)
    return normalize_text(value)


def discover_methods(source: str, masked: str, type_info: TypeInfo) -> list[MethodInfo]:
    methods: list[MethodInfo] = []
    index = type_info.body_open + 1
    segment_start = index
    while index < type_info.body_close:
        char = masked[index]
        if char == "(":
            close = matching(masked, index, "(", ")")
            if close is None or close >= type_info.body_close:
                break
            prefix = masked[segment_start:index]
            name_match = re.search(r"([A-Za-z_$][\w$]*)\s*$", prefix)
            name = name_match.group(1) if name_match else ""
            valid = bool(name) and name not in CONTROL_NAMES and "=" not in prefix
            boundary = close + 1
            while boundary < type_info.body_close and masked[boundary].isspace():
                boundary += 1
            while boundary < type_info.body_close and masked[boundary] not in "{;":
                if masked[boundary] == "=":
                    valid = False
                boundary += 1
            if valid and boundary < type_info.body_close and masked[boundary] in "{;":
                member_start = first_code_offset(masked, segment_start, index)
                if member_start is not None:
                    declaration = source[member_start : boundary + 1]
                    methods.append(
                        MethodInfo(
                            owner=type_info.owner,
                            name=name,
                            signature=normalize_signature(declaration),
                            declaration_offset=member_start,
                            line=line_number(source, member_start),
                            override="@Override" in source[segment_start:member_start],
                        )
                    )
                    if masked[boundary] == "{":
                        end = matching(masked, boundary, "{", "}")
                        if end is None:
                            break
                        index = end + 1
                        segment_start = index
                        continue
                    index = boundary + 1
                    segment_start = index
                    continue
            index = close + 1
            continue
        if char == "{":
            end = matching(masked, index, "{", "}")
            if end is None:
                break
            index = end + 1
            segment_start = index
            continue
        if char == ";":
            index += 1
            segment_start = index
            continue
        index += 1
    return methods


def category_for(description: str, kind: str, member_name: str = "") -> str | None:
    text = normalize_text(description)
    if not text:
        return "missing-description"
    if kind == "throws":
        if text in {"异常信息。", "操作失败时抛出异常。", "异常。"}:
            return "review"
        return None
    if kind == "type" and TEST_GENERIC.search(text):
        return "review"
    if member_name and (
        re.search(
            rf"(?:\{{@code\s+)?{re.escape(member_name)}\}}?\s*执行当前类型定义的业务操作。?$",
            text,
        )
        or re.fullmatch(
            rf"执行\s+(?:\{{@code\s+)?{re.escape(member_name)}\}}?\s+定义的接口操作。?",
            text,
        )
    ):
        return "name-only"
    patterns = TEMPLATE_PATTERNS.get(f"template-{kind}", ())
    if any(pattern.search(text) for pattern in patterns):
        return f"template-{kind}"
    if kind in {"type", "method"} and (
        "职责边界" in text or "业务状态或协作行为" in text
    ):
        return "review"
    return None


def issue_message(category: str) -> str:
    return {
        "template-type": "类型注释使用泛化职责模板，未说明该类型的实际功能",
        "template-method": "方法注释使用泛化操作模板，未说明实际行为、条件或副作用",
        "template-param": "参数注释使用泛化模板，未说明参数的业务含义",
        "template-return": "返回值注释使用泛化模板，未说明真实返回结果",
        "name-only": "方法注释只重复方法名和泛化操作，没有描述功能",
        "missing-description": "Javadoc 没有有效的功能描述",
        "review": "注释疑似泛化或测试模板，需要结合实现人工复核",
    }[category]


def module_relative(path: Path) -> str:
    try:
        return path.resolve().relative_to(Path.cwd().resolve()).as_posix()
    except ValueError:
        return path.resolve().as_posix()


def scan_file(path: Path, include_tests: bool = False) -> list[Issue]:
    if path.suffix != ".java" or any(part in EXCLUDED_PARTS for part in path.parts):
        return []
    current_source_set = source_set(path)
    if current_source_set == "test" and not include_tests:
        return []
    source = path.read_text(encoding="utf-8")
    masked = mask_java(source)
    types = discover_types(source, masked)
    issues: list[Issue] = []
    module = module_name(path)
    for type_info in types:
        doc_location = find_javadoc(source, type_info.declaration_offset)
        if doc_location:
            line, block = doc_location
            doc = parse_doc(line, block)
            category = category_for(doc.description, "type")
            if category:
                issues.append(
                    Issue(
                        module_relative(path),
                        line,
                        current_source_set,
                        module,
                        type_info.owner,
                        "<type>",
                        category,
                        issue_message(category),
                        doc.summary,
                    )
                )
        for method in discover_methods(source, masked, type_info):
            doc_location = find_javadoc(source, method.declaration_offset)
            if not doc_location:
                continue
            line, block = doc_location
            doc = parse_doc(line, block)
            categories: list[str] = []
            details: list[str] = []
            category = category_for(doc.description, "method", method.name)
            if category:
                categories.append(category)
                details.append(f"方法描述：{issue_message(category)}")
            for parameter, description in doc.params.items():
                category = category_for(description, "param")
                if category:
                    categories.append(category)
                    details.append(f"@param {parameter}：{issue_message(category)}")
            if doc.returns is not None:
                category = category_for(doc.returns, "return")
                if category:
                    categories.append(category)
                    details.append(f"@return：{issue_message(category)}")
            for exception, description in doc.throws.items():
                category = category_for(description, "throws")
                if category:
                    categories.append(category)
                    details.append(f"@throws {exception}：{issue_message(category)}")
            if categories:
                unique_categories = list(dict.fromkeys(categories))
                issues.append(
                    Issue(
                        module_relative(path),
                        line,
                        current_source_set,
                        module,
                        method.owner,
                        method.signature,
                        "+".join(unique_categories),
                        "；".join(details),
                        doc.summary,
                    )
                )
    return issues


def iter_java_files(paths: list[Path]):
    seen: set[Path] = set()
    for raw in paths:
        root = raw.resolve()
        candidates = [root] if root.is_file() else root.rglob("*.java")
        for path in candidates:
            if path in seen or not path.is_file() or any(part in EXCLUDED_PARTS for part in path.parts):
                continue
            seen.add(path)
            yield path


def scan_paths(paths: list[Path], include_tests: bool = False) -> list[Issue]:
    issues: list[Issue] = []
    for path in iter_java_files(paths):
        issues.extend(scan_file(path, include_tests=include_tests))
    return sorted(issues, key=lambda item: (item.source_set, item.module, item.path, item.line, item.member, item.category))


def load_baseline(path: Path) -> dict[str, dict]:
    if not path.is_file():
        return {}
    payload = json.loads(path.read_text(encoding="utf-8"))
    if isinstance(payload, dict):
        entries = payload.get("issues", [])
    elif isinstance(payload, list):
        entries = payload
    else:
        entries = []
    return {entry["key"]: entry for entry in entries}


def write_baseline(path: Path, issues: list[Issue]) -> None:
    production = [issue.to_dict() for issue in issues if issue.source_set == "production"]
    payload = {
        "version": 1,
        "scope": "modules/**/src/main/java",
        "issues": sorted(production, key=lambda item: item["key"]),
    }
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def new_issues(issues: list[Issue], baseline: dict[str, dict]) -> list[Issue]:
    return [issue for issue in issues if issue.source_set == "production" and issue.key not in baseline]


def render_report(issues: list[Issue], test_issues: list[Issue] | None = None, baseline: dict[str, dict] | None = None) -> str:
    test_issues = test_issues or []
    baseline = baseline or {}
    production = [issue for issue in issues if issue.source_set == "production"]
    modules = Counter(issue.module for issue in production)
    categories = Counter(
        category
        for issue in production
        for category in issue.category.split("+")
    )
    lines = [
        "# 工程功能注释整改清单",
        "",
        "本清单由 `check_functional_javadocs.py` 生成。它识别已知泛化模板，不能证明自然语言与实现完全一致；`review` 项必须人工核对。",
        "",
        "## 扫描范围",
        "",
        f"- 生产问题：{len(production)} 条，涉及 {len({item.path for item in production})} 个文件。",
        f"- 测试观察项：{len(test_issues)} 条，涉及 {len({item.path for item in test_issues})} 个文件。",
        f"- 当前基线：{len(baseline)} 条；新增问题：{len(new_issues(production, baseline))} 条。",
        "- 扫描路径：`modules/**/src/main/java`；测试路径：`modules/**/src/test/java`；排除 `target`、生成目录、构建目录和备份目录。",
        "- 规则覆盖类型、方法、构造方法以及 `@param`、`@return`、`@throws`；同一段 Javadoc 的多个命中聚合为一条记录。",
        "- 基线中的历史问题允许保留，新增问题阻断生产门禁；静态通过不等于注释已经与实现完全一致。",
        (
            "- 当前问题主要集中在 Agent、Education、IAM、Knowledge 和 Model 模块。"
            if production
            else "- 当前未发现已知模板问题；后续新增问题仍由基线门禁阻断。"
        ),
        "- 生产源码参与门禁；测试源码只单独统计，不阻断生产门禁。",
        "",
        "## 生产问题汇总",
        "",
        "| 模块 | 问题数 |",
        "| --- | ---: |",
    ]
    lines.extend(f"| `{module}` | {count} |" for module, count in sorted(modules.items()))
    lines.extend(["", "| 类别 | 问题数 |", "| --- | ---: |"])
    lines.extend(f"| `{category}` | {count} |" for category, count in sorted(categories.items()))
    lines.extend(["", "## 生产问题明细", "", "| 文件 | 行 | 类型/方法 | 类别 | 说明 |", "| --- | ---: | --- | --- | --- |"])
    lines.extend(
        f"| `{issue.path}` | {issue.line} | `{issue.owner}::{issue.member}` | `{issue.category}` | {issue.message} |"
        for issue in production
    )
    lines.extend(["", "## 测试观察项", "", "| 文件 | 行 | 类型/方法 | 类别 | 说明 |", "| --- | ---: | --- | --- | --- |"])
    lines.extend(
        f"| `{issue.path}` | {issue.line} | `{issue.owner}::{issue.member}` | `{issue.category}` | {issue.message} |"
        for issue in test_issues
    )
    lines.append("")
    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser(description="检查 JavaDoc 是否描述类型和方法的实际功能")
    parser.add_argument("paths", nargs="+", help="待扫描的源码目录或文件")
    parser.add_argument("--baseline", type=Path)
    parser.add_argument("--write-baseline", action="store_true")
    parser.add_argument("--report", type=Path)
    parser.add_argument("--tests-report", type=Path)
    parser.add_argument("--include-tests", action="store_true")
    args = parser.parse_args()

    all_issues = scan_paths([Path(value) for value in args.paths], include_tests=args.include_tests)
    production = [issue for issue in all_issues if issue.source_set == "production"]
    tests = [issue for issue in all_issues if issue.source_set == "test"]
    baseline = load_baseline(args.baseline) if args.baseline else {}
    if args.write_baseline:
        if not args.baseline:
            parser.error("--write-baseline requires --baseline")
        write_baseline(args.baseline, production)
        baseline = load_baseline(args.baseline)
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(render_report(production, tests if args.include_tests else [], baseline), encoding="utf-8")
    if args.tests_report and args.tests_report != args.report:
        args.tests_report.parent.mkdir(parents=True, exist_ok=True)
        args.tests_report.write_text(render_report([], tests, baseline), encoding="utf-8")
    additions = new_issues(production, baseline) if args.baseline else production
    for issue in additions:
        print(f"{issue.path}:{issue.line}: {issue.category}: {issue.owner}::{issue.member}")
    print(f"生产问题 {len(production)} 条，测试观察项 {len(tests)} 条，新增生产问题 {len(additions)} 条。")
    if additions:
        print("功能注释检查失败：发现未纳入基线的生产问题。")
        return 1
    print("功能注释检查通过：未发现新的生产模板问题。")
    return 0


if __name__ == "__main__":
    sys.exit(main())

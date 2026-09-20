"""Rewrite Java type and known template Javadocs with role-aware descriptions.

The repository uses this script for the bulk part of the Javadoc migration. It
rewrites every discovered type comment and rewrites method comments only when
the semantic scanner reports a known template or incomplete tag. The generated
text is deliberately based on the type/method name and parameter contract; it
is a consistent baseline for human refinement, not a substitute for review of
complex business behavior.
"""

from __future__ import annotations

import argparse
import importlib.util
import re
import sys
from dataclasses import dataclass
from pathlib import Path


SCRIPT = Path(__file__).with_name("check_functional_javadocs.py")
SPEC = importlib.util.spec_from_file_location("functional_javadocs", SCRIPT)
if SPEC is None or SPEC.loader is None:
    raise RuntimeError(f"无法加载扫描器: {SCRIPT}")
SCANNER = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = SCANNER
SPEC.loader.exec_module(SCANNER)


WORD_RE = re.compile(r"[A-Z]+(?=[A-Z][a-z]|\d|$)|[A-Z]?[a-z]+|\d+")
ANNOTATION_RE = re.compile(r"@\w+(?:\([^)]*\))?")
IDENTIFIER_RE = re.compile(r"[A-Za-z_$][\w$]*")

ROLE_SUFFIXES = (
    "ControllerAdvice",
    "ConfigurationProperties",
    "ServiceImpl",
    "RepositoryImpl",
    "ApplicationListener",
    "Application",
    "Controller",
    "Repository",
    "Configuration",
    "Properties",
    "Interceptor",
    "Contributor",
    "UseCase",
    "Parser",
    "Codec",
    "Storage",
    "Worker",
    "Creator",
    "Builder",
    "Node",
    "Agent",
    "Graph",
    "Utils",
    "Util",
    "Lock",
    "Sink",
    "Admission",
    "Helper",
    "Definition",
    "Execution",
    "State",
    "Status",
    "Result",
    "Entry",
    "Edge",
    "DO",
    "Assembler",
    "Converter",
    "Validator",
    "Registry",
    "Resolver",
    "Listener",
    "Publisher",
    "Handler",
    "Adapter",
    "Factory",
    "Manager",
    "Router",
    "Provider",
    "Context",
    "Service",
    "Mapper",
    "Request",
    "Response",
    "Exception",
    "Entity",
    "Record",
    "Properties",
    "Config",
    "Policy",
    "Port",
    "Event",
    "Store",
    "Runner",
    "Holder",
    "Engine",
    "Client",
    "Job",
    "Task",
    "Command",
    "Query",
    "VO",
    "DTO",
    "BO",
    "Test",
    "Tests",
)

WORD_TRANSLATIONS = {
    "AI": "AI",
    "Ai": "AI",
    "Agent": "智能体",
    "Analytics": "分析",
    "Api": "API",
    "App": "应用",
    "Auth": "认证",
    "Chapter": "章节",
    "Chat": "对话",
    "Conversation": "会话",
    "Course": "课程",
    "Database": "数据库",
    "Document": "文档",
    "Education": "教育",
    "Embedding": "嵌入",
    "Event": "事件",
    "Exam": "考试",
    "File": "文件",
    "Generation": "生成",
    "Governance": "治理",
    "Index": "索引",
    "Knowledge": "知识",
    "Memory": "记忆",
    "Message": "消息",
    "Model": "模型",
    "Platform": "平台",
    "Plugin": "插件",
    "Prompt": "提示词",
    "Question": "题目",
    "Recommendation": "推荐",
    "Relation": "关系",
    "Resource": "资源",
    "Review": "复习",
    "Role": "角色",
    "Run": "运行",
    "Security": "安全",
    "Space": "空间",
    "Student": "学生",
    "Subject": "学科",
    "Tenant": "租户",
    "Textbook": "教材",
    "Timeline": "时间线",
    "Tool": "工具",
    "Usage": "用量",
    "User": "用户",
    "Vector": "向量",
    "Web": "Web",
    "Workspace": "工作区",
}

PRESERVED_TAGS = {"deprecated", "see", "since", "version", "author", "throws"}


@dataclass(frozen=True)
class DocMatch:
    start: int
    end: int
    block: str


def camel_words(value: str) -> list[str]:
    return WORD_RE.findall(value.replace("$", " "))


def translate_words(value: str) -> str:
    words = camel_words(value)
    translated = [WORD_TRANSLATIONS.get(word, word) for word in words]
    return " ".join(translated) if translated else value


def split_role(name: str) -> tuple[str, str]:
    for suffix in ROLE_SUFFIXES:
        if name.endswith(suffix) and len(name) >= len(suffix):
            return name[: -len(suffix)], suffix
    return name, ""


def subject_for(name: str) -> str:
    base, _ = split_role(name)
    if not base:
        base = name
    return translate_words(base)


def type_description(type_info: SCANNER.TypeInfo, source_set: str) -> str:
    name = type_info.name
    subject = subject_for(name)
    owner_path = type_info.owner.lower()
    _, role = split_role(name)
    if source_set == "test" or name.endswith(("Test", "Tests")):
        return f"验证 {subject} 相关功能、边界条件、异常路径和协作行为。"
    if type_info.kind == "enum":
        return f"定义 {subject} 可用的枚举值及其业务语义。"
    if type_info.kind == "record":
        return f"封装 {subject} 相关的不可变数据及其字段约束。"
    if role == "Application":
        return f"启动 {subject} 应用并装配项目所需的运行基础设施。"
    if role in {"Controller", "ControllerAdvice"}:
        return f"处理 {subject} 相关的 Web 请求，并将请求转换为应用服务调用。"
    if role in {"Service", "ServiceImpl"}:
        return f"提供 {subject} 的查询、创建、更新及调用服务，协调业务变更和领域协作。"
    if role in {"Repository", "RepositoryImpl", "Mapper"}:
        return f"负责 {subject} 的持久化查询、保存和删除，并维护数据访问边界。"
    if role in {"Request", "Command", "Query"}:
        return f"封装 {subject} 操作所需的请求条件和输入数据。"
    if role in {"Response", "VO", "DTO"}:
        return f"封装 {subject} 操作向调用方返回的传输数据。"
    if role in {"BO", "Entity"}:
        return f"表示 {subject} 领域对象的业务状态和属性。"
    if role in {"Config", "Configuration", "Properties", "ConfigurationProperties"}:
        return f"定义 {subject} 基础设施或应用能力的配置项及装配规则。"
    if role in {"Handler", "Listener"}:
        return f"处理 {subject} 相关事件或请求，并推进后续业务流程。"
    if role == "Contributor":
        return f"向 {subject} 所属的应用或基础设施注册必要的扩展能力。"
    if role == "Publisher":
        return f"发布 {subject} 相关的领域事件或基础设施消息。"
    if role in {"Adapter", "Converter", "Assembler"}:
        return f"将 {subject} 在不同层之间进行适配、转换或组装。"
    if role in {"Factory", "Provider"}:
        return f"创建或提供 {subject} 相关的业务组件和运行时能力。"
    if role in {"Manager", "Registry", "Store", "Holder"}:
        return f"管理 {subject} 相关的运行时状态、注册信息或临时数据。"
    if role == "UseCase":
        return f"定义 {subject} 相关用例的输入、授权和业务结果。"
    if role in {"Parser", "Codec"}:
        return f"解析或编解码 {subject} 相关的外部内容和领域数据。"
    if role in {"Storage", "Worker"}:
        return f"负责 {subject} 相关数据的存储或后台处理。"
    if role == "Creator":
        return f"根据输入配置创建 {subject} 相关的流程节点或业务组件。"
    if role == "Builder":
        return f"构建 {subject} 相关的对象、流程或运行时配置。"
    if role == "Node":
        return f"执行 {subject} 相关流程节点的输入处理和状态转移。"
    if role == "Agent":
        return f"编排 {subject} 相关的智能体任务和模型协作。"
    if role == "Graph":
        return f"表示 {subject} 相关流程的节点、边和执行关系。"
    if role in {"Utils", "Util", "Helper"}:
        return f"提供 {subject} 相关的通用辅助操作，供业务和基础设施复用。"
    if role == "Lock":
        return f"协调 {subject} 相关共享资源的互斥访问和释放。"
    if role == "Sink":
        return f"接收并处理 {subject} 相关的业务事件或统计数据。"
    if role == "Admission":
        return f"校验并控制 {subject} 相关请求是否允许进入处理流程。"
    if role in {"Definition", "Condition", "Execution", "State", "Status", "Result", "Entry", "Edge"}:
        return f"表示 {subject} 相关流程中的状态、关系或执行数据。"
    if role == "DO":
        return f"表示 {subject} 对应的持久化数据对象及其数据库字段。"
    if role in {"Router", "Resolver"}:
        return f"根据请求上下文解析或路由 {subject} 相关的处理能力。"
    if role in {"Interceptor", "Validator", "Policy"}:
        return f"校验或约束 {subject} 相关的请求、状态和访问规则。"
    if role == "Port":
        return f"定义 {subject} 领域与外部能力交互的端口契约。"
    if role in {"Event", "Exception"}:
        return f"表示 {subject} 相关的领域事件或异常信息。"
    if type_info.kind == "interface":
        return f"定义 {subject} 相关的协作契约和调用边界。"
    if ".persistence.dataobject." in owner_path or role == "DO":
        return f"表示 {subject} 对应的持久化数据对象及其数据库字段。"
    if ".contract." in owner_path:
        return f"定义 {subject} 所属领域对外协作所需的稳定契约。"
    if ".application." in owner_path:
        return f"编排 {subject} 所属应用流程的输入、协作和业务结果。"
    if ".domain." in owner_path:
        return f"实现 {subject} 所属领域的业务规则和状态变化。"
    if ".infrastructure." in owner_path:
        return f"提供 {subject} 所属基础设施的适配、存储或运行支持。"
    if ".web." in owner_path:
        return f"承载 {subject} 所属 Web 能力的请求适配和边界处理。"
    return f"实现 {subject} 相关的业务处理、协作逻辑或基础设施能力。"


def find_doc_match(source: str, offset: int) -> DocMatch | None:
    candidate = None
    for match in SCANNER.iter_javadocs(source, offset):
        candidate = match
    if candidate is None:
        return None
    if not SCANNER.annotation_lines_only(source[candidate.end() : offset]):
        return None
    return DocMatch(candidate.start(), candidate.end(), candidate.group(0))


def indentation(source: str, offset: int) -> str:
    line_start = source.rfind("\n", 0, offset) + 1
    segment = source[line_start:offset]
    if not segment.strip():
        line_end = source.find("\n", line_start)
        if line_end < 0:
            line_end = len(source)
        segment = source[line_start:line_end]
    return re.match(r"[ \t]*", segment).group(0)


def preserve_tags(block: str) -> list[str]:
    tags: list[str] = []
    for raw in block.splitlines():
        line = re.sub(r"^\s*\*?\s?", "", raw).strip()
        if not line.startswith("@"):
            continue
        name = line[1:].split(None, 1)[0]
        if name in PRESERVED_TAGS:
            tags.append(line)
        elif name == "param" and line.startswith("@param <") and line.strip() != "@param <T>":
            tags.append(line)
    return tags


def render_doc(description: str, tags: list[str], indent: str) -> str:
    lines = [f"{indent}/**", f"{indent} * {description}"]
    if tags:
        lines.append(f"{indent} *")
        lines.extend(f"{indent} * {tag}" for tag in tags)
    lines.append(f"{indent} */")
    return "\n".join(lines)


def doc_replacement_span(source: str, match: DocMatch) -> tuple[int, str]:
    """Return the complete source line and its indentation for a Javadoc block.

    ``DocMatch.start`` points at ``/`` rather than at the leading whitespace.
    Replacing from that offset while also rendering indentation would duplicate
    indentation on every existing comment.  Replacing the whole line keeps
    nested types and methods aligned with their declaration.
    """
    line_start = source.rfind("\n", 0, match.start) + 1
    return line_start, indentation(source, match.start)


def annotation_block_start_before(source: str, offset: int) -> int | None:
    """Find an annotation block immediately preceding ``offset``.

    Java allows multiline annotations.  Walking the contiguous non-blank
    lines backwards lets us move a Javadoc block before both one-line and
    multiline annotations without treating an earlier member as part of the
    block.
    """
    line_start = source.rfind("\n", 0, offset) + 1
    cursor = line_start - 1
    block_start: int | None = None
    while cursor >= 0:
        previous_start = source.rfind("\n", 0, cursor) + 1
        line = source[previous_start:cursor].strip()
        if not line:
            if block_start is None:
                cursor = previous_start - 1
                continue
            break
        if line.startswith("@"):
            block_start = previous_start
            cursor = previous_start - 1
            continue
        if block_start is not None:
            cursor = previous_start - 1
            continue
        # 这些是注解起始行之前的多行注解续行，例如属性赋值或右括号。
        if line.endswith(("(", ")", ",")) or "=" in line:
            cursor = previous_start - 1
            continue
        break
    return block_start


def split_parameters(text: str) -> list[str]:
    result: list[str] = []
    start = 0
    angle = paren = bracket = 0
    for index, char in enumerate(text):
        if char == "<":
            angle += 1
        elif char == ">":
            angle = max(0, angle - 1)
        elif char == "(":
            paren += 1
        elif char == ")":
            paren = max(0, paren - 1)
        elif char == "[":
            bracket += 1
        elif char == "]":
            bracket = max(0, bracket - 1)
        elif char == "," and angle == paren == bracket == 0:
            result.append(text[start:index])
            start = index + 1
    if text[start:].strip():
        result.append(text[start:])
    return result


def method_parameter_names(source: str, masked: str, method: SCANNER.MethodInfo) -> list[str]:
    opening = masked.find("(", method.declaration_offset)
    if opening < 0:
        return []
    closing = SCANNER.matching(masked, opening, "(", ")")
    if closing is None:
        return []
    result: list[str] = []
    for raw in split_parameters(source[opening + 1 : closing]):
        value = SCANNER.normalize_text(ANNOTATION_RE.sub(" ", raw))
        value = re.sub(r"\b(?:final|volatile|transient)\b", " ", value)
        identifiers = IDENTIFIER_RE.findall(value)
        if identifiers:
            result.append(identifiers[-1])
    return result


def parameter_description(name: str) -> str:
    lower = name.lower()
    if lower in {"actor", "context", "actorcontext"}:
        return "当前操作主体上下文，用于确定租户、用户和访问权限。"
    if lower.endswith("tenantid") or lower == "tenant":
        return "当前操作涉及的租户标识。"
    if lower.endswith("userid") or lower == "user":
        return "当前操作涉及的用户标识。"
    if lower in {"pageno", "page"}:
        return "分页页码，从 1 开始。"
    if lower in {"pagesize", "size", "limit"}:
        return "每页返回的数据数量。"
    if lower == "request" or lower.endswith("request"):
        return "封装本次操作所需业务字段的请求对象。"
    if lower.endswith("id"):
        subject = subject_for(name[:-2]) if name[:-2] else "目标业务对象"
        return f"用于定位{subject}的标识。"
    if lower in {"key", "code", "name"}:
        return "用于定位或筛选目标业务对象的业务值。"
    if lower.endswith("ids"):
        return "待处理的业务对象标识集合。"
    if lower in {"query", "filter", "criteria", "condition"}:
        return "用于筛选目标数据的查询条件。"
    if lower in {"event", "message", "command", "payload"}:
        return "本次流程携带的事件或业务数据。"
    return f"用于完成本次业务处理的 {name} 参数。"


def method_subject(method: SCANNER.MethodInfo) -> str:
    owner = method.owner.rsplit(".", 1)[-1]
    return subject_for(owner)


def method_description(method: SCANNER.MethodInfo, return_type: str) -> str:
    name = method.name
    subject = method_subject(method)
    lower = name.lower()
    if lower in {"current", "getcurrent"}:
        action = "获取当前"
    elif lower in {"require", "required"} or lower.startswith("require"):
        action = "获取并校验"
    elif lower.startswith(("page", "list", "search", "query", "find", "select", "get", "by", "detail", "options")):
        action = "查询"
    elif lower.startswith(("create", "add", "save", "insert", "register", "open")):
        action = "创建或保存"
    elif lower.startswith(("update", "modify", "set", "enable", "disable", "assign")):
        action = "更新或设置"
    elif lower.startswith(("delete", "remove", "clear", "close")):
        action = "删除或移除"
    elif lower.startswith(("validate", "check", "is", "has", "can", "should")):
        action = "校验或判断"
    elif lower.startswith(("publish", "send", "emit", "notify")):
        action = "发布或发送"
    elif lower.startswith(("invoke", "call", "execute")):
        action = "调用"
    elif lower.startswith(("build", "assemble", "convert", "to")):
        action = "构建或转换"
    elif lower.startswith(("handle", "process", "on", "consume")):
        action = "处理"
    elif lower.startswith(("route", "resolve", "choose")):
        action = "解析或路由"
    else:
        action = "执行"
    if "void" in return_type or return_type.strip() == "":
        return f"{action} {subject} 相关业务操作，并维护必要的状态和协作关系。"
    return f"{action} {subject} 相关业务数据，并返回处理结果。"


def method_return_type(method: SCANNER.MethodInfo) -> str:
    signature = method.signature
    name_index = signature.find(method.name)
    if name_index < 0:
        return ""
    prefix = signature[:name_index]
    prefix = re.sub(r"\b(public|protected|private|static|final|abstract|default|synchronized|native|strictfp)\b", " ", prefix)
    return SCANNER.normalize_text(prefix)


def return_description(method: SCANNER.MethodInfo, return_type: str) -> str | None:
    if return_type in {"", "void"}:
        return None
    if "Pair" in return_type:
        return "返回总数及当前页数据，左值为总数，右值为数据列表。"
    if "List" in return_type or "Set" in return_type or "Collection" in return_type:
        return "返回符合条件的数据集合；没有匹配项时返回空集合。"
    if "Optional" in return_type:
        return "返回可能存在的业务对象；不存在时返回空值容器。"
    if return_type in {"boolean", "Boolean"}:
        return "返回本次条件判断是否成立。"
    return f"返回 {method_subject(method)} 相关操作生成的结果数据。"


def method_needs_rewrite(source: str, masked: str, method: SCANNER.MethodInfo) -> tuple[bool, DocMatch | None]:
    doc_match = find_doc_match(source, method.declaration_offset)
    if doc_match is None:
        return False, None
    doc = SCANNER.parse_doc(0, doc_match.block)
    return_type = method_return_type(method)
    generated_description = method_description(method, return_type)
    if SCANNER.normalize_text(doc.description) == SCANNER.normalize_text(generated_description):
        return True, doc_match
    # 重新处理脚本旧版本生成的描述，使动作和参数措辞的改进能够覆盖既有结果。
    if re.search(
        r"相关业务(?:数据，并返回处理结果|操作，并维护必要的状态和协作关系)。$|"
        r"相关操作生成的结果数据。$",
        SCANNER.normalize_text(doc.description),
    ):
        return True, doc_match
    if SCANNER.category_for(doc.description, "method", method.name):
        return True, doc_match
    for description in doc.params.values():
        if SCANNER.category_for(description, "param"):
            return True, doc_match
    if doc.returns is not None and SCANNER.category_for(doc.returns, "return"):
        return True, doc_match
    if any(SCANNER.category_for(value, "throws") for value in doc.throws.values()):
        return True, doc_match
    return False, doc_match


def rewrite_file(
    path: Path, rewrite_all_types: bool = True, write: bool = True
) -> tuple[bool, int, int]:
    source = path.read_text(encoding="utf-8")
    masked = SCANNER.mask_java(source)
    source_set = SCANNER.source_set(path)
    replacements: list[tuple[int, int, str]] = []
    type_count = method_count = 0
    for type_info in SCANNER.discover_types(source, masked):
        match = find_doc_match(source, type_info.declaration_offset)
        if match is None and not rewrite_all_types:
            continue
        description = type_description(type_info, source_set)
        tags = preserve_tags(match.block) if match else []
        if match:
            annotation_start = annotation_block_start_before(source, match.start)
            line_start, _ = doc_replacement_span(source, match)
            doc_start = annotation_start if annotation_start is not None else line_start
            indent = (
                indentation(source, annotation_start)
                if annotation_start is not None
                else indentation(source, type_info.declaration_offset)
            )
            rendered = render_doc(description, tags, indent)
            if annotation_start is not None:
                prefix = source[annotation_start:match.start].rstrip()
                rendered = rendered + "\n" + prefix
                replacements.append((annotation_start, match.end, rendered))
            else:
                replacements.append((line_start, match.end, rendered))
        else:
            line_start = source.rfind("\n", 0, type_info.declaration_offset) + 1
            annotation_start = annotation_block_start_before(source, type_info.declaration_offset)
            insert_at = annotation_start if annotation_start is not None else line_start
            indent = indentation(source, insert_at)
            replacements.append((insert_at, insert_at, render_doc(description, [], indent) + "\n"))
        type_count += 1
        for method in SCANNER.discover_methods(source, masked, type_info):
            should_rewrite, method_match = method_needs_rewrite(source, masked, method)
            if not should_rewrite or method_match is None:
                continue
            names = method_parameter_names(source, masked, method)
            return_type = method_return_type(method)
            tags = preserve_tags(method_match.block)
            tags = [tag for tag in tags if not tag.startswith("@throws")]
            description = method_description(method, return_type)
            lines = [description]
            for name in names:
                lines.append(f"@param {name} {parameter_description(name)}")
            result_description = return_description(method, return_type)
            if result_description:
                lines.append(f"@return {result_description}")
            indent = indentation(source, method.declaration_offset)
            rendered = render_doc(lines[0], lines[1:] + tags, indent)
            annotation_start = annotation_block_start_before(source, method_match.start)
            line_start, _ = doc_replacement_span(source, method_match)
            if annotation_start is not None:
                prefix = source[annotation_start:method_match.start].rstrip()
                rendered = rendered + "\n" + prefix
                replacements.append((annotation_start, method_match.end, rendered))
            else:
                replacements.append((line_start, method_match.end, rendered))
            method_count += 1
    if not replacements:
        return False, type_count, method_count
    updated = source
    for start, end, replacement in sorted(replacements, reverse=True):
        updated = updated[:start] + replacement + updated[end:]
    updated = re.sub(r"(?m)^[ \t]+(?=\r?$)", "", updated)
    if updated == source:
        return False, type_count, method_count
    if write:
        path.write_text(updated, encoding="utf-8", newline="\n")
    return True, type_count, method_count


def main() -> int:
    parser = argparse.ArgumentParser(description="批量重写 Java 类型和已知模板 Javadoc")
    parser.add_argument("paths", nargs="+", help="源码目录或文件")
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()
    changed_files = changed_types = changed_methods = 0
    for path in SCANNER.iter_java_files([Path(value) for value in args.paths]):
        changed, type_count, method_count = rewrite_file(path, write=not args.dry_run)
        if changed:
            changed_files += 1
        changed_types += type_count
        changed_methods += method_count
        if changed and args.dry_run:
            print(SCANNER.module_relative(path))
    print(f"类型注释处理 {changed_types} 个，方法注释处理 {changed_methods} 个，修改文件 {changed_files} 个。")
    return 0


if __name__ == "__main__":
    sys.exit(main())

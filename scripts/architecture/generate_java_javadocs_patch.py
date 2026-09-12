"""Generate an apply_patch patch for missing production Java Javadocs.

The script is intentionally read-only: it analyzes source files and writes a
patch to stdout.  It documents type responsibilities and class-level fields;
it does not change Java declarations or annotations.
"""

from __future__ import annotations

import difflib
import re
import sys
from pathlib import Path


TYPE_RE = re.compile(r"\b(class|interface|enum|record)\s+([A-Za-z_$][\w$]*)")
MODIFIER_RE = re.compile(
    r"^(?:public|protected|private|static|final|transient|volatile|"
    r"synchronized|native|strictfp)\b"
)

DOMAIN_LABELS = {
    "education": "教育",
    "agent": "智能体",
    "conversation": "会话",
    "knowledge": "知识",
    "model": "模型",
    "tooling": "工具",
    "governance": "治理",
    "identity": "身份",
    "billing": "计费",
    "platform": "平台",
    "common": "平台基础设施",
    "kernel": "共享内核",
    "bootstrap": "启动",
    "web": "Web",
}

FIELD_LABELS = {
    "id": "标识",
    "ids": "标识集合",
    "tenant": "租户",
    "user": "用户",
    "student": "学生",
    "teacher": "教师",
    "course": "课程",
    "chapter": "章节",
    "section": "小节",
    "subject": "学科",
    "question": "题目",
    "exam": "考试",
    "score": "分数",
    "name": "名称",
    "title": "标题",
    "description": "描述",
    "content": "内容",
    "code": "编码",
    "type": "类型",
    "status": "状态",
    "state": "状态",
    "version": "版本",
    "order": "顺序",
    "orderno": "序号",
    "created": "创建",
    "updated": "更新",
    "deleted": "删除",
    "time": "时间",
    "at": "时间",
    "date": "日期",
    "url": "地址",
    "path": "路径",
    "key": "键",
    "value": "值",
    "config": "配置",
    "properties": "配置属性",
    "repository": "仓储",
    "service": "服务",
    "mapper": "映射器",
    "client": "客户端",
    "engine": "引擎",
    "provider": "提供者",
    "handler": "处理器",
    "manager": "管理器",
    "store": "存储",
    "factory": "工厂",
    "template": "模板",
    "resolver": "解析器",
    "listener": "监听器",
    "publisher": "发布器",
    "executor": "执行器",
    "scheduler": "调度器",
    "port": "端口",
    "token": "令牌",
    "role": "角色",
    "permission": "权限",
    "tenantid": "租户标识",
    "studentid": "学生标识",
    "courseid": "课程标识",
    "questionid": "题目标识",
    "examid": "考试标识",
    "parentid": "父级标识",
    "input": "输入",
    "output": "输出",
    "result": "结果",
    "error": "错误",
    "message": "消息",
    "metadata": "元数据",
    "metadatastore": "元数据存储",
    "enabled": "启用开关",
    "limit": "上限",
    "size": "大小",
    "count": "数量",
    "weight": "权重",
    "level": "级别",
    "ratio": "比例",
    "cost": "成本",
    "amount": "数量",
    "currency": "货币",
    "source": "来源",
    "target": "目标",
    "request": "请求",
    "response": "响应",
    "context": "上下文",
    "policy": "策略",
    "rules": "规则",
    "options": "选项",
    "data": "数据",
    "items": "条目",
    "list": "列表",
    "map": "映射",
    "lock": "锁",
    "resource": "资源",
    "file": "文件",
    "directory": "目录",
    "root": "根目录",
    "jdbc": "JDBC",
    "redis": "Redis",
    "database": "数据库",
    "connection": "连接",
    "graph": "图结构",
    "index": "索引",
    "dimension": "维度",
    "vector": "向量",
    "env": "环境变量",
    "property": "属性",
    "pattern": "匹配模式",
    "channel": "通道",
    "server": "服务",
    "days": "天数",
    "retention": "保留策略",
    "baseline": "基线",
    "seed": "种子",
    "profile": "配置档案",
    "marker": "标记",
    "reservation": "预留记录",
    "prompt": "提示词",
    "estimated": "预计",
    "generation": "生成",
    "usage": "用量",
    "quota": "配额",
    "audit": "审计",
    "execution": "执行",
    "task": "任务",
    "embedding": "嵌入向量",
}


def strip_code(text: str) -> str:
    """Remove comments and literals while retaining line and brace shape."""

    output: list[str] = []
    index = 0
    state = "normal"
    while index < len(text):
        char = text[index]
        following = text[index + 1] if index + 1 < len(text) else ""
        if state == "normal":
            if char == "/" and following == "*":
                state = "block"
                output.extend("  ")
                index += 2
            elif char == "/" and following == "/":
                while index < len(text) and text[index] != "\n":
                    output.append(" ")
                    index += 1
            elif char == '"':
                state = "string"
                output.append(" ")
                index += 1
            elif char == "'":
                state = "char"
                output.append(" ")
                index += 1
            else:
                output.append(char)
                index += 1
        elif state == "block":
            if char == "*" and following == "/":
                state = "normal"
                output.extend("  ")
                index += 2
            else:
                output.append("\n" if char == "\n" else " ")
                index += 1
        elif state == "string":
            if char == "\\":
                output.extend("  ")
                index += 2
            elif char == '"':
                state = "normal"
                output.append(" ")
                index += 1
            else:
                output.append("\n" if char == "\n" else " ")
                index += 1
        else:
            if char == "\\":
                output.extend("  ")
                index += 2
            elif char == "'":
                state = "normal"
                output.append(" ")
                index += 1
            else:
                output.append("\n" if char == "\n" else " ")
                index += 1
    return "".join(output)


def has_javadoc(lines: list[str], index: int) -> bool:
    def annotation_block_only(block: list[str]) -> bool:
        """判断声明前的内容是否只有空白和注解块。"""

        parenthesis_depth = 0
        saw_annotation = False
        for raw_line in block:
            line = raw_line.strip()
            if not line:
                continue
            if parenthesis_depth > 0:
                parenthesis_depth += line.count("(") - line.count(")")
                if parenthesis_depth < 0:
                    return False
                continue
            if not line.startswith("@"):
                return False
            saw_annotation = True
            parenthesis_depth = line.count("(") - line.count(")")
        return saw_annotation and parenthesis_depth == 0

    for end in range(index - 1, max(-1, index - 120), -1):
        if not lines[end].strip().endswith("*/"):
            continue
        start = end
        while start >= 0 and "/**" not in lines[start]:
            start -= 1
        if start < 0:
            continue
        between = lines[end + 1 : index]
        if not between or annotation_block_only(between):
            return True
    return False


def insertion_index(lines: list[str], index: int) -> int:
    cursor = index
    while cursor > 0 and lines[cursor - 1].strip().startswith("@"):
        cursor -= 1
    return cursor


def domain_label(path: Path) -> str:
    for part in path.as_posix().split("/"):
        if part in DOMAIN_LABELS:
            return DOMAIN_LABELS[part]
    return "平台"


def type_description(kind: str, name: str, path: Path) -> str:
    domain = domain_label(path)
    reference = "{@code " + name + "}"
    if kind == "interface":
        return f"{reference} 定义{domain}模块的协作契约，约束调用方可使用的能力和边界。"
    if kind == "enum":
        return f"{reference} 表示{domain}模块中的一组受控业务状态或分类。"
    if kind == "record":
        return f"{reference} 封装{domain}模块中不可变的结构化数据，并作为相关操作之间的值对象。"
    if name.endswith("Controller"):
        return f"{reference} 是{domain}模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。"
    if name.endswith("ServiceImpl"):
        return f"{reference} 实现{domain}模块的应用服务，负责编排用例流程并维护业务边界。"
    if name.endswith("Service"):
        return f"{reference} 定义{domain}模块的应用服务能力，供上层用例调用。"
    if name.endswith("RepositoryImpl"):
        return f"{reference} 实现{domain}模块的持久化端口，负责在领域对象与存储模型之间转换。"
    if name.endswith("Repository"):
        return f"{reference} 定义{domain}模块的持久化端口，隔离领域逻辑与具体存储实现。"
    if name.endswith("DO"):
        return f"{reference} 是{domain}模块的持久化对象，承载数据库记录与映射字段。"
    if name.endswith("BO"):
        return f"{reference} 是{domain}模块的业务对象，承载用例处理所需的领域数据。"
    if name.endswith("DTO"):
        return f"{reference} 是{domain}模块的数据传输对象，用于边界之间传递结构化数据。"
    if name.endswith("Request"):
        return f"{reference} 表示{domain}模块的请求参数，承载调用方提交的输入数据。"
    if name.endswith("Response"):
        return f"{reference} 表示{domain}模块的响应数据，承载返回给调用方的结果。"
    if name.endswith(("Properties", "Config", "Configuration")):
        return f"{reference} 提供{domain}模块的配置项，并集中声明其默认值和运行约束。"
    if name.endswith("Mapper"):
        return f"{reference} 负责{domain}模块持久化对象与数据库记录之间的映射。"
    if name.endswith("Exception"):
        return f"{reference} 表示{domain}模块中的业务异常，用于向调用方传递失败原因。"
    if name.endswith("Module"):
        return f"{reference} 声明{domain}模块的装配入口和模块边界。"
    if name.endswith(("Factory", "Creator")):
        return f"{reference} 负责创建{domain}模块中的运行时对象，并集中封装构造规则。"
    if name.endswith(("Node", "Agent")):
        return f"{reference} 承载{domain}模块中的智能流程节点，负责执行本节点的输入处理与结果产出。"
    return f"{reference} 承载{domain}模块的领域状态或协作行为，负责维护本类型的职责边界。"


def field_description(name: str) -> str:
    key = name.lower()
    if key in FIELD_LABELS:
        return f"{FIELD_LABELS[key]}，表示当前对象中的对应属性。"
    tokens = re.findall(r"[A-Z]+(?=[A-Z][a-z]|\b)|[A-Z]?[a-z]+|\d+", name)
    labels = [FIELD_LABELS.get(token.lower()) for token in tokens]
    if labels and all(labels):
        return "".join(labels) + "，表示当前对象中的对应属性。"
    return f"{name} 属性，保存当前对象中的业务数据或协作依赖。"


def process(path: Path) -> str | None:
    original = path.read_text(encoding="utf-8")
    lines = original.splitlines()
    code = strip_code(original).splitlines()
    code.extend([""] * (len(lines) - len(code)))
    depth = 0
    type_stack: list[int] = []
    pending_type_body = False
    pending_field: str | None = None
    field_start: int | None = None
    inserts: dict[int, list[str]] = {}

    for index, code_line in enumerate(code):
        declarations = list(TYPE_RE.finditer(code_line))
        for match in declarations:
            if re.search(r"\bnew\s*$", code_line[: match.start()]):
                continue
            kind, name = match.group(1), match.group(2)
            if not has_javadoc(lines, index):
                target = insertion_index(lines, index)
                indent = re.match(r"\s*", lines[index]).group(0)
                inserts.setdefault(target, []).extend(
                    [indent + "/**", indent + " * " + type_description(kind, name, path), indent + " */"]
                )
            if "{" in code_line:
                type_stack.append(depth + 1)
            else:
                pending_type_body = True

        if pending_type_body and "{" in code_line and not declarations:
            type_stack.append(depth + 1)
            pending_type_body = False

        if type_stack and depth == type_stack[-1]:
            if pending_field is None:
                candidate = re.sub(
                    r"^\s*(?:@[A-Za-z_$][\w$]*(?:\([^;]*?\))?\s*)+", "", code_line
                ).strip()
                if (
                    candidate
                    and "(" not in candidate
                    and not candidate.startswith(
                        ("@", "*", "/", "package ", "import ", "class ", "interface ", "enum ", "record ")
                    )
                    and not re.search(r"\b(?:class|interface|enum|record)\b", candidate)
                ):
                    pending_field = candidate
                    field_start = index
            else:
                pending_field += " " + code_line.strip()

            if pending_field and ";" in pending_field:
                statement = pending_field.split(";", 1)[0].strip() + ";"
                start = field_start
                pending_field = None
                field_start = None
                if (
                    start is not None
                    and "(" not in statement
                    and ")" not in statement
                    and not statement.startswith(
                        ("return ", "throw ", "assert ", "if ", "for ", "while ", "switch ", "case ", "do ", "class ", "interface ", "enum ", "record ", "new ")
                    )
                ):
                    field_match = re.match(
                        r"(?:(?:public|protected|private|static|final|transient|volatile|synchronized|native|strictfp)\s+)*"
                        r"(?:[\w$?<>., \[\]]+)\s+([A-Za-z_$][\w$]*(?:\s*,\s*[A-Za-z_$][\w$]*)*)\s*(?:=.*)?;",
                        statement,
                    )
                    if field_match:
                        names = [item.strip() for item in field_match.group(1).split(",")]
                        first_word = statement.split()[0] if statement.split() else ""
                        explicit_modifier = bool(MODIFIER_RE.match(statement))
                        if (
                            first_word != "throws"
                            and (explicit_modifier or any(not item.isupper() for item in names))
                            and not has_javadoc(lines, start)
                        ):
                            target = insertion_index(lines, start)
                            indent = re.match(r"\s*", lines[start]).group(0)
                            inserts.setdefault(target, []).extend(
                                [indent + "/**", indent + " * " + field_description(names[0]), indent + " */"]
                            )

        depth += code_line.count("{") - code_line.count("}")
        depth = max(depth, 0)
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
    prefix = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("modules")
    chunks: list[str] = []
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

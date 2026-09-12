"""Generate an apply_patch patch that documents Java record components."""

from __future__ import annotations

import difflib
import re
from pathlib import Path


LABELS = {
    "id": "标识",
    "tenantId": "租户标识",
    "userId": "用户标识",
    "studentId": "学生标识",
    "courseId": "课程标识",
    "questionId": "题目标识",
    "examId": "考试标识",
    "name": "名称",
    "title": "标题",
    "content": "内容",
    "value": "值",
    "code": "编码",
    "type": "类型",
    "status": "状态",
    "source": "来源",
    "target": "目标",
    "description": "描述",
    "score": "分数",
    "input": "输入",
    "output": "输出",
    "result": "结果",
    "request": "请求",
    "response": "响应",
    "context": "上下文",
    "metadata": "元数据",
    "token": "令牌",
    "tokens": "令牌数量",
    "variables": "变量集合",
    "uri": "资源地址",
    "path": "路径",
    "size": "大小",
    "createdAt": "创建时间",
    "updatedAt": "更新时间",
    "expiresAt": "过期时间",
    "pageNumber": "页码",
    "pageSize": "页大小",
    "namespace": "命名空间",
    "dimension": "向量维度",
    "dataDir": "数据目录",
    "vector": "向量",
    "role": "角色",
    "contentType": "内容类型",
    "filename": "文件名",
    "fileName": "文件名",
    "ownerUserId": "所属用户标识",
    "runId": "运行标识",
    "estimatedTokens": "预计令牌数",
    "durationMs": "耗时毫秒数",
    "valid": "是否有效",
    "errors": "错误列表",
    "required": "是否必填",
    "kind": "类别",
    "model": "模型",
    "provider": "提供方",
    "temperature": "采样温度",
    "maxTokens": "最大令牌数",
    "stop": "停止序列",
    "messages": "消息列表",
    "parameters": "参数映射",
}


def component_names(text: str, match: re.Match[str]) -> list[str]:
    """Extract component names while ignoring commas in generic types."""

    position = match.end()
    depth = 1
    while position < len(text) and depth:
        if text[position] == "(":
            depth += 1
        elif text[position] == ")":
            depth -= 1
        position += 1
    body = text[match.end() : position - 1]
    parts: list[str] = []
    current: list[str] = []
    angle_depth = 0
    paren_depth = 0
    bracket_depth = 0
    for char in body:
        if char == "<":
            angle_depth += 1
        elif char == ">" and angle_depth:
            angle_depth -= 1
        elif char == "(":
            paren_depth += 1
        elif char == ")" and paren_depth:
            paren_depth -= 1
        elif char == "[":
            bracket_depth += 1
        elif char == "]" and bracket_depth:
            bracket_depth -= 1
        if char == "," and angle_depth == paren_depth == bracket_depth == 0:
            parts.append("".join(current))
            current = []
        else:
            current.append(char)
    if current:
        parts.append("".join(current))
    names: list[str] = []
    for part in parts:
        identifiers = re.findall(r"[A-Za-z_$][\w$]*", part)
        if identifiers:
            names.append(identifiers[-1])
    return names


def doc_bounds(lines: list[str], declaration_line: int) -> tuple[int, int] | None:
    end = declaration_line - 1
    while end >= 0 and (not lines[end].strip() or lines[end].strip().startswith("@")):
        end -= 1
    if end < 0 or not lines[end].strip().endswith("*/"):
        return None
    start = end
    while start >= 0 and end - start < 120:
        if "/**" in lines[start]:
            return start, end
        start -= 1
    return None


def description(name: str) -> str:
    return LABELS.get(name, name + " 属性") + "，表示该记录组件承载的数据。"


def main() -> int:
    chunks: list[str] = []
    for path in Path("modules").rglob("*.java"):
        if "src/main/java" not in path.as_posix() or "target" in path.parts:
            continue
        original = path.read_text(encoding="utf-8")
        lines = original.splitlines()
        changes: list[tuple[int, int, list[str]]] = []
        for match in re.finditer(r"\brecord\s+([A-Za-z_$][\w$]*)\s*\(", original):
            declaration_line = original[: match.start()].count("\n")
            names = component_names(original, match)
            bounds = doc_bounds(lines, declaration_line)
            if not names or not bounds:
                continue
            start, end = bounds
            current_doc = "\n".join(lines[start : end + 1])
            missing = [name for name in names if "@param " + name not in current_doc]
            if not missing:
                continue
            indent = re.match(r"\s*", lines[start]).group(0)
            if start == end:
                body = lines[start].strip()[3:-2].strip()
                replacement = [indent + "/**", indent + " * " + body, indent + " *"]
                replacement.extend(indent + " * @param " + name + " " + description(name) for name in missing)
                replacement.append(indent + " */")
            else:
                replacement = lines[start : end + 1]
                replacement[-1:-1] = [
                    indent + " * @param " + name + " " + description(name) for name in missing
                ]
            changes.append((start, end, replacement))
        for start, end, replacement in sorted(changes, reverse=True):
            lines[start : end + 1] = replacement
        if changes:
            updated = "\n".join(lines) + "\n"
            chunks.extend(
                difflib.unified_diff(
                    original.splitlines(True),
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

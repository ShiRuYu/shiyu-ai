#!/usr/bin/env python3
"""Generate API, database, menu and permission reference documents.

The script only uses Python's standard library. By default it reads the live
SpringDoc document from http://127.0.0.1:9000/v3/api-docs. A checked-in JSON
snapshot can be supplied with --openapi-file for offline generation.
"""

from __future__ import annotations

import argparse
import csv
import json
import re
import urllib.request
from collections import defaultdict
from pathlib import Path
from typing import Any


HTTP_METHODS = {"get", "post", "put", "delete", "patch", "head", "options"}
PUBLIC_PATHS = {
    "/auth/login",
    "/auth/register",
    "/auth/code-login",
    "/auth/forget-password",
    "/auth/refresh",
    "/auth/captcha",
}


def md(value: Any) -> str:
    return str(value if value is not None else "-").replace("|", "\\|").replace("\n", " ")


def schema_name(schema: dict[str, Any] | None) -> str:
    if not schema:
        return "-"
    if "$ref" in schema:
        return schema["$ref"].rsplit("/", 1)[-1]
    if "allOf" in schema:
        return " & ".join(schema_name(item) for item in schema["allOf"])
    if "oneOf" in schema:
        return " oneOf ".join(schema_name(item) for item in schema["oneOf"])
    if "anyOf" in schema:
        return " anyOf ".join(schema_name(item) for item in schema["anyOf"])
    schema_type = schema.get("type", "object")
    if schema_type == "array":
        return f"array<{schema_name(schema.get('items'))}>"
    if schema.get("enum"):
        return f"{schema_type}({','.join(map(str, schema['enum']))})"
    fmt = schema.get("format")
    return f"{schema_type}/{fmt}" if fmt else schema_type


def load_openapi(args: argparse.Namespace) -> dict[str, Any]:
    if args.openapi_file:
        return json.loads(Path(args.openapi_file).read_text(encoding="utf-8"))
    with urllib.request.urlopen(args.openapi_url, timeout=20) as response:
        return json.load(response)


def request_summary(operation: dict[str, Any]) -> str:
    entries = []
    for parameter in operation.get("parameters", []):
        required = "必填" if parameter.get("required") else "可选"
        entries.append(
            f"{parameter.get('name')}[{parameter.get('in')},{required}]:{schema_name(parameter.get('schema'))}"
        )
    request_body = operation.get("requestBody")
    if request_body:
        content = request_body.get("content", {})
        variants = []
        for content_type, media in content.items():
            variants.append(f"{content_type}:{schema_name(media.get('schema'))}")
        required = "必填" if request_body.get("required") else "可选"
        entries.append(f"body[{required}]=" + "/".join(variants))
    return "; ".join(entries) or "-"


def response_summary(operation: dict[str, Any]) -> str:
    entries = []
    for status, response in operation.get("responses", {}).items():
        variants = []
        for content_type, media in response.get("content", {}).items():
            variants.append(f"{content_type}:{schema_name(media.get('schema'))}")
        entries.append(f"{status}=" + ("/".join(variants) or response.get("description", "-")))
    return "; ".join(entries) or "-"


def generate_api_reference(spec: dict[str, Any], output: Path, source: str) -> tuple[int, int]:
    grouped: dict[str, list[tuple[str, str, dict[str, Any]]]] = defaultdict(list)
    operation_count = 0
    for path, path_item in sorted(spec.get("paths", {}).items()):
        for method, operation in path_item.items():
            if method.lower() not in HTTP_METHODS:
                continue
            operation_count += 1
            tags = operation.get("tags") or ["未分组"]
            grouped[tags[0]].append((method.upper(), path, operation))

    lines = [
        "# API 接口参考",
        "",
        "> 本文档由 `scripts/docs/generate_reference_docs.py` 从 SpringDoc OpenAPI 自动生成。",
        f"> 生成源：`{md(source)}`；OpenAPI：`{md(spec.get('openapi'))}`；服务版本：`{md(spec.get('info', {}).get('version'))}`。",
        "",
        "## 契约约定",
        "",
        "- 浏览器开发环境使用 `/api` 作为 Vite 代理前缀；后端控制器路径本身不包含 `/api`。",
        "- 除登录、注册、验证码等公开入口外，请求使用 `Authorization: Bearer <accessToken>`。",
        "- 普通 JSON 接口通常返回 `Result<T>`；流式接口按 OpenAPI 标注返回 SSE 或二进制内容。",
        "- `requestBody` 与响应栏保留 OpenAPI schema 名称，具体字段见“组件模型”。",
        "",
        "## 接口清单",
        "",
    ]
    for tag in sorted(grouped):
        lines.extend(
            [
                f"### {tag}",
                "",
                "| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |",
                "|---|---|---|---|---|---|",
            ]
        )
        for method, path, operation in grouped[tag]:
            is_public = path in PUBLIC_PATHS or path.startswith("/captcha/")
            lines.append(
                "| {} | `{}` | {} | {} | {} | {} |".format(
                    method,
                    md(path),
                    md(operation.get("summary") or operation.get("operationId")),
                    "公开" if is_public else "登录态；细粒度权限见权限矩阵",
                    md(request_summary(operation)),
                    md(response_summary(operation)),
                )
            )
        lines.append("")

    lines.extend(
        [
            "## 组件模型",
            "",
            "| Schema | 类型 | 必填字段 | 字段定义 |",
            "|---|---|---|---|",
        ]
    )
    schemas = spec.get("components", {}).get("schemas", {})
    for name, schema in sorted(schemas.items()):
        required = set(schema.get("required", []))
        fields = []
        for field_name, field_schema in schema.get("properties", {}).items():
            marker = "*" if field_name in required else ""
            fields.append(f"{field_name}{marker}:{schema_name(field_schema)}")
        lines.append(
            f"| `{md(name)}` | {md(schema_name(schema))} | {md(', '.join(sorted(required)) or '-')} | {md('; '.join(fields) or '-')} |"
        )
    lines.extend(
        [
            "",
            "## 维护与验证",
            "",
            "```powershell",
            "python scripts/docs/generate_reference_docs.py --openapi-url http://127.0.0.1:9000/v3/api-docs",
            "```",
            "",
            "生成后应运行前端 `pnpm run test:contract`，确保前端方法与路径仍被该 OpenAPI 契约覆盖。",
            "",
        ]
    )
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text("\n".join(lines), encoding="utf-8")
    return len(spec.get("paths", {})), operation_count


def domain_for(table: str) -> str:
    prefix = table.split("_", 1)[0]
    return {
        "COMMON": "公共基础",
        "STORAGE": "对象存储",
        "AUTH": "认证授权",
        "AGENT": "智能体编排",
        "MEMORY": "记忆",
        "KNOWLEDGE": "知识平台",
        "EDU": "教育",
        "RECORD": "成长记录",
        "VECTOR": "向量检索",
        "OBSERVATION": "可观测性",
    }.get(prefix, "其他")


def parse_tables(schema_files: list[Path]) -> list[dict[str, Any]]:
    tables: list[dict[str, Any]] = []
    pattern = re.compile(
        r'CREATE\s+(?:CACHED\s+)?TABLE\s+"PUBLIC"\."(?P<name>[^"]+)"(?P<tail>.*?)\n\);',
        re.I | re.S,
    )
    for file in schema_files:
        text = file.read_text(encoding="utf-8")
        for match in pattern.finditer(text):
            name = match.group("name")
            tail = match.group("tail")
            table_comment = "-"
            comment_match = re.match(r"\s+COMMENT\s+'([^']*)'", tail, re.I)
            if comment_match:
                table_comment = comment_match.group(1)
            body_start = tail.find("(")
            body = tail[body_start + 1 :] if body_start >= 0 else tail
            columns = []
            constraints = []
            for raw_line in body.splitlines():
                line = raw_line.strip().rstrip(",")
                column_match = re.match(r'"([^"]+)"\s+(.+)', line)
                if column_match:
                    column_name, definition = column_match.groups()
                    type_match = re.match(
                        r"(.+?)(?=\s+(?:GENERATED|DEFAULT|COMMENT|NOT\s+NULL|NULL|PRIMARY|UNIQUE|REFERENCES|CHECK)\b|$)",
                        definition,
                        re.I,
                    )
                    column_type = type_match.group(1) if type_match else definition
                    comment = "-"
                    column_comment = re.search(r"COMMENT\s+'([^']*)'", definition, re.I)
                    if column_comment:
                        comment = column_comment.group(1)
                    attrs = []
                    if re.search(r"\bNOT\s+NULL\b", definition, re.I):
                        attrs.append("NOT NULL")
                    default_match = re.search(
                        r"\bDEFAULT\s+(.+?)(?=\s+COMMENT\b|\s+NOT\s+NULL\b|\s+ON\s+UPDATE\b|$)",
                        definition,
                        re.I,
                    )
                    if default_match:
                        attrs.append("DEFAULT " + default_match.group(1))
                    columns.append((column_name, column_type, "; ".join(attrs) or "-", comment))
                elif line.upper().startswith("CONSTRAINT"):
                    constraints.append(line)
            tables.append(
                {
                    "name": name,
                    "comment": table_comment,
                    "domain": domain_for(name),
                    "source": file.name,
                    "columns": columns,
                    "constraints": constraints,
                }
            )
    return tables


def generate_data_dictionary(tables: list[dict[str, Any]], output: Path) -> None:
    by_domain: dict[str, list[dict[str, Any]]] = defaultdict(list)
    for table in tables:
        by_domain[table["domain"]].append(table)
    lines = [
        "# 领域模型与数据字典",
        "",
        "> 本文档由 H2 最终基线 SQL 自动生成；基线目录是数据库结构的事实来源。字段注释中的历史乱码来自既有 SQL 注释，不影响字段名、类型和约束。",
        "",
        "## 数据建模约定",
        "",
        "- 业务表通常使用 `TENANT_ID` 做租户边界，`STATUS` 表示启停，`DEL_FLAG` 表示逻辑删除。",
        "- `CREATE_BY/CREATE_TIME/UPDATE_BY/UPDATE_TIME` 为审计字段。",
        "- 表间关系主要由应用服务与 `*_ID` 字段维护；基线中并非所有逻辑关系都声明数据库外键。",
        "- 知识、教育、Agent、成长记录分别独立建模，通过租户、空间、用户和知识点标识关联。",
        "",
        "## 表级总览",
        "",
        "| 领域 | 表 | 说明 | 字段数 | 基线文件 |",
        "|---|---|---|---:|---|",
    ]
    for table in tables:
        lines.append(
            f"| {md(table['domain'])} | `{table['name']}` | {md(table['comment'])} | {len(table['columns'])} | `{table['source']}` |"
        )
    lines.append("")
    for domain in sorted(by_domain):
        lines.extend([f"## {domain}", ""])
        for table in sorted(by_domain[domain], key=lambda item: item["name"]):
            lines.extend(
                [
                    f"### {table['name']}",
                    "",
                    f"说明：{table['comment']}。来源：`{table['source']}`。",
                    "",
                    "| 字段 | 类型 | 约束/默认值 | 注释 |",
                    "|---|---|---|---|",
                ]
            )
            for column_name, column_type, attrs, comment in table["columns"]:
                lines.append(f"| `{column_name}` | `{md(column_type)}` | {md(attrs)} | {md(comment)} |")
            if table["constraints"]:
                lines.extend(["", "表约束："])
                for constraint in table["constraints"]:
                    lines.append(f"- `{md(constraint)}`")
            lines.append("")
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text("\n".join(lines), encoding="utf-8")


def sql_values(fragment: str) -> list[str]:
    reader = csv.reader([fragment], delimiter=",", quotechar="'", skipinitialspace=True)
    return [item.strip() for item in next(reader)]


def seed_rows(text: str, table: str) -> list[list[str]]:
    pattern = re.compile(
        rf'INSERT\s+INTO\s+"PUBLIC"\."{re.escape(table)}"\s+VALUES\((.*?)\);',
        re.I | re.S,
    )
    return [sql_values(match.group(1)) for match in pattern.finditer(text)]


def canonical_menu_rows(text: str) -> list[list[str]]:
    """Read v3 menu rows from the MERGE statement in 05_navigation.sql."""
    rows = []
    for line in text.splitlines():
        value = line.strip()
        if not re.match(r"^\(20\d{2},", value):
            continue
        value = value[1:]
        if value.endswith(");"):
            value = value[:-2]
        elif value.endswith(",") or value.endswith(")"):
            value = value[:-1]
        rows.append(sql_values(value))
    return rows


def parse_controller_permissions(repo: Path) -> list[dict[str, str]]:
    rows = []
    mapping_pattern = re.compile(r"@(Get|Post|Put|Delete|Patch)Mapping(?:\s*\((.*?)\))?", re.S)
    for file in repo.rglob("*Controller.java"):
        if "target" in file.parts:
            continue
        text = file.read_text(encoding="utf-8")
        class_pos = re.search(r"\bclass\s+\w+Controller\b", text)
        if not class_pos:
            continue
        class_annotations = text[: class_pos.start()]
        class_paths = re.findall(r'@RequestMapping\s*\([^)]*?"([^"]*)"', class_annotations, re.S)
        class_path = class_paths[-1] if class_paths else ""
        class_permissions = re.findall(r'@SaCheckPermission\s*\(\s*"([^"]+)"', class_annotations)
        class_permission = class_permissions[-1] if class_permissions else "-"
        for match in mapping_pattern.finditer(text[class_pos.end() :]):
            method = match.group(1).upper()
            args = match.group(2) or ""
            path_match = re.search(r'"([^"]*)"', args)
            local_path = path_match.group(1) if path_match else ""
            after = text[class_pos.end() + match.end() : class_pos.end() + match.end() + 1200]
            method_decl = re.search(r"\b(?:public|protected|private)\s+[^;{]+\(", after)
            if not method_decl:
                continue
            between_previous = text[max(class_pos.end(), class_pos.end() + match.start() - 500) : class_pos.end() + match.start()]
            method_permissions = re.findall(r'@SaCheckPermission\s*\(\s*"([^"]+)"', between_previous)
            permission = method_permissions[-1] if method_permissions else class_permission
            full_path = "/" + "/".join(part.strip("/") for part in [class_path, local_path] if part.strip("/"))
            rows.append(
                {
                    "method": method,
                    "path": full_path if full_path != "/" else "/",
                    "permission": permission,
                    "source": file.name,
                }
            )
    return rows


def generate_permission_matrix(repo: Path, seed_file: Path, navigation_file: Path, output: Path) -> tuple[int, int]:
    seed = seed_file.read_text(encoding="utf-8")
    navigation = navigation_file.read_text(encoding="utf-8")
    roles = {int(row[0]): {"code": row[1], "name": row[2]} for row in seed_rows(seed, "AUTH_ROLE")}
    menus = {}
    system_menu_ids = {1, 2, 3, 4, 5, 7, 11, 90}
    menu_rows = [row for row in seed_rows(seed, "AUTH_MENU") if int(row[0]) in system_menu_ids]
    menu_rows.extend(canonical_menu_rows(navigation))
    for row in menu_rows:
        menus[int(row[0])] = {
            "name": row[1],
            "code": row[2],
            "type": row[3],
            "parent": row[4],
            "path": row[6],
            "component": row[9],
            "show": row[15],
        }
    auth_codes = {int(row[0]): {"code": row[1], "name": row[2]} for row in seed_rows(seed, "AUTH_AUTH_CODE")}
    menu_roles: dict[int, set[int]] = defaultdict(set)
    for row in seed_rows(seed, "AUTH_ROLE_SCOPE_MENU"):
        menu_roles[int(row[1])].add(int(row[0]))
    code_roles: dict[int, set[int]] = defaultdict(set)
    for row in seed_rows(seed, "AUTH_ROLE_SCOPE_AUTH_CODE"):
        code_roles[int(row[1])].add(int(row[0]))
    permission_to_roles = {
        value["code"]: [roles[role_id]["code"] for role_id in sorted(code_roles.get(code_id, set())) if role_id in roles]
        for code_id, value in auth_codes.items()
    }
    endpoints = parse_controller_permissions(repo)

    lines = [
        "# 菜单、角色与权限矩阵",
        "",
        "> 系统菜单与角色来自 `02_auth.sql`，平台菜单来自 `05_navigation.sql`，接口权限来自 Controller 的 `@SaCheckPermission`。运行期管理员可修改授权，因此本表描述“空库初始化基线”，不是某一运行库的实时快照。",
        "",
        "## 权限判定链路",
        "",
        "`用户 → 租户作用域角色 → 角色菜单/权限码 → 前端可见性 + 后端注解鉴权`。菜单只控制导航可达性，真正的安全边界必须由后端权限注解和租户过滤共同保证。",
        "",
        "## 初始角色",
        "",
        "| ID | 角色编码 | 名称 |",
        "|---:|---|---|",
    ]
    for role_id, role in sorted(roles.items()):
        lines.append(f"| {role_id} | `{md(role['code'])}` | {md(role['name'])} |")
    lines.extend(
        [
            "",
            "## 菜单与角色",
            "",
            "| 菜单 ID | 类型 | 名称 | 路径 | 前端组件 | 显示 | 初始角色 |",
            "|---:|---|---|---|---|---|---|",
        ]
    )
    for menu_id, menu in sorted(menus.items()):
        assigned_roles = menu_roles.get(menu_id, set()) or ({1, 2, 3} if menu_id >= 2000 else set())
        assigned = ", ".join(roles[role_id]["code"] for role_id in sorted(assigned_roles) if role_id in roles)
        lines.append(
            f"| {menu_id} | {md(menu['type'])} | {md(menu['name'])} | `{md(menu['path'])}` | `{md(menu['component'])}` | {md(menu['show'])} | {md(assigned or '-')} |"
        )
    lines.extend(
        [
            "",
            "## 权限码与角色",
            "",
            "| 权限 ID | 权限码 | 名称 | 初始角色 |",
            "|---:|---|---|---|",
        ]
    )
    for code_id, code in sorted(auth_codes.items()):
        assigned = ", ".join(permission_to_roles.get(code["code"], []))
        lines.append(f"| {code_id} | `{md(code['code'])}` | {md(code['name'])} | {md(assigned or '-')} |")
    lines.extend(
        [
            "",
            "## 后端接口权限",
            "",
            "| 方法 | 路径 | 权限码 | 具备该码的初始角色 | 源文件 |",
            "|---|---|---|---|---|",
        ]
    )
    for endpoint in sorted(endpoints, key=lambda item: (item["path"], item["method"])):
        permission = endpoint["permission"]
        assigned = ", ".join(permission_to_roles.get(permission, [])) if permission != "-" else "仅登录态/服务内校验"
        lines.append(
            f"| {endpoint['method']} | `{md(endpoint['path'])}` | `{md(permission)}` | {md(assigned or '-')} | `{endpoint['source']}` |"
        )
    lines.extend(
        [
            "",
            "## 审核规则",
            "",
            "- 页面按钮即使隐藏，写操作仍须有后端 `@SaCheckPermission`；没有注解的接口应在安全审计中逐项说明其自服务或内部接口理由。",
            "- 新增菜单时必须同时校验组件文件存在、父级菜单存在、角色菜单关联存在。",
            "- 新增权限码时必须同时校验种子定义、角色关联、Controller 注解和前端按钮码拼写一致。",
            "",
        ]
    )
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text("\n".join(lines), encoding="utf-8")
    return len(menus), len(auth_codes)


def main() -> None:
    script = Path(__file__).resolve()
    repo = script.parents[2]
    parser = argparse.ArgumentParser()
    parser.add_argument("--openapi-url", default="http://127.0.0.1:9000/v3/api-docs")
    parser.add_argument("--openapi-file")
    args = parser.parse_args()

    spec = load_openapi(args)
    source = args.openapi_file or args.openapi_url
    paths, operations = generate_api_reference(spec, repo / "docs/参考/API接口参考.md", source)

    schema_dir = repo / "infrastructure/shiyu-ai-dal/src/main/resources/db/baseline/h2/schema"
    tables = parse_tables(sorted(schema_dir.glob("*.sql")))
    generate_data_dictionary(tables, repo / "docs/参考/领域模型与数据字典.md")

    seed_file = repo / "infrastructure/shiyu-ai-dal/src/main/resources/db/baseline/h2/seed/02_auth.sql"
    navigation_file = repo / "infrastructure/shiyu-ai-dal/src/main/resources/db/baseline/h2/seed/05_navigation.sql"
    menus, auth_codes = generate_permission_matrix(repo, seed_file, navigation_file, repo / "docs/参考/菜单角色权限矩阵.md")
    print(
        f"generated: paths={paths}, operations={operations}, tables={len(tables)}, "
        f"menus={menus}, auth_codes={auth_codes}"
    )


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""Verify the complete shiyu-ai/shiyu-ui documentation delivery."""

from __future__ import annotations

import json
import os
import re
import sys
from pathlib import Path


BACKEND_REQUIRED = {
    "项目介绍.md": ["项目是什么", "核心能力", "当前边界"],
    "技术文档.md": ["总体架构", "API 契约", "数据与存储", "前后端契约审计"],
    "使用文档.md": ["环境准备", "第一次构建和启动", "与前端联调", "生产检查清单"],
    "参考/API接口参考.md": ["接口清单", "组件模型"],
    "参考/领域模型与数据字典.md": ["表级总览", "认证授权", "知识平台"],
    "参考/菜单角色权限矩阵.md": ["菜单与角色", "权限码与角色", "后端接口权限"],
    "参考/错误码手册.md": ["通用错误码", "业务错误码", "特殊响应"],
    "安全与租户隔离审计.md": ["请求安全链路", "租户隔离", "上线前强制清单"],
    "部署运维手册.md": ["构建与启动", "备份与恢复", "发布与回滚"],
    "质量与联调报告.md": ["验证结果", "核心联调断言", "已知非阻断项"],
    "文档导航.md": ["了解项目", "开发参考", "上线与质量"],
}

FRONTEND_REQUIRED = {
    "项目介绍.md": ["产品导航", "主要功能", "与后端的关系"],
    "技术文档.md": ["路由和动态菜单", "请求层", "认证、租户和权限", "测试策略"],
    "使用文档.md": ["登录和工作上下文", "功能使用", "生产构建和部署"],
    "页面操作手册.md": ["登录与工作台", "Agent 平台", "知识引擎", "系统与文件管理"],
    "参考/页面路由与角色清单.md": ["完整清单", "使用规则"],
    "部署发布与联调.md": ["发布门禁", "生产部署", "联调冒烟清单"],
    "文档导航.md": ["产品与使用", "开发与发布", "后端契约"],
}


def markdown_files(root: Path) -> list[Path]:
    files: list[Path] = []
    for current, dirs, names in os.walk(root, followlinks=False):
        current_path = Path(current)
        dirs[:] = [
            name
            for name in dirs
            if name not in {".git", "node_modules", "target", "dist"}
            and not (current_path / name).is_symlink()
        ]
        files.extend(current_path / name for name in names if name.endswith(".md"))
    return files


def verify_required(root: Path, required: dict[str, list[str]], failures: list[str]) -> None:
    for relative, phrases in required.items():
        path = root / relative
        if not path.is_file():
            failures.append(f"missing document: {path}")
            continue
        text = path.read_text(encoding="utf-8")
        for phrase in phrases:
            if phrase not in text:
                failures.append(f"missing section '{phrase}': {path}")


def verify_links(files: list[Path], failures: list[str]) -> int:
    checked = 0
    pattern = re.compile(r"\[[^\]]*\]\(([^)]+)\)")
    for path in files:
        for target in pattern.findall(path.read_text(encoding="utf-8")):
            if target.startswith(("http://", "https://", "mailto:", "#")):
                continue
            target = target.split("#", 1)[0]
            if not target:
                continue
            checked += 1
            if not (path.parent / target).resolve().exists():
                failures.append(f"broken link: {path} -> {target}")
    return checked


def count_openapi_operations(spec: dict) -> int:
    methods = {"get", "post", "put", "delete", "patch", "head", "options"}
    return sum(method in methods for item in spec.get("paths", {}).values() for method in item)


def verify_final_baseline(backend: Path, failures: list[str]) -> int:
    baseline = backend / "infrastructure/shiyu-ai-dal/src/main/resources/db/baseline/h2"
    modifying = re.compile(r"^\s*(ALTER|UPDATE|DELETE|MERGE|DROP|TRUNCATE)\b", re.I | re.M)
    secondary_statements = 0
    for path in baseline.rglob("*.sql"):
        matches = modifying.findall(path.read_text(encoding="utf-8"))
        secondary_statements += len(matches)
        if matches:
            failures.append(f"secondary SQL statements in final baseline: {path} -> {', '.join(matches)}")
    migration_root = backend / "infrastructure/shiyu-ai-dal/src/main/resources/db/migration"
    migration_files = list(migration_root.rglob("*.sql")) if migration_root.exists() else []
    for path in migration_files:
        failures.append(f"separate migration SQL remains: {path}")
    return secondary_statements + len(migration_files)


def verify_counts(backend: Path, frontend: Path, failures: list[str]) -> dict[str, int]:
    snapshot = frontend / "tests/contracts/shiyu-ai-openapi.json"
    spec = json.loads(snapshot.read_text(encoding="utf-8"))
    paths = len(spec.get("paths", {}))
    operations = count_openapi_operations(spec)
    api_doc = (backend / "docs/参考/API接口参考.md").read_text(encoding="utf-8")
    documented_operations = len(re.findall(r"^\| (?:GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS) \|", api_doc, re.M))
    if documented_operations != operations:
        failures.append(f"API operation count mismatch: document={documented_operations}, snapshot={operations}")

    schema_dir = backend / "infrastructure/shiyu-ai-dal/src/main/resources/db/baseline/h2/schema"
    table_pattern = re.compile(r'CREATE\s+(?:CACHED\s+)?TABLE\s+"PUBLIC"\."', re.I)
    tables = sum(len(table_pattern.findall(path.read_text(encoding="utf-8"))) for path in schema_dir.glob("*.sql"))
    data_doc = (backend / "docs/参考/领域模型与数据字典.md").read_text(encoding="utf-8")
    documented_tables = len(re.findall(r"^### [A-Z][A-Z0-9_]+$", data_doc, re.M))
    if documented_tables != tables:
        failures.append(f"table count mismatch: document={documented_tables}, schema={tables}")

    seed = (backend / "infrastructure/shiyu-ai-dal/src/main/resources/db/baseline/h2/seed/02_auth.sql").read_text(encoding="utf-8")
    menus = len(re.findall(r'INSERT\s+INTO\s+"PUBLIC"\."AUTH_MENU"\s+VALUES', seed, re.I))
    auth_codes = len(re.findall(r'INSERT\s+INTO\s+"PUBLIC"\."AUTH_AUTH_CODE"\s+VALUES', seed, re.I))
    route_doc = (frontend / "docs/参考/页面路由与角色清单.md").read_text(encoding="utf-8")
    documented_menus = len(re.findall(r"^\| \d+ \| (?:MENU|CATALOG) \|", route_doc, re.M))
    missing_components = len(re.findall(r"\| 缺失 \|$", route_doc, re.M))
    if documented_menus != menus:
        failures.append(f"menu count mismatch: document={documented_menus}, seed={menus}")
    if missing_components:
        failures.append(f"menu components missing: {missing_components}")
    permission_doc = (backend / "docs/参考/菜单角色权限矩阵.md").read_text(encoding="utf-8")
    permission_section = permission_doc.split("## 权限码与角色", 1)[1].split("## 后端接口权限", 1)[0]
    documented_codes = len(re.findall(r"^\| \d+ \| `[^`]+` \|", permission_section, re.M))
    if documented_codes != auth_codes:
        failures.append(f"permission count mismatch: document={documented_codes}, seed={auth_codes}")
    return {"paths": paths, "operations": operations, "tables": tables, "menus": menus, "auth_codes": auth_codes}


def main() -> None:
    backend = Path(__file__).resolve().parents[2]
    frontend = backend.parent / "shiyu-ui"
    failures: list[str] = []
    verify_required(backend / "docs", BACKEND_REQUIRED, failures)
    verify_required(frontend / "docs", FRONTEND_REQUIRED, failures)
    files = markdown_files(backend / "docs") + markdown_files(frontend / "docs")
    files.extend([backend / "README.md", frontend / "README.md"])
    links = verify_links(files, failures)
    counts = verify_counts(backend, frontend, failures)
    secondary_sql = verify_final_baseline(backend, failures)
    print(
        "documentation summary: "
        f"documents={len(files)}, links={links}, paths={counts['paths']}, operations={counts['operations']}, "
        f"tables={counts['tables']}, menus={counts['menus']}, auth_codes={counts['auth_codes']}, "
        f"secondary_sql={secondary_sql}"
    )
    if failures:
        print(f"documentation failures ({len(failures)}):", file=sys.stderr)
        for failure in failures:
            print(f"  {failure}", file=sys.stderr)
        raise SystemExit(1)
    print("Documentation verification passed.")


if __name__ == "__main__":
    main()

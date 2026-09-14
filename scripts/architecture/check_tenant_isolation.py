"""检查租户隔离入口、Flex 租户字段和受控跨租户豁免。"""

from __future__ import annotations

import argparse
import json
import re
import sys
from collections import defaultdict
from dataclasses import asdict, dataclass
from pathlib import Path

from check_mixed_package_roles import classify_source, iter_java_files


@dataclass(frozen=True)
class SourceReport:
    path: str
    package: str
    roles: tuple[str, ...]
    has_tenant_mapping: bool
    has_explicit_sql: bool


@dataclass(frozen=True)
class Violation:
    path: str
    kind: str
    message: str


def _package(text: str) -> str:
    match = re.search(r"^\s*package\s+([^;]+);", text, flags=re.MULTILINE)
    return match.group(1).strip() if match else ""


def _is_test(path: Path) -> bool:
    normalized = path.as_posix()
    return "/src/test/" in normalized


def _relative(path: Path, root: Path) -> str:
    try:
        return path.resolve().relative_to(root.resolve()).as_posix()
    except ValueError:
        return path.resolve().as_posix()


def scan(paths: list[Path], bypass_allowlist: set[str] | None = None) -> tuple[list[SourceReport], list[Violation]]:
    """返回职责清单和隔离门禁违规。

    当前门禁阻断直接关闭 Flex 租户过滤、注册额外 ``TenantFactory``，以及
    domain 包修改租户作用域。原生 SQL/JDBC 只建立清单，由真实数据库测试和人工审计
    验证，不把字符串扫描误报当成隔离正确。
    """

    allowlist = {Path(value).resolve().as_posix() for value in (bypass_allowlist or set())}
    reports: list[SourceReport] = []
    violations: list[Violation] = []
    package_roles: dict[str, set[str]] = defaultdict(set)

    for path in iter_java_files(paths):
        try:
            text = path.read_text(encoding="utf-8")
        except (OSError, UnicodeDecodeError):
            continue
        package = _package(text)
        if not package:
            continue
        roles = tuple(sorted(classify_source(text, path.as_posix())))
        if not _is_test(path):
            package_roles[package].update(roles)
        reports.append(
            SourceReport(
                path=path.resolve().as_posix(),
                package=package,
                roles=roles,
                has_tenant_mapping=bool(
                    re.search(r"@Column\s*\([^)]*\btenantId\s*=\s*true\b", text)
                    or re.search(r"implements\s+TenantEntity\b|extends\s+TenantEntity\b", text)
                ),
                has_explicit_sql=bool(
                    re.search(r"@(?:Select|Update|Delete|Insert)\b", text)
                    or re.search(r"\b(?:JdbcTemplate|NamedParameterJdbcTemplate)\b", text)
                ),
            )
        )

        normalized_path = path.resolve().as_posix()
        if _is_test(path):
            continue
        scope_mutation = re.search(
            r"\bTenantScope\s*\.\s*(?:set|withTenant|clear)\s*\(", text
        ) or re.search(
            r"import\s+static\s+com\.shiyu\.ai\.kernel\.context\.TenantScope\s*\.\s*(?:set|withTenant|clear|\*)\s*;", text
        )
        if "domain" in package.split(".") and scope_mutation:
            violations.append(Violation(
                normalized_path, "domain-tenant-scope-mutation",
                "领域层不得修改租户作用域；请在授权后的编排或基础设施入口绑定",
            ))
        infrastructure = "modules/infrastructure/shiyu-common-mybatis/src/main/java/com/shiyu/ai/common/mybatis/"
        controlled_executor = normalized_path.endswith(infrastructure + "tenant/TenantQueryExecutor.java")
        direct_bypass = re.search(
            r"\bTenantManager\s*\.\s*(?:withoutTenantCondition|ignoreTenantCondition)\s*\(", text
        ) or re.search(
            r"import\s+static\s+com\.mybatisflex\.core\.tenant\.TenantManager\.(?:withoutTenantCondition|ignoreTenantCondition|\*)\s*;", text
        )
        if direct_bypass and not controlled_executor and normalized_path not in allowlist:
            violations.append(
                Violation(
                    normalized_path,
                    "direct-tenant-bypass",
                    "直接关闭 Flex 租户过滤；请通过 TenantQueryExecutor 执行",
                )
            )
        defines_factory = re.search(
            r"\b(?:implements|extends)\s+[^{};]*\bTenantFactory\b"
            r"|\bnew\s+(?:com\.mybatisflex\.core\.tenant\.)?TenantFactory\b", text
        )
        registers_factory = re.search(r"\bTenantManager\s*\.\s*setTenantFactory\s*\(", text) or re.search(
            r"import\s+static\s+com\.mybatisflex\.core\.tenant\.TenantManager\s*\.\s*(?:setTenantFactory|\*)\s*;", text
        )
        if defines_factory or registers_factory:
            permitted = (
                (not defines_factory or normalized_path.endswith(infrastructure + "config/ContextTenantFactory.java"))
                and (not registers_factory or normalized_path.endswith(infrastructure + "config/TenantFlexConfig.java"))
            )
            if not permitted:
                violations.append(
                    Violation(
                        normalized_path,
                        "extra-tenant-factory",
                        "普通领域代码不得注册或实现额外 TenantFactory",
                    )
                )

    return sorted(reports, key=lambda item: item.path), violations


def render(reports: list[SourceReport], violations: list[Violation]) -> str:
    payload = {
        "sources": [asdict(report) for report in reports],
        "violations": [asdict(violation) for violation in violations],
        "packages": {
            package: sorted({role for report in reports if report.package == package for role in report.roles})
            for package in sorted({report.package for report in reports})
        },
    }
    return json.dumps(payload, ensure_ascii=False, indent=2)


def main() -> int:
    parser = argparse.ArgumentParser(description="检查 Java 租户隔离入口和豁免")
    parser.add_argument("paths", nargs="+", help="待扫描的源码目录或文件")
    parser.add_argument("--allow-bypass", action="append", default=[], help="允许直接使用 TenantManager 的完整文件路径")
    parser.add_argument("--json", action="store_true", help="输出 JSON 清单")
    args = parser.parse_args()
    reports, violations = scan([Path(path) for path in args.paths], set(args.allow_bypass))
    if args.json:
        print(render(reports, violations))
    else:
        for violation in violations:
            print(f"{violation.kind}: {violation.path}: {violation.message}")
        print(f"租户隔离清单：{len(reports)} 个 Java 文件，{len({report.package for report in reports})} 个包")
    if violations:
        print(f"租户隔离门禁失败：{len(violations)} 个违规。")
        return 1
    print("租户隔离门禁通过：未发现未受控过滤豁免、额外 TenantFactory 或领域层作用域修改。")
    return 0


if __name__ == "__main__":
    sys.exit(main())

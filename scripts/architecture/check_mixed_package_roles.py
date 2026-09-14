"""扫描 Java 包中的职责混放，并输出可审计的包角色报告。"""

from __future__ import annotations

import argparse
import re
import sys
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path


EXCLUDED_PARTS = {
    ".git",
    ".idea",
    "target",
    "generated",
    "build",
    "dist",
}

ROLE_ORDER = (
    "controller",
    "configuration",
    "path-contributor",
    "handler/listener",
    "service",
    "port/interface",
    "model",
    "transport",
    "adapter",
    "persistence",
    "component",
)

# 这些组合代表职责边界已经发生混合；单纯的接口与模型共存不在此列表中，
# 因为契约包经常需要同时承载接口、record 和 enum。
CONFLICTS = {
    frozenset({"controller", "configuration"}),
    frozenset({"controller", "path-contributor"}),
    frozenset({"controller", "handler/listener"}),
    frozenset({"controller", "component"}),
    frozenset({"controller", "service"}),
    frozenset({"configuration", "handler/listener"}),
    frozenset({"configuration", "service"}),
    frozenset({"configuration", "component"}),
    frozenset({"model", "service"}),
    frozenset({"model", "adapter"}),
    frozenset({"model", "component"}),
    frozenset({"port/interface", "service"}),
    frozenset({"port/interface", "adapter"}),
    frozenset({"port/interface", "component"}),
    frozenset({"adapter", "service"}),
    frozenset({"service", "persistence"}),
}

P0_CONFLICTS = {conflict for conflict in CONFLICTS if "controller" in conflict}


@dataclass(frozen=True)
class PackageReport:
    """描述一个 Java 包中检测到的职责集合。"""

    package: str
    roles: frozenset[str]
    files: tuple[Path, ...]

    @property
    def conflicts(self) -> tuple[tuple[str, str], ...]:
        pairs: list[tuple[str, str]] = []
        ordered = [role for role in ROLE_ORDER if role in self.roles]
        for index, left in enumerate(ordered):
            for right in ordered[index + 1 :]:
                if frozenset({left, right}) in CONFLICTS:
                    pairs.append((left, right))
        return tuple(pairs)

    @property
    def mixed_level(self) -> str:
        """返回适合迁移排序的混合等级。"""

        if not self.conflicts:
            return "clean" if len(self.roles) <= 1 else "intentional-aggregation"
        conflict_sets = {frozenset(pair) for pair in self.conflicts}
        return "P0" if conflict_sets & P0_CONFLICTS else "P1"

    @property
    def migration_suggestion(self) -> str:
        """给出职责拆分或保留聚合的下一步建议。"""

        if not self.roles:
            return "未识别生产职责，人工审计"
        if not self.conflicts:
            if len(self.roles) > 1:
                return "保留当前聚合，并在 allowlist 中记录边界理由"
            return "保持当前包"
        role_names = "、".join(role for role in ROLE_ORDER if role in self.roles)
        return f"按职责拆分为独立子包：{role_names}"


def strip_comments(text: str) -> str:
    """移除 Java 注释，避免注释中的注解或类型名称影响职责判断。"""

    text = re.sub(r"/\*.*?\*/", " ", text, flags=re.DOTALL)
    return re.sub(r"//[^\n]*", " ", text)


def declaration_names(code: str) -> list[str]:
    return re.findall(r"\b(?:class|interface|record|enum|@interface)\s+([A-Za-z_$][\w$]*)", code)


def classify_source(text: str, filename: str = "") -> set[str]:
    """根据 Java 类型声明和注解推断一个源文件的主要职责。"""

    code = strip_comments(text)
    names = declaration_names(code)
    roles: set[str] = set()

    if re.search(r"@(?:RestController|Controller)\b", code):
        roles.add("controller")
    if re.search(r"@(?:Configuration|AutoConfiguration)\b", code):
        roles.add("configuration")
    if "PathContributor" in code or re.search(r"\bPathContributor\b", code):
        roles.add("path-contributor")
    if re.search(r"@EventListener\b", code) or any(
        name.endswith(("Handler", "Listener")) for name in names
    ):
        roles.add("handler/listener")
    if re.search(r"@Service\b", code):
        roles.add("service")
    if re.search(r"\binterface\s+[A-Za-z_$][\w$]*", code):
        roles.add("port/interface")
    if re.search(r"\brecord\s+[A-Za-z_$][\w$]*", code) or re.search(
        r"\benum\s+[A-Za-z_$][\w$]*", code
    ):
        roles.add("model")
    if any(name.endswith(("BO", "DO", "VO", "DTO", "Request", "Response", "Properties")) for name in names):
        roles.add("transport" if any(name.endswith(("Request", "Response", "DTO")) for name in names) else "model")
    # Provider 接口属于端口；只有具体 Provider 实现才是 adapter。
    # 这样不会把 VectorStoreProvider、ChatProvider 等契约接口误报为适配器。
    has_interface = bool(re.search(r"\binterface\s+[A-Za-z_$][\w$]*", code))
    if any(name.endswith("Adapter") for name in names) or (
        any(name.endswith("Provider") for name in names) and not has_interface
    ):
        roles.add("adapter")
    if re.search(r"@(?:Repository|Mapper)\b", code) or "persistence" in filename.replace("\\", "/").split("/"):
        roles.add("persistence")
    if re.search(r"@Component\b", code) and not roles:
        roles.add("component")

    return roles


def iter_java_files(paths: list[Path]):
    """遍历输入目录中的 Java 文件，排除构建和生成目录。"""

    seen: set[Path] = set()
    for raw_root in paths:
        root = raw_root.resolve()
        candidates = [root] if root.is_file() else root.rglob("*.java")
        for candidate in candidates:
            if not candidate.is_file() or candidate in seen:
                continue
            if any(part in EXCLUDED_PARTS for part in candidate.parts):
                continue
            seen.add(candidate)
            yield candidate


def scan_paths(paths: list[Path]) -> list[PackageReport]:
    """扫描目录并按 package 聚合职责角色。"""

    package_files: dict[str, list[Path]] = defaultdict(list)
    package_roles: dict[str, set[str]] = defaultdict(set)
    for path in iter_java_files(paths):
        # 测试类用于验证生产代码，不应把 *Test 的类名当成生产职责；
        # 仍然遍历 test 源码，以便发现过期 package 声明和测试包中的混放。
        is_test_source = "\\src\\test\\" in str(path) or "/src/test/" in path.as_posix()
        try:
            text = path.read_text(encoding="utf-8")
        except (OSError, UnicodeDecodeError):
            continue
        match = re.search(r"^\s*package\s+([^;]+);", text, flags=re.MULTILINE)
        if not match:
            continue
        package = match.group(1).strip()
        package_files[package].append(path)
        if not is_test_source:
            package_roles[package].update(classify_source(text, path.as_posix()))

    reports = [
        PackageReport(package, frozenset(package_roles[package]), tuple(sorted(files)))
        for package, files in package_files.items()
    ]
    return sorted(reports, key=lambda report: report.package)


def find_violations(paths: list[Path], allowlist: set[str] | None = None) -> list[PackageReport]:
    """返回存在明确职责冲突且不在 allowlist 中的包。"""

    allowed = allowlist or set()
    return [report for report in scan_paths(paths) if report.package not in allowed and report.conflicts]


def read_allowlist(path: Path) -> set[str]:
    """读取带审计说明的包白名单；空行和 ``#`` 注释会被忽略。"""

    packages: set[str] = set()
    for line in path.read_text(encoding="utf-8").splitlines():
        value = line.split("#", 1)[0].strip()
        if value:
            packages.add(value)
    return packages


def render_report(
    reports: list[PackageReport],
    allowlist: set[str] | None = None,
    include_files: bool = True,
) -> str:
    """将扫描结果渲染为适合审阅的中文报告。"""

    allowed = allowlist or set()
    lines: list[str] = []
    for report in reports:
        roles = ", ".join(role for role in ROLE_ORDER if role in report.roles)
        conflicts = ", ".join(f"{left}+{right}" for left, right in report.conflicts)
        status = "allowlisted" if report.package in allowed else "checked"
        lines.append(
            f"{report.package} | roles={roles or 'unknown'} | level={report.mixed_level}"
            f" | conflicts={conflicts or 'none'} | status={status}"
            f" | suggestion={report.migration_suggestion} | files={len(report.files)}"
        )
        if include_files:
            lines.extend(f"  - {path}" for path in report.files)
    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser(description="检查 Java 包中的职责混放")
    parser.add_argument("paths", nargs="+", help="待扫描的源码目录或文件")
    parser.add_argument("--allow-package", action="append", default=[], help="允许保留混合职责的完整包名")
    parser.add_argument("--allowlist-file", type=Path, help="包含包名和审计说明的白名单文件")
    parser.add_argument(
        "--report",
        action="store_true",
        help="输出全部包的角色分布、混合等级和迁移建议",
    )
    args = parser.parse_args()
    allowlist = set(args.allow_package)
    if args.allowlist_file:
        allowlist.update(read_allowlist(args.allowlist_file))
    all_reports = scan_paths([Path(path) for path in args.paths])
    reports = [report for report in all_reports if report.package not in allowlist and report.conflicts]
    if args.report:
        print(render_report(all_reports, allowlist))
    elif reports:
        print(render_report(reports, allowlist))
    if reports:
        print(f"职责混放扫描发现 {len(reports)} 个包。")
        return 1
    print("职责混放扫描通过：未发现未允许的冲突包。")
    return 0


if __name__ == "__main__":
    sys.exit(main())

"""check domain module dependencies 脚本，执行项目架构与工程校验。"""

from __future__ import annotations

import sys
import re
import xml.etree.ElementTree as ET
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
NS = {"m": "http://maven.apache.org/POM/4.0.0"}


def dependencies(pom: Path) -> list[str]:
    tree = ET.parse(pom)
    return [
        (node.findtext("m:artifactId", default="", namespaces=NS) or "").strip()
        for node in tree.findall("m:dependencies/m:dependency", NS)
    ]


def own_implementation_artifact(pom: Path) -> str | None:
    """执行 own_implementation_artifact，处理输入并返回校验结果。"""
    artifact = ET.parse(pom).getroot().findtext(
        "m:artifactId", default="", namespaces=NS
    ).strip()
    return artifact if artifact.endswith("-implementation") else None


def check_web_adapter_dependencies(root: Path) -> list[str]:
    """执行 check_web_adapter_dependencies，处理输入并返回校验结果。"""
    web_pom = root / "modules/applications/shiyu-ai-web/pom.xml"
    if not web_pom.exists():
        return ["Missing required modules/applications/shiyu-ai-web/pom.xml"]
    allowed = {"shiyu-iam-implementation", "shiyu-agent-implementation"}
    return [
        f"{web_pom.relative_to(root)} -> {artifact}"
        for artifact in dependencies(web_pom)
        if artifact.endswith("-implementation") and artifact not in allowed
    ]


JAVA_DECLARATION = re.compile(
    r"(?m)^(?:public\s+)?(?:abstract\s+|final\s+)?"
    r"(?:class|interface|record|enum)\s+(\w+)"
)
PACKAGE_DECLARATION = re.compile(r"(?m)^package\s+([\w.]+)\s*;")
IMPORT_DECLARATION = re.compile(r"(?m)^import\s+(?:static\s+)?([\w.]+)\s*;")
THREAD_CONTEXT_REFERENCES = re.compile(
    r"\b(?:UserContextHolder|RequestContextHolder|SecurityContextHolder|"
    r"TransactionSynchronizationManager)\b"
)


def implementation_modules(root: Path) -> list[Path]:
    roots = [root / "modules/domains", root / "modules/business"]
    return sorted(
        pom.parent
        for source_root in roots
        if source_root.exists()
        for pom in source_root.glob("*/*/pom.xml")
        if own_implementation_artifact(pom)
    )


def implementation_classes(root: Path) -> dict[str, Path]:
    """执行 implementation_classes，处理输入并返回校验结果。"""
    classes: dict[str, Path] = {}
    for module in implementation_modules(root):
        source_root = module / "src/main/java"
        for source in source_root.rglob("*.java"):
            text = source.read_text(encoding="utf-8", errors="ignore")
            package = PACKAGE_DECLARATION.search(text)
            if not package:
                continue
            for name in JAVA_DECLARATION.findall(text):
                classes[f"{package.group(1)}.{name}"] = module
    return classes


def check_java_imports(root: Path) -> list[str]:
    """执行 check_java_imports，处理输入并返回校验结果。"""
    published = implementation_classes(root)
    violations: list[str] = []
    for source_module in implementation_modules(root):
        source_root = source_module / "src/main/java"
        for source in source_root.rglob("*.java"):
            text = source.read_text(encoding="utf-8", errors="ignore")
            for imported in IMPORT_DECLARATION.findall(text):
                owner = next(
                    (
                        module
                        for qualified_name, module in published.items()
                        if imported == qualified_name
                        or imported.startswith(qualified_name + ".")
                    ),
                    None,
                )
                if owner is not None and owner != source_module:
                    violations.append(
                        f"{source.relative_to(root)} -> {imported} "
                        f"({owner.relative_to(root)})"
                    )
    return violations


def contract_modules(root: Path) -> list[Path]:
    """执行 contract_modules，处理输入并返回校验结果。"""
    roots = [root / "modules/domains", root / "modules/business"]
    return sorted(
        pom.parent
        for source_root in roots
        if source_root.exists()
        for pom in source_root.glob("*/*-contract/pom.xml")
    )


FORBIDDEN_CONTRACT_DEPENDENCY_PREFIXES = (
    "org.springframework",
    "org.mybatis",
    "com.mybatis",
    "org.bsc.langgraph4j",
    "dev.langchain4j",
    "cn.dev33",
    "jakarta.servlet",
    "jakarta.persistence",
)


def check_contract_framework_dependencies(root: Path) -> list[str]:
    """执行 check_contract_framework_dependencies，处理输入并返回校验结果。"""
    violations: list[str] = []
    for module in contract_modules(root):
        pom = module / "pom.xml"
        tree = ET.parse(pom)
        for dependency in tree.findall("m:dependencies/m:dependency", NS):
            group = (dependency.findtext("m:groupId", default="", namespaces=NS) or "").strip()
            artifact = (dependency.findtext("m:artifactId", default="", namespaces=NS) or "").strip()
            coordinate = f"{group}:{artifact}"
            if group.startswith(FORBIDDEN_CONTRACT_DEPENDENCY_PREFIXES):
                violations.append(f"{pom.relative_to(root)} -> {coordinate}")
    return violations


def check_contract_imports(root: Path) -> list[str]:
    """执行 check_contract_imports，处理输入并返回校验结果。"""
    published = implementation_classes(root)
    violations: list[str] = []
    for module in contract_modules(root):
        source_root = module / "src/main/java"
        for source in source_root.rglob("*.java"):
            text = source.read_text(encoding="utf-8", errors="ignore")
            for imported in IMPORT_DECLARATION.findall(text):
                owner = next(
                    (
                        implementation
                        for qualified_name, implementation in published.items()
                        if imported == qualified_name
                        or imported.startswith(qualified_name + ".")
                    ),
                    None,
                )
                if owner is not None:
                    violations.append(
                        f"{source.relative_to(root)} -> {imported} "
                        f"({owner.relative_to(root)})"
                    )
    return violations


def check_thread_context_access(root: Path) -> list[str]:
    """执行 check_thread_context_access，处理输入并返回校验结果。"""
    violations: list[str] = []
    scan_roots = tuple(
        path for path in (root / "modules/domains", root / "modules/applications")
        if path.exists()
    )
    for scan_root in scan_roots:
        for source in scan_root.rglob("*.java"):
            normalized = source.as_posix()
            if "/target/" in normalized or "/src/test/" in normalized:
                continue
            if "/web/" in normalized:
                continue
            text = source.read_text(encoding="utf-8", errors="ignore")
            for match in THREAD_CONTEXT_REFERENCES.finditer(text):
                violations.append(
                    f"{source.relative_to(root)}:{text.count(chr(10), 0, match.start()) + 1}"
                    f" -> {match.group(0)}"
                )
    return violations


def check_migrated_package_ownership(root: Path) -> list[str]:
    """执行 check_migrated_package_ownership，处理输入并返回校验结果。"""
    source_root = root / "modules/domains/conversation/shiyu-conversation-implementation/src/main/java"
    if not source_root.is_dir():
        return []
    violations: list[str] = []
    for source in source_root.rglob("*.java"):
        text = source.read_text(encoding="utf-8", errors="ignore")
        package = PACKAGE_DECLARATION.search(text)
        if package and not package.group(1).startswith("com.shiyu.ai.conversation.implementation"):
            violations.append(
                f"{source.relative_to(root)} -> package {package.group(1)} must be under "
                "com.shiyu.ai.conversation.implementation"
            )
    return violations


def analyze_repository(root: Path) -> list[str]:
    root = root.resolve()
    violations: list[str] = []
    implementations = implementation_modules(root)
    contracts = contract_modules(root)
    if not implementations:
        violations.append("No implementation modules discovered")
    if not contracts:
        violations.append("No contract modules discovered")
    for source_root in (root / "modules/domains", root / "modules/business"):
        if not source_root.exists():
            continue
        for pom in sorted(source_root.rglob("pom.xml")):
            own_implementation = own_implementation_artifact(pom)
            for artifact in dependencies(pom):
                if artifact.endswith("-implementation") and artifact != own_implementation:
                    violations.append(f"{pom.relative_to(root)} -> {artifact}")

    violations.extend(check_web_adapter_dependencies(root))
    violations.extend(check_java_imports(root))
    violations.extend(check_contract_framework_dependencies(root))
    violations.extend(check_contract_imports(root))
    violations.extend(check_thread_context_access(root))
    violations.extend(check_migrated_package_ownership(root))
    return violations


def main() -> int:
    violations = analyze_repository(ROOT)

    if violations:
        print("Cross-domain implementation dependencies are forbidden:")
        print("\n".join(f"- {item}" for item in violations))
        return 1

    print("No cross-domain implementation dependencies found.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

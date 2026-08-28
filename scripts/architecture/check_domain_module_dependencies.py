"""Fail when a bounded-context module depends on another context's implementation.

Domain implementations may consume other domains only through their contract
artifacts.  This check operates on the Maven model rather than Java imports so
the rule remains effective even when a transitive dependency happens to make
the source compile.
"""

from __future__ import annotations

import sys
import re
import xml.etree.ElementTree as ET
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
DOMAINS = ROOT / "shiyu-domains"
APPLICATION = ROOT / "application"
WEB_POM = ROOT / "infrastructure" / "shiyu-ai-web" / "pom.xml"
NS = {"m": "http://maven.apache.org/POM/4.0.0"}


def dependencies(pom: Path) -> list[str]:
    tree = ET.parse(pom)
    return [
        (node.findtext("m:artifactId", default="", namespaces=NS) or "").strip()
        for node in tree.findall("m:dependencies/m:dependency", NS)
    ]


def check_web_adapter_dependencies() -> list[str]:
    """Keep the technical Web adapter from becoming a central domain hub."""
    if not WEB_POM.exists():
        return []
    allowed = {"shiyu-iam-implementation", "shiyu-agent-implementation"}
    return [
        f"{WEB_POM.relative_to(ROOT)} -> {artifact}"
        for artifact in dependencies(WEB_POM)
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


def implementation_modules() -> list[Path]:
    return sorted(
        path
        for path in DOMAINS.glob("*/*-implementation")
        if (path / "src/main/java").is_dir()
    )


def implementation_classes() -> dict[str, Path]:
    """Return the concrete classes published by each implementation module.

    Package names overlap with contract modules (for example model.chat), so
    package-prefix matching would incorrectly reject valid contract imports.
    Exact declared type names let this check distinguish implementation types
    from contract types while still catching nested/static imports.
    """
    classes: dict[str, Path] = {}
    for module in implementation_modules():
        source_root = module / "src/main/java"
        for source in source_root.rglob("*.java"):
            text = source.read_text(encoding="utf-8", errors="ignore")
            package = PACKAGE_DECLARATION.search(text)
            if not package:
                continue
            for name in JAVA_DECLARATION.findall(text):
                classes[f"{package.group(1)}.{name}"] = module
    return classes


def check_java_imports() -> list[str]:
    """Reject direct imports of another bounded context's implementation type."""
    published = implementation_classes()
    violations: list[str] = []
    for source_module in implementation_modules():
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
                        f"{source.relative_to(ROOT)} -> {imported} "
                        f"({owner.relative_to(ROOT)})"
                    )
    return violations


def check_thread_context_access() -> list[str]:
    """Keep domain/application code independent of request/thread context."""
    violations: list[str] = []
    roots = tuple(root for root in (DOMAINS, APPLICATION) if root.exists())
    for root in roots:
        for source in root.rglob("*.java"):
            normalized = source.as_posix()
            if "/target/" in normalized or "/src/test/" in normalized:
                continue
            # Controllers are HTTP adapters; the application and domain layers
            # beneath them must receive ActorContext explicitly instead.
            if "/web/" in normalized:
                continue
            text = source.read_text(encoding="utf-8", errors="ignore")
            for match in THREAD_CONTEXT_REFERENCES.finditer(text):
                violations.append(
                    f"{source.relative_to(ROOT)}:{text.count(chr(10), 0, match.start()) + 1}"
                    f" -> {match.group(0)}"
                )
    return violations


def main() -> int:
    violations: list[str] = []
    for pom in sorted(DOMAINS.rglob("pom.xml")):
        for artifact in dependencies(pom):
            if artifact.endswith("-implementation"):
                own = pom.parent.name
                if artifact != own:
                    violations.append(f"{pom.relative_to(ROOT)} -> {artifact}")

    violations.extend(check_web_adapter_dependencies())
    violations.extend(check_java_imports())
    violations.extend(check_thread_context_access())

    if violations:
        print("Cross-domain implementation dependencies are forbidden:")
        print("\n".join(f"- {item}" for item in violations))
        return 1

    print("No cross-domain implementation dependencies found.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

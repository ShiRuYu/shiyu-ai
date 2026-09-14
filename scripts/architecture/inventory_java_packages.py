"""inventory java packages 脚本，执行项目架构与工程校验。"""

from __future__ import annotations

import argparse
import csv
import re
import sys
import xml.etree.ElementTree as ET
from collections import defaultdict
from dataclasses import dataclass, field
from pathlib import Path


PACKAGE = re.compile(r"(?m)^\s*package\s+([\w.]+)\s*;")
IMPORT = re.compile(r"(?m)^\s*import\s+(?:static\s+)?([\w.*]+)\s*;")
NS = {"m": "http://maven.apache.org/POM/4.0.0"}
CSV_COLUMNS = ("source_path", "module", "old_fqcn", "target_fqcn", "role", "phase")


@dataclass(frozen=True)
class InventoryEntry:
    source_path: str
    module: str
    old_fqcn: str
    target_fqcn: str
    role: str
    phase: str


@dataclass(frozen=True)
class CrossModuleImport:
    source_path: str
    source_module: str
    imported: str
    target_module: str


@dataclass
class Inventory:
    entries: list[InventoryEntry] = field(default_factory=list)
    split_packages: dict[str, list[str]] = field(default_factory=dict)
    duplicate_fqcns: dict[str, list[str]] = field(default_factory=dict)
    cross_module_imports: list[CrossModuleImport] = field(default_factory=list)
    contract_external_dependencies: list[str] = field(default_factory=list)
    manual_review: list[str] = field(default_factory=list)
    path_mismatches: list[str] = field(default_factory=list)
    errors: list[str] = field(default_factory=list)

    @property
    def has_errors(self) -> bool:
        return bool(self.errors or self.duplicate_fqcns or self.path_mismatches)


def _artifact_id(pom: Path) -> str:
    root = ET.parse(pom).getroot()
    artifact = root.findtext("m:artifactId", default="", namespaces=NS).strip()
    if not artifact:
        raise ValueError(f"Missing artifactId: {pom}")
    return artifact


def _role(artifact: str) -> str:
    if artifact.endswith("-contract"):
        return "contract"
    if artifact.endswith("-implementation"):
        return "implementation"
    if artifact == "shiyu-shared-kernel":
        return "shared"
    if artifact.startswith("shiyu-common-"):
        return "infrastructure"
    if artifact in {
        "shiyu-ai-bootstrap",
        "shiyu-platform-bootstrap",
        "shiyu-ai-web",
        "shiyu-application",
    }:
        return "application"
    return "unclassified"


def _phase(module_path: Path, artifact: str) -> str:
    parts = module_path.parts
    if "domains" in parts:
        domain = parts[parts.index("domains") + 1]
        order = {
            "conversation": "3",
            "iam": "4A",
            "knowledge": "4B",
            "memory": "4C",
            "model": "4D",
            "tooling": "4E",
            "education": "4F",
            "governance": "4G",
            "agent": "5",
        }
        return order.get(domain, "manual-review")
    if "business" in parts:
        business = parts[parts.index("business") + 1]
        if business == "education":
            return "4F"
    non_domain = {
        "shiyu-shared-kernel": "6A",
        "shiyu-common-core": "6A",
        "shiyu-common-mybatis": "6B",
        "shiyu-common-web": "6C",
        "shiyu-ai-web": "6C",
        "shiyu-common-storage": "6D",
        "shiyu-common-thread": "6E",
        "shiyu-common-vector": "6F",
        "shiyu-application": "6G",
        "shiyu-ai-bootstrap": "6H",
        "shiyu-platform-bootstrap": "6H",
    }
    return non_domain.get(artifact, "manual-review")


def build_inventory(root: Path) -> Inventory:
    root = root.resolve()
    inventory = Inventory()
    packages: dict[str, set[str]] = defaultdict(set)
    fqcns: dict[str, list[str]] = defaultdict(list)
    fqcn_owners: dict[str, str] = {}
    sources: list[tuple[str, str, str]] = []

    for pom in sorted((root / "modules").rglob("pom.xml")):
        module_path = pom.parent
        source_root = module_path / "src" / "main" / "java"
        if not source_root.is_dir():
            continue
        artifact = _artifact_id(pom)
        if artifact.endswith("-contract"):
            pom_root = ET.parse(pom).getroot()
            for dependency in pom_root.findall("m:dependencies/m:dependency", NS):
                group = dependency.findtext("m:groupId", default="", namespaces=NS).strip()
                dependency_artifact = dependency.findtext(
                    "m:artifactId", default="", namespaces=NS
                ).strip()
                if group and group != "com.shiyu.ai" and dependency_artifact:
                    inventory.contract_external_dependencies.append(
                        f"{artifact} -> {group}:{dependency_artifact}"
                    )
        for source in sorted(source_root.rglob("*.java")):
            text = source.read_text(encoding="utf-8", errors="replace")
            package = PACKAGE.search(text)
            relative = source.relative_to(root).as_posix()
            if package is None:
                inventory.errors.append(f"Missing package declaration: {relative}")
                continue
            package_name = package.group(1)
            expected = source_root / Path(*package_name.split(".")) / source.name
            if source.resolve() != expected.resolve():
                inventory.path_mismatches.append(
                    f"{relative}: package {package_name} expects {expected.relative_to(root).as_posix()}"
                )
            fqcn = f"{package_name}.{source.stem}"
            entry = InventoryEntry(
                source_path=relative,
                module=artifact,
                old_fqcn=fqcn,
                target_fqcn=fqcn,
                role=_role(artifact),
                phase=_phase(module_path.relative_to(root), artifact),
            )
            if entry.role == "unclassified":
                inventory.errors.append(f"Unclassified production module: {artifact} ({relative})")
            if entry.phase == "manual-review":
                inventory.errors.append(f"Unclassified migration phase: {artifact} ({relative})")
            inventory.entries.append(entry)
            packages[package_name].add(artifact)
            fqcns[fqcn].append(relative)
            fqcn_owners.setdefault(fqcn, artifact)
            sources.append((relative, artifact, text))

    inventory.split_packages = {
        package: sorted(modules)
        for package, modules in sorted(packages.items())
        if len(modules) > 1
    }
    inventory.duplicate_fqcns = {
        fqcn: paths for fqcn, paths in sorted(fqcns.items()) if len(paths) > 1
    }
    for source_path, source_module, source_text in sources:
        for imported in IMPORT.findall(source_text):
            if imported.endswith(".*"):
                inventory.manual_review.append(f"{source_path} -> {imported}")
                continue
            target_module = fqcn_owners.get(imported)
            if target_module and target_module != source_module:
                inventory.cross_module_imports.append(
                    CrossModuleImport(source_path, source_module, imported, target_module)
                )
    inventory.contract_external_dependencies.sort()
    inventory.manual_review.sort()
    inventory.path_mismatches.sort()
    return inventory


def write_inventory(inventory: Inventory, destination: Path) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    with destination.open("w", encoding="utf-8", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=CSV_COLUMNS)
        writer.writeheader()
        for entry in sorted(inventory.entries, key=lambda item: item.source_path):
            writer.writerow({column: getattr(entry, column) for column in CSV_COLUMNS})


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    inventory = build_inventory(args.root)
    destination = args.output or args.root / "scripts/architecture/package-migration.csv"
    write_inventory(inventory, destination)
    print(f"Inventoried {len(inventory.entries)} production Java files.")
    print(f"Split packages: {len(inventory.split_packages)}")
    print(f"Duplicate FQCNs: {len(inventory.duplicate_fqcns)}")
    print(f"Cross-module imports: {len(inventory.cross_module_imports)}")
    print(f"Contract external dependencies: {len(inventory.contract_external_dependencies)}")
    print(f"Manual import review: {len(inventory.manual_review)}")
    print(f"Package path mismatches: {len(inventory.path_mismatches)}")
    for mismatch in inventory.path_mismatches:
        print(mismatch, file=sys.stderr)
    for error in inventory.errors:
        print(error, file=sys.stderr)
    return 1 if inventory.has_errors else 0


if __name__ == "__main__":
    sys.exit(main())

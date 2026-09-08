"""Validate that filesystem module directories match Maven coordinates."""

from __future__ import annotations

import sys
import xml.etree.ElementTree as ET
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
NS = {"m": "http://maven.apache.org/POM/4.0.0"}
SCAN_ROOTS = (ROOT / "modules", ROOT / "tests")

LEGACY_PATHS = (
    ROOT / "modules/application",
    ROOT / "modules/applications/bootstrap",
    ROOT / "modules/applications/web",
    ROOT / "modules/applications/composition",
    ROOT / "tests/architecture",
    ROOT / "modules/shared/kernel",
    ROOT / "data",
)


def main() -> int:
    violations: list[str] = []
    for legacy_path in LEGACY_PATHS:
        if legacy_path.exists():
            violations.append(
                f"legacy path must stay absent: {legacy_path.relative_to(ROOT).as_posix()}"
            )
    root_model = ET.parse(ROOT / "pom.xml")
    declared = {node.text.strip() for node in root_model.findall("m:modules/m:module", NS)}
    actual = {
        pom.parent.relative_to(ROOT).as_posix()
        for base in SCAN_ROOTS
        for pom in base.rglob("pom.xml")
        if "target" not in pom.parts
    }
    if declared != actual:
        violations.append(f"Root reactor mismatch: missing={sorted(actual - declared)}, unexpected={sorted(declared - actual)}")
    if not actual:
        violations.append("No leaf modules found under modules/ or tests/")
    project_poms = [ROOT / "pom.xml"] + [pom for base in SCAN_ROOTS for pom in base.rglob("pom.xml")]
    for pom in sorted(set(project_poms)):
        if "target" in pom.parts:
            continue
        model = ET.parse(pom)
        if pom != ROOT / "pom.xml":
            if model.find("m:modules", NS) is not None or model.findtext("m:packaging", default="jar", namespaces=NS) == "pom":
                violations.append(f"{pom.relative_to(ROOT)}: only root may aggregate modules")
            parent = model.findtext("m:parent/m:relativePath", default="", namespaces=NS)
            if (pom.parent / parent).resolve() != ROOT / "pom.xml":
                violations.append(f"{pom.relative_to(ROOT)}: leaf must inherit root POM")
        artifact_id = (ET.parse(pom).findtext("m:artifactId", default="", namespaces=NS) or "").strip()
        if not artifact_id:
            continue
        if pom.parent.name != artifact_id:
            violations.append(
                f"{pom.relative_to(ROOT)}: directory '{pom.parent.name}' != artifactId '{artifact_id}'"
            )

    if violations:
        print("Backend Maven module paths and artifactIds are inconsistent:")
        print("\n".join(f"- {violation}" for violation in violations))
        return 1

    print("Backend module names are consistent with their directories.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

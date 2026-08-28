"""Fail when a backend Maven module directory differs from its artifact id.

Keeping the directory and Maven module names identical avoids ambiguous IDE
module labels such as ``conversation-implementation [shiyu-conversation-implementation]``
and makes paths in build, documentation, and schema tooling unambiguous.
"""

from __future__ import annotations

import sys
import xml.etree.ElementTree as ET
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
NS = {"m": "http://maven.apache.org/POM/4.0.0"}


def main() -> int:
    violations: list[str] = []
    for pom in sorted(ROOT.rglob("pom.xml")):
        if "target" in pom.parts:
            continue
        artifact_id = (ET.parse(pom).findtext("m:artifactId", default="", namespaces=NS) or "").strip()
        if not artifact_id:
            continue
        directory_name = pom.parent.name
        if directory_name != artifact_id:
            violations.append(
                f"{pom.relative_to(ROOT)}: directory '{directory_name}' != artifactId '{artifact_id}'"
            )

    if violations:
        print("Backend Maven module directories must match their artifactId:")
        print("\n".join(f"- {violation}" for violation in violations))
        return 1

    print("Backend module names are consistent with their directories.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

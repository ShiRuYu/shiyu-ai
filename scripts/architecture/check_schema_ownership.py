#!/usr/bin/env python3
"""Enforce ownership and isolation rules for the v4 database baseline."""

from __future__ import annotations

import re
import sys
from pathlib import Path


OWNER_ROOTS = {
    "application": "application/shiyu-application/src/main/resources/db/baseline/h2",
    "common-core": "infrastructure/shiyu-common/shiyu-common-core/src/main/resources/db/baseline/h2",
    "common-storage": "infrastructure/shiyu-common/shiyu-common-storage/src/main/resources/db/baseline/h2",
    "iam": "shiyu-domains/iam/shiyu-iam-implementation/src/main/resources/db/baseline/h2",
    "agent": "shiyu-domains/agent/shiyu-agent-implementation/src/main/resources/db/baseline/h2",
    "conversation": "shiyu-domains/conversation/shiyu-conversation-implementation/src/main/resources/db/baseline/h2",
    "education": "shiyu-domains/education/shiyu-education-implementation/src/main/resources/db/baseline/h2",
    "governance": "shiyu-domains/governance/shiyu-governance-implementation/src/main/resources/db/baseline/h2",
    "knowledge": "shiyu-domains/knowledge/shiyu-knowledge-implementation/src/main/resources/db/baseline/h2",
    "memory": "shiyu-domains/memory/shiyu-memory-implementation/src/main/resources/db/baseline/h2",
    "record": "shiyu-domains/record/shiyu-record-implementation/src/main/resources/db/baseline/h2",
    "tooling": "shiyu-domains/tooling/shiyu-tooling-implementation/src/main/resources/db/baseline/h2",
}

CREATE_TABLE = re.compile(
    r'CREATE\s+(?:CACHED\s+)?TABLE(?:\s+IF\s+NOT\s+EXISTS)?\s+"PUBLIC"\."([A-Z0-9_]+)"', re.I
)
TABLE_REF = re.compile(r'\b(?:JOIN|REFERENCES)\s+"PUBLIC"\."([A-Z0-9_]+)"', re.I)
TABLE_WITH_BODY = re.compile(
    r'CREATE\s+(?:CACHED\s+)?TABLE(?:\s+IF\s+NOT\s+EXISTS)?\s+"PUBLIC"\."([A-Z0-9_]+)".*?\((.*?)\);',
    re.I | re.S,
)
INDEX_COLUMNS = re.compile(
    r'CREATE\s+(?:UNIQUE\s+(?:NULLS\s+DISTINCT\s+)?)?INDEX.*?ON\s+"PUBLIC"\."([A-Z0-9_]+)"\s*\(([^)]*)\)',
    re.I | re.S,
)


def strip_comments(text: str) -> str:
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.S)
    return re.sub(r"^\s*--.*?$", "", text, flags=re.M)


def main() -> int:
    repo = Path(__file__).resolve().parents[2]
    failures: list[str] = []
    files: list[tuple[str, Path]] = []
    central = repo / "infrastructure" / ("shiyu-" + "ai-" + "dal") / "src/main/resources/db/baseline/h2"
    if central.exists():
        for path in central.rglob("*.sql"):
            failures.append(f"central DAL owns baseline SQL: {path.relative_to(repo)}")

    for owner, relative_root in OWNER_ROOTS.items():
        root = repo / relative_root
        if not root.exists():
            failures.append(f"missing schema owner root: {relative_root}")
            continue
        for path in root.rglob("*.sql"):
            files.append((owner, path))

    if not files:
        failures.append("no owned baseline SQL files found")

    tables: dict[str, tuple[str, Path]] = {}
    sql_by_file: list[tuple[str, Path, str]] = []
    for owner, path in files:
        sql = strip_comments(path.read_text(encoding="utf-8"))
        sql_by_file.append((owner, path, sql))
        for table in CREATE_TABLE.findall(sql):
            normalized = table.upper()
            if normalized in tables:
                previous_owner, previous_path = tables[normalized]
                failures.append(
                    f"duplicate table {normalized}: {previous_path.relative_to(repo)} and {path.relative_to(repo)}"
                )
            else:
                tables[normalized] = (owner, path)

        tenant_indexes: dict[str, set[str]] = {}
        for index in INDEX_COLUMNS.finditer(sql):
            table_name = index.group(1).upper()
            first_column = index.group(2).split(",", 1)[0]
            first_column = re.sub(r'"|\bNULLS\s+FIRST\b', "", first_column, flags=re.I).strip().upper()
            tenant_indexes.setdefault(table_name, set()).add(first_column)
        for table_name, body in TABLE_WITH_BODY.findall(sql):
            tenant_column = re.search(r'"TENANT_ID"\s+([^,\n]+)', body, re.I)
            if not tenant_column:
                continue
            normalized = table_name.upper()
            if "NOT NULL" not in tenant_column.group(1).upper():
                failures.append(f"tenant_id must be NOT NULL for {normalized}: {path.relative_to(repo)}")
            if "TENANT_ID" not in tenant_indexes.get(normalized, set()):
                failures.append(
                    f"tenant-leading index missing for {normalized}: {path.relative_to(repo)}"
                )

    for owner, path, sql in sql_by_file:
        for referenced in TABLE_REF.findall(sql):
            normalized = referenced.upper()
            referenced_owner = tables.get(normalized, (None, None))[0]
            if referenced_owner and referenced_owner != owner:
                failures.append(
                    f"cross-domain SQL reference {owner}->{referenced_owner} for {normalized}: {path.relative_to(repo)}"
                )

    if failures:
        print(f"Schema ownership failures ({len(failures)}):", file=sys.stderr)
        for failure in failures:
            print(f"  {failure}", file=sys.stderr)
        return 1

    print(f"Schema ownership valid: {len(files)} SQL files, {len(tables)} tables, {len(OWNER_ROOTS)} owners.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

"""generate-postgresql-baseline 脚本，执行项目架构与工程校验。"""

from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "scripts" / "database" / "postgresql"

SCHEMA = [
    "modules/applications/shiyu-application/src/main/resources/db/baseline/h2/schema/application/00_baseline.sql",
    "modules/infrastructure/shiyu-common-storage/src/main/resources/db/baseline/h2/schema/storage/01_storage.sql",
    "modules/infrastructure/shiyu-common-core/src/main/resources/db/baseline/h2/schema/common/02_common.sql",
    "modules/domains/iam/shiyu-iam-implementation/src/main/resources/db/baseline/h2/schema/iam/03_auth.sql",
    "modules/domains/agent/shiyu-agent-implementation/src/main/resources/db/baseline/h2/schema/agent/04_agent.sql",
    "modules/domains/model/shiyu-model-implementation/src/main/resources/db/baseline/h2/schema/model/04_model.sql",
    "modules/domains/governance/shiyu-governance-implementation/src/main/resources/db/baseline/h2/schema/governance/05_governance.sql",
    "modules/domains/knowledge/shiyu-knowledge-implementation/src/main/resources/db/baseline/h2/schema/knowledge/06_knowledge.sql",
    "modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/schema/education/07_education.sql",
    "modules/domains/knowledge/shiyu-knowledge-implementation/src/main/resources/db/baseline/h2/schema/knowledge/09_vector.sql",
    "modules/domains/governance/shiyu-governance-implementation/src/main/resources/db/baseline/h2/schema/governance/10_observation.sql",
    "modules/domains/conversation/shiyu-conversation-implementation/src/main/resources/db/baseline/h2/schema/conversation/11_conversation.sql",
    "modules/domains/memory/shiyu-memory-implementation/src/main/resources/db/baseline/h2/schema/memory/12_memory_magma.sql",
    "modules/domains/tooling/shiyu-tooling-implementation/src/main/resources/db/baseline/h2/schema/tooling/15_plugin_market.sql",
    "modules/domains/agent/shiyu-agent-implementation/src/main/resources/db/baseline/h2/schema/agent/16_ai_runtime.sql",
]
SEED = [
    "modules/infrastructure/shiyu-common-core/src/main/resources/db/baseline/h2/seed/common/01_common.sql",
    "modules/domains/iam/shiyu-iam-implementation/src/main/resources/db/baseline/h2/seed/iam/02_auth.sql",
    "modules/domains/agent/shiyu-agent-implementation/src/main/resources/db/baseline/h2/seed/agent/03_agent.sql",
    "modules/domains/agent/shiyu-agent-implementation/src/main/resources/db/baseline/h2/seed/agent/04_app_runtime.sql",
    "modules/domains/model/shiyu-model-implementation/src/main/resources/db/baseline/h2/seed/model/03_model.sql",
    "modules/domains/knowledge/shiyu-knowledge-implementation/src/main/resources/db/baseline/h2/seed/knowledge/04_knowledge.sql",
    "modules/domains/knowledge/shiyu-knowledge-implementation/src/main/resources/db/baseline/h2/seed/knowledge/06_demo_content.sql",
    "modules/domains/iam/shiyu-iam-implementation/src/main/resources/db/baseline/h2/seed/iam/05_navigation.sql",
    "modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/01_auth.sql",
    "modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/02_navigation.sql",
    "modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/07_education.sql",
    "modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/08_learning_progress.sql",
    "modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/09_curriculum.sql",
    "modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/10_resources.sql",
    "modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/11_resource_variants.sql",
    "modules/domains/conversation/shiyu-conversation-implementation/src/main/resources/db/baseline/h2/seed/conversation/11_conversation.sql",
    "modules/domains/governance/shiyu-governance-implementation/src/main/resources/db/baseline/h2/seed/governance/10_governance.sql",
    "modules/domains/memory/shiyu-memory-implementation/src/main/resources/db/baseline/h2/seed/memory/12_memory.sql",
    "modules/domains/tooling/shiyu-tooling-implementation/src/main/resources/db/baseline/h2/seed/tooling/15_plugin_market.sql",
]


def convert(text: str) -> str:
    text = re.sub(r'"([A-Za-z_][A-Za-z0-9_]*)"', lambda m: m.group(1).lower(), text)
    text = re.sub(r"\bpublic\.", "", text, flags=re.I)
    text = re.sub(r"\bCREATE\s+CACHED\s+TABLE\b", "CREATE TABLE", text, flags=re.I)
    text = re.sub(r"\bDEFAULT\s+ON\s+NULL\b", "", text, flags=re.I)
    text = re.sub(r"\bCHARACTER\s+LARGE\s+OBJECT\b", "TEXT", text, flags=re.I)
    text = re.sub(r"\bCLOB\b", "TEXT", text, flags=re.I)
    text = re.sub(r"\bBLOB\b", "BYTEA", text, flags=re.I)
    text = re.sub(r"\bTINYINT\b", "SMALLINT", text, flags=re.I)
    text = re.sub(r"\bNULLS\s+DISTINCT\b", "", text, flags=re.I)
    text = re.sub(r"\s+NULLS\s+(?:FIRST|LAST)\b", "", text, flags=re.I)
    return re.sub(r"\s+COMMENT\s+'(?:''|[^'])*'", "", text, flags=re.I)


def write(name: str, paths: list[str], marker: bool = False) -> None:
    chunks = [
        "-- PostgreSQL baseline generated from the validated H2 resources.",
        "-- Do not edit generated sections; update the source baseline and regenerate.",
        "",
    ]
    for path in paths:
        chunks += [f"-- Source: {path}", convert((ROOT / path).read_text(encoding="utf-8")).rstrip(), ""]
    if marker:
        chunks += [
            "-- The validator requires this marker after all schema and seed statements.",
            "INSERT INTO common_schema_baseline (id, baseline_version, seed_profile) VALUES (1, '4', 'system-ai');",
            "",
        ]
    (OUT / name).write_text("\n".join(chunks), encoding="utf-8")


if __name__ == "__main__":
    OUT.mkdir(parents=True, exist_ok=True)
    write("baseline-schema.sql", SCHEMA)
    write("baseline-seed.sql", SEED, marker=True)

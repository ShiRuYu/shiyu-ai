"""generate-postgresql-baseline 脚本，执行项目架构与工程校验。"""

from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "scripts" / "database" / "postgresql"

SCHEMA = [
    "modules/applications/shiyu-platform-composition/src/main/resources/db/baseline/h2/schema/application/00_baseline.sql",
    "modules/infrastructure/shiyu-common-storage/src/main/resources/db/baseline/h2/schema/storage/01_storage.sql",
    "modules/infrastructure/shiyu-common-foundation/src/main/resources/db/baseline/h2/schema/common/02_common.sql",
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
    "modules/infrastructure/shiyu-common-foundation/src/main/resources/db/baseline/h2/seed/common/01_common.sql",
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
UPDATES = [
    "modules/domains/iam/shiyu-iam-implementation/src/main/resources/db/updates/iam/20260914_platform_usage.sql",
    "modules/domains/iam/shiyu-iam-implementation/src/main/resources/db/updates/iam/20260916_tenant_module_access.sql",
    "modules/domains/iam/shiyu-iam-implementation/src/main/resources/db/updates/iam/20260923_permission_alignment.sql",
    "modules/business/education/shiyu-education-implementation/src/main/resources/db/updates/education/20261005_exam_attempt.sql",
    "modules/business/education/shiyu-education-implementation/src/main/resources/db/updates/education/20261006_question_purpose.sql",
    "modules/domains/knowledge/shiyu-knowledge-implementation/src/main/resources/db/updates/knowledge/20261007_demo_education_access.sql",
    "modules/business/education/shiyu-education-implementation/src/main/resources/db/updates/education/20261007_practice_submission.sql",
    "modules/domains/model/shiyu-model-implementation/src/main/resources/db/updates/model/20261011_deepseek_flash.sql",
    "modules/domains/agent/shiyu-agent-implementation/src/main/resources/db/updates/agent/20261012_checkpoint_pointer.sql",
    "modules/domains/agent/shiyu-agent-implementation/src/main/resources/db/updates/agent/20261013_sample_apps.sql",
    "modules/domains/agent/shiyu-agent-implementation/src/main/resources/db/updates/agent/20261014_seed_deepseek_graphs.sql",
]


def convert(text: str) -> str:
    text = re.sub(
        r"'(?:''|[^'])*'|\"([A-Za-z_][A-Za-z0-9_]*)\"",
        lambda match: match.group(0) if match.group(1) is None else f'"{match.group(1).lower()}"',
        text,
    )
    text = re.sub(r'"public"\.|\bpublic\.', "", text, flags=re.I)
    text = re.sub(r"\bCREATE\s+CACHED\s+TABLE\b", "CREATE TABLE", text, flags=re.I)
    text = re.sub(r"\bDEFAULT\s+ON\s+NULL\b", "", text, flags=re.I)
    text = re.sub(r"\bCHARACTER\s+LARGE\s+OBJECT\b", "TEXT", text, flags=re.I)
    text = re.sub(r"\bBINARY\s+LARGE\s+OBJECT\b", "BYTEA", text, flags=re.I)
    text = re.sub(r"\bCLOB\b", "TEXT", text, flags=re.I)
    text = re.sub(r"\bBLOB\b", "BYTEA", text, flags=re.I)
    text = re.sub(r"\bTINYINT\b", "SMALLINT", text, flags=re.I)
    text = re.sub(r"\bDOUBLE\b(?!\s+PRECISION\b)", "DOUBLE PRECISION", text, flags=re.I)
    text = re.sub(r"\bNULLS\s+DISTINCT\b", "", text, flags=re.I)
    text = re.sub(r"\s+NULLS\s+(?:FIRST|LAST)\b", "", text, flags=re.I)
    text = re.sub(r"\s+ON\s+UPDATE\s+CURRENT_TIMESTAMP\b", "", text, flags=re.I)
    return re.sub(r"\s+COMMENT\s+'(?:''|[^'])*'", "", text, flags=re.I)


def auto_update_columns(text: str) -> list[tuple[str, str]]:
    """Locate H2-managed update timestamps that need PostgreSQL triggers."""
    table = None
    columns = []
    for line in text.splitlines():
        declaration = re.match(
            r'\s*CREATE\s+(?:CACHED\s+)?TABLE\s+(?:"PUBLIC"\.)?"([A-Za-z_][A-Za-z0-9_]*)"',
            line,
            flags=re.I,
        )
        if declaration:
            table = declaration.group(1).lower()
        if table and re.search(r'\bON\s+UPDATE\s+CURRENT_TIMESTAMP\b', line, flags=re.I):
            column = re.match(r'\s*"([A-Za-z_][A-Za-z0-9_]*)"', line)
            if column:
                columns.append((table, column.group(1).lower()))
    return columns


def write(name: str, paths: list[str], marker: bool = False) -> None:
    chunks = [
        "-- PostgreSQL 基线由经过验证的 H2 资源生成。",
        "-- 不要直接编辑生成区，请修改源基线后重新生成。",
        "",
    ]
    update_columns = []
    for path in paths:
        source = (ROOT / path).read_text(encoding="utf-8")
        chunks += [f"-- 来源：{path}", convert(source).rstrip(), ""]
        if name == "baseline-schema.sql":
            update_columns.extend(auto_update_columns(source))
    if update_columns:
        chunks += ["-- H2 ON UPDATE CURRENT_TIMESTAMP 的 PostgreSQL 等价触发器。"]
        for column in sorted({column for _, column in update_columns}):
            chunks += [
                f'CREATE FUNCTION shiyu_touch_{column}() RETURNS trigger LANGUAGE plpgsql AS $$',
                "BEGIN",
                f'    NEW."{column}" = CURRENT_TIMESTAMP;',
                "    RETURN NEW;",
                "END;",
                "$$;",
                "",
            ]
        for table, column in update_columns:
            chunks += [
                f'CREATE TRIGGER shiyu_touch_{table}_{column} BEFORE UPDATE ON "{table}"',
                f'FOR EACH ROW EXECUTE FUNCTION shiyu_touch_{column}();',
                "",
            ]
    if marker:
        for path in UPDATES:
            chunks += [f"-- 来源：{path}", convert((ROOT / path).read_text(encoding="utf-8")).rstrip(), ""]
        chunks += [
            "-- H2 在显式 seed ID 后自动推进序列；PostgreSQL 需要显式同步。",
            "DO $$",
            "DECLARE identity_column record;",
            "BEGIN",
            "  FOR identity_column IN",
            "    SELECT table_name, column_name FROM information_schema.columns",
            "    WHERE table_schema = 'public' AND is_identity = 'YES'",
            "  LOOP",
            "    EXECUTE format(",
            "      'SELECT setval(pg_get_serial_sequence(%L, %L), COALESCE((SELECT MAX(%I) FROM %I), 1), EXISTS(SELECT 1 FROM %I))',",
            "      identity_column.table_name, identity_column.column_name,",
            "      identity_column.column_name, identity_column.table_name, identity_column.table_name);",
            "  END LOOP;",
            "END;",
            "$$;",
            "",
        ]
        chunks += [
            "-- 校验器要求在全部 schema 和 seed 语句之后存在此标记。",
            "INSERT INTO common_schema_baseline (id, baseline_version, seed_profile) VALUES (1, '4', 'system-ai');",
            "",
        ]
    (OUT / name).write_text("\n".join(chunks), encoding="utf-8")


if __name__ == "__main__":
    OUT.mkdir(parents=True, exist_ok=True)
    write("baseline-schema.sql", SCHEMA)
    write("baseline-seed.sql", SEED, marker=True)

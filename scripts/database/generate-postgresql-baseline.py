"""generate-postgresql-baseline 脚本，执行项目架构与工程校验。"""

from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "scripts" / "database" / "postgresql"
RUNTIME_RESOURCES = (
    ROOT
    / "modules/applications/shiyu-platform-composition/src/main/resources"
)
BASELINE_VERSION = "4"
SEED_PROFILE = "system-ai"

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
    "modules/domains/governance/shiyu-governance-implementation/src/main/resources/db/updates/governance/20261024_quota_state.sql",
    "modules/domains/conversation/shiyu-conversation-implementation/src/main/resources/db/updates/conversation/20261024_idempotency_fingerprint.sql",
    "modules/domains/conversation/shiyu-conversation-implementation/src/main/resources/db/updates/conversation/20261024_generation_postprocessing.sql",
    "modules/domains/agent/shiyu-agent-implementation/src/main/resources/db/updates/agent/20261027_version_numeric_delete_flag.sql",
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
            r'\s*CREATE\s+(?:CACHED\s+)?TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?(?:"PUBLIC"\.)?"([A-Za-z_][A-Za-z0-9_]*)"',
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


def generated_header(description: str) -> list[str]:
    return [
        "-- 从 H2 源资源生成的 PostgreSQL 资源。",
        "-- 不要直接编辑此生成文件；请修改源资源后重新生成。",
        f"-- {description}",
        "",
    ]


def schema_sql(paths: list[str]) -> str:
    chunks = generated_header("安装数据库结构")
    update_columns = []
    for path in paths:
        source = (ROOT / path).read_text(encoding="utf-8")
        chunks += [f"-- 来源：{path}", convert(source).rstrip(), ""]
        update_columns.extend(auto_update_columns(source))
    if update_columns:
        chunks += ["-- 使用 PostgreSQL 触发器实现 H2 的自动更新时间行为。"]
        for table, column in update_columns:
            function_name = f"shiyu_touch_{table}_{column}"
            chunks += [
                f"CREATE FUNCTION {function_name}() RETURNS trigger LANGUAGE plpgsql AS $$",
                "BEGIN",
                f'    NEW."{column}" = CURRENT_TIMESTAMP;',
                "    RETURN NEW;",
                "END;",
                "$$;",
                "",
                f'CREATE TRIGGER {function_name} BEFORE UPDATE ON "{table}"',
                f"FOR EACH ROW EXECUTE FUNCTION {function_name}();",
                "",
            ]
    return "\n".join(chunks)


def seed_sql(paths: list[str]) -> str:
    chunks = generated_header("基线种子数据")
    for path in paths:
        chunks += [
            f"-- 来源：{path}",
            convert((ROOT / path).read_text(encoding="utf-8")).rstrip(),
            "",
        ]
    return "\n".join(chunks)


def convert_update(text: str) -> str:
    text = convert(text)
    # PostgreSQL 转换旧字符列时先移除字符默认值，再显式转换并恢复整数默认值。
    text = text.replace(
        "ALTER TABLE AGENT_VERSION ALTER COLUMN DEL_FLAG SMALLINT DEFAULT 0;",
        "ALTER TABLE AGENT_VERSION ALTER COLUMN DEL_FLAG DROP DEFAULT;\n"
        "ALTER TABLE AGENT_VERSION ALTER COLUMN DEL_FLAG TYPE SMALLINT USING CAST(DEL_FLAG AS SMALLINT);\n"
        "ALTER TABLE AGENT_VERSION ALTER COLUMN DEL_FLAG SET DEFAULT 0;",
    )
    return re.sub(
        r"INSERT INTO AUTH_AUTH_CODE \(ID, CODE, NAME, STATUS, DEL_FLAG, CREATE_BY, UPDATE_BY\)\s*"
        r"SELECT \(SELECT COALESCE\(MAX\(ID\), 0\) \+ 1 FROM AUTH_AUTH_CODE\),\s*",
        "INSERT INTO AUTH_AUTH_CODE (CODE, NAME, STATUS, DEL_FLAG, CREATE_BY, UPDATE_BY)\nSELECT ",
        text,
        flags=re.I,
    )


def updates_sql(paths: list[str]) -> str:
    chunks = generated_header("按初始化器顺序执行幂等启动增补")
    for path in paths:
        chunks += [
            f"-- 来源：{path}",
            convert_update((ROOT / path).read_text(encoding="utf-8")).rstrip(),
            "",
        ]
    return "\n".join(chunks)


def identity_sequence_sql(paths: list[str]) -> list[str]:
    table_names = sorted(
        {
            match.group(1).lower()
            for path in paths
            for match in re.finditer(
                r'CREATE\s+(?:CACHED\s+)?TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?(?:"PUBLIC"\.)?"([A-Za-z_][A-Za-z0-9_]*)"',
                (ROOT / path).read_text(encoding="utf-8"),
                flags=re.I,
            )
        }
    )
    values = ", ".join("'" + table.replace("'", "''") + "'" for table in table_names)
    return [
        "-- 显式种子标识写入后需要同步 PostgreSQL 自增序列。",
        "DO $$",
        "DECLARE identity_column record; identity_value bigint;",
        "BEGIN",
        "  FOR identity_column IN",
        f"    SELECT table_name, column_name FROM information_schema.columns",
        f"    WHERE table_schema = 'public' AND is_identity = 'YES' AND table_name IN ({values})",
        "  LOOP",
        "    EXECUTE format('LOCK TABLE %I.%I IN ACCESS EXCLUSIVE MODE',",
        "      'public', identity_column.table_name);",
        "    EXECUTE format('SELECT COALESCE(MAX(%I), 0) + 1 FROM %I.%I',",
        "      identity_column.column_name, 'public', identity_column.table_name) INTO identity_value;",
        "    EXECUTE format('ALTER TABLE %I.%I ALTER COLUMN %I RESTART WITH %s',",
        "      'public', identity_column.table_name, identity_column.column_name, identity_value);",
        "  END LOOP;",
        "END;",
        "$$;",
        "",
    ]


def postgresql_update_resource(path: str) -> str:
    source_path = path.split("/src/main/resources/", 1)[1]
    return f"db/updates/postgresql/{source_path.split('/updates/', 1)[1]}"


def write_generated(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def write_baseline_resources() -> None:
    education_schema = [path for path in SCHEMA if "/education/" in path]
    platform_schema = [path for path in SCHEMA if path not in education_schema]
    education_seed = [path for path in SEED if "/education/" in path]
    platform_seed = [path for path in SEED if path not in education_seed]

    platform_schema_sql = schema_sql(platform_schema)
    education_schema_sql = schema_sql(education_schema)
    ordered_schema = platform_schema + education_schema
    all_schema_sql = schema_sql(ordered_schema)
    ordered_seed = platform_seed + education_seed
    updates = updates_sql(UPDATES)
    update_manifest = generated_header("按顺序排列的 PostgreSQL 启动增补")
    update_manifest.extend(postgresql_update_resource(path) for path in UPDATES)

    write_generated(RUNTIME_RESOURCES / "db/baseline/postgresql/schema.sql", platform_schema_sql)
    write_generated(
        RUNTIME_RESOURCES / "db/baseline/postgresql/education-schema.sql",
        education_schema_sql,
    )
    write_generated(RUNTIME_RESOURCES / "db/baseline/postgresql/seed.sql", seed_sql(platform_seed))
    write_generated(
        RUNTIME_RESOURCES / "db/baseline/postgresql/education-seed.sql",
        seed_sql(education_seed),
    )
    write_generated(
        RUNTIME_RESOURCES / "db/baseline/postgresql/identity-sequences.sql",
        "\n".join(
            generated_header("写入显式种子标识后推进自增序列")
            + identity_sequence_sql(SCHEMA)
        ),
    )
    write_generated(
        RUNTIME_RESOURCES / "db/baseline/postgresql/update-manifest.txt",
        "\n".join(update_manifest),
    )
    for path in UPDATES:
        runtime_path = RUNTIME_RESOURCES / postgresql_update_resource(path)
        write_generated(
            runtime_path,
            "\n".join(
                generated_header(f"转换来源：{path}")
                + [convert_update((ROOT / path).read_text(encoding="utf-8")).rstrip(), ""]
            ),
        )

    write_generated(OUT / "baseline-schema.sql", all_schema_sql)
    baseline_seed = seed_sql(ordered_seed).rstrip().splitlines()
    baseline_seed += [
        "",
        "-- 在数据库结构和种子语句完成后写入版本标记。",
        "INSERT INTO common_schema_baseline (id, baseline_version, seed_profile) "
        f"VALUES (1, '{BASELINE_VERSION}', '{SEED_PROFILE}');",
        "",
    ]
    baseline_seed += identity_sequence_sql(ordered_schema)
    baseline_seed += [""]
    baseline_seed += updates_sql(UPDATES).rstrip().splitlines()
    write_generated(OUT / "baseline-seed.sql", "\n".join(baseline_seed))


if __name__ == "__main__":
    write_baseline_resources()

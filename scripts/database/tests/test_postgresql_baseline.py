"""Keep generated PostgreSQL runtime resources aligned with H2 initialization."""

import importlib.util
from pathlib import Path
import re
import unittest


GENERATOR = Path(__file__).resolve().parents[1] / "generate-postgresql-baseline.py"
SPEC = importlib.util.spec_from_file_location("generate_postgresql_baseline", GENERATOR)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


class PostgreSqlBaselineTest(unittest.TestCase):
    def test_order_column_keeps_quotes(self) -> None:
        converted = MODULE.convert('CREATE TABLE "AUTH_TENANT" ("ORDER" INTEGER);')
        self.assertIn('CREATE TABLE "auth_tenant"', converted)
        self.assertIn('"order" INTEGER', converted)

    def test_h2_public_schema_qualifier_is_removed(self) -> None:
        converted = MODULE.convert('CREATE INDEX "PUBLIC"."IDX_X" ON "X"("ID");')
        self.assertIn('CREATE INDEX "idx_x" ON "x"("id")', converted)

    def test_h2_on_update_syntax_is_removed(self) -> None:
        converted = MODULE.convert(
            'CREATE TABLE "X" ("UPDATED_AT" TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);'
        )
        self.assertNotIn('ON UPDATE CURRENT_TIMESTAMP', converted)

    def test_h2_binary_large_object_becomes_bytea(self) -> None:
        converted = MODULE.convert('"EMBEDDING_BINARY" BINARY LARGE OBJECT DEFAULT NULL')
        self.assertEqual('"embedding_binary" BYTEA DEFAULT NULL', converted)

    def test_h2_double_becomes_double_precision(self) -> None:
        converted = MODULE.convert('"CONFIDENCE" DOUBLE NOT NULL, "SCORE" DOUBLE PRECISION')
        self.assertEqual(
            '"confidence" DOUBLE PRECISION NOT NULL, "score" DOUBLE PRECISION',
            converted,
        )

    def test_auto_update_columns_are_discovered_for_triggers(self) -> None:
        source = '''
CREATE CACHED TABLE "PUBLIC"."EDU_STUDENT"(
    "UPDATED_AT" TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE CACHED TABLE "PUBLIC"."EDU_QUESTION"(
    "UPDATE_TIME" TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
'''
        self.assertEqual(
            [("edu_student", "updated_at"), ("edu_question", "update_time")],
            MODULE.auto_update_columns(source),
        )

    def test_json_seed_content_preserves_case(self) -> None:
        source = 'INSERT INTO "APP" ("CONFIG") VALUES (\'{"NodeType":"Start"}\');'
        converted = MODULE.convert(source)
        self.assertIn('"app" ("config")', converted)
        self.assertIn('\'{"NodeType":"Start"}\'', converted)

    def test_postgresql_updates_match_h2_initializer_order(self) -> None:
        initializer = (
            MODULE.ROOT
            / "modules/applications/shiyu-platform-composition/src/main/java/com/shiyu/ai/composition/database/DatabaseInitializer.java"
        ).read_text(encoding="utf-8")
        h2_updates = re.findall(r'"classpath:(db/updates/[^"]+)"', initializer)
        expected_updates = [
            f"db/updates/postgresql/{path.removeprefix('db/updates/')}"
            for path in h2_updates
        ]
        self.assertEqual(
            expected_updates,
            [MODULE.postgresql_update_resource(path) for path in MODULE.UPDATES],
        )

    def test_runtime_generation_separates_seed_and_updates(self) -> None:
        MODULE.write_baseline_resources()
        resources = MODULE.RUNTIME_RESOURCES / "db/baseline/postgresql"
        runtime_seed = (resources / "seed.sql").read_text(encoding="utf-8")
        runtime_updates = [
            (MODULE.RUNTIME_RESOURCES / MODULE.postgresql_update_resource(path)).read_text(
                encoding="utf-8"
            )
            for path in MODULE.UPDATES
        ]
        self.assertNotIn("CREATE TABLE", runtime_seed)
        self.assertTrue(all(sql.strip() for sql in runtime_updates))
        self.assertIn("shiyu_touch_", (resources / "education-schema.sql").read_text(encoding="utf-8"))
        identity_sql = (resources / "identity-sequences.sql").read_text(encoding="utf-8")
        self.assertIn("DO $$", identity_sql)
        self.assertIn("'common_dict'", identity_sql)
        self.assertNotIn("AND table_name IN ()", identity_sql)
        baseline_seed = (MODULE.OUT / "baseline-seed.sql").read_text(encoding="utf-8")
        self.assertLess(
            baseline_seed.index("20261024_generation_postprocessing.sql"),
            baseline_seed.index("DO $$"),
        )
        self.assertEqual(
            [MODULE.postgresql_update_resource(path) for path in MODULE.UPDATES],
            [
                line
                for line in (resources / "update-manifest.txt").read_text(encoding="utf-8").splitlines()
                if line and not line.startswith("--")
            ],
        )

    def test_education_resources_are_separate_from_platform_resources(self) -> None:
        MODULE.write_baseline_resources()
        resources = MODULE.RUNTIME_RESOURCES / "db/baseline/postgresql"
        platform_schema = (resources / "schema.sql").read_text(encoding="utf-8")
        education_schema = (resources / "education-schema.sql").read_text(encoding="utf-8")
        platform_seed = (resources / "seed.sql").read_text(encoding="utf-8")
        education_seed = (resources / "education-seed.sql").read_text(encoding="utf-8")
        self.assertNotIn('CREATE TABLE "edu_', platform_schema)
        self.assertIn('CREATE TABLE "edu_', education_schema)
        self.assertNotIn('INSERT INTO "edu_', platform_seed)
        self.assertIn('INSERT INTO "edu_', education_seed)


if __name__ == "__main__":
    unittest.main()

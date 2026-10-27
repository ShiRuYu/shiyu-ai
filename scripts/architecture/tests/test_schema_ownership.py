"""验证租户索引门禁识别数据库约束索引并拒绝非租户前缀。"""

import importlib.util
import unittest
from pathlib import Path


SCRIPT = Path(__file__).resolve().parents[1] / "check_schema_ownership.py"
SPEC = importlib.util.spec_from_file_location("check_schema_ownership", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


class TenantLeadingIndexTest(unittest.TestCase):
    def test_primary_and_unique_constraints_supply_tenant_leading_indexes(self):
        for constraint in (
            'CONSTRAINT "PK_ROW" PRIMARY KEY ("TENANT_ID", "ID")',
            'CONSTRAINT "UK_ROW" UNIQUE ("TENANT_ID", "REFERENCE")',
            'UNIQUE NULLS DISTINCT ("TENANT_ID", "REFERENCE")',
        ):
            with self.subTest(constraint=constraint):
                sql = 'CREATE TABLE "PUBLIC"."ROW" ("TENANT_ID" BIGINT NOT NULL, ' + constraint + ');'
                self.assertIn("ROW", MODULE.tenant_leading_tables(sql))

    def test_nonleading_constraint_and_foreign_key_do_not_supply_tenant_index(self):
        for constraint in (
            'PRIMARY KEY ("ID", "TENANT_ID")',
            'UNIQUE ("REFERENCE", "TENANT_ID")',
            'FOREIGN KEY ("TENANT_ID") REFERENCES "PUBLIC"."TENANT" ("TENANT_ID")',
            'CHECK ("TENANT_ID" > 0)',
        ):
            with self.subTest(constraint=constraint):
                sql = 'CREATE TABLE "PUBLIC"."ROW" ("TENANT_ID" BIGINT NOT NULL, ' + constraint + ');'
                self.assertNotIn("ROW", MODULE.tenant_leading_tables(sql))

    def test_explicit_tenant_index_and_inline_primary_key_are_recognized(self):
        self.assertIn("ROW", MODULE.tenant_leading_tables(
            'CREATE INDEX "IDX" ON "PUBLIC"."ROW" ("TENANT_ID", "ID");'))
        self.assertIn("ROW", MODULE.tenant_leading_tables(
            'CREATE TABLE "PUBLIC"."ROW" ("TENANT_ID" BIGINT NOT NULL PRIMARY KEY);'))


if __name__ == "__main__":
    unittest.main()

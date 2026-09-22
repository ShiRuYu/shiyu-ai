"""验证契约门禁能够识别接口漂移和路径模板差异。"""

import importlib.util
import tempfile
import unittest
from pathlib import Path

MODULE_PATH = Path(__file__).resolve().parents[2] / "docs/openapi_contract.py"
SPEC = importlib.util.spec_from_file_location("openapi_contract", MODULE_PATH)
contract = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(contract)


class OpenApiContractTest(unittest.TestCase):
    def test_permission_matrix_preserves_navigation_grants(self):
        generator_spec = importlib.util.spec_from_file_location(
            "generate_reference_docs", MODULE_PATH.parent / "generate_reference_docs.py"
        )
        generator = importlib.util.module_from_spec(generator_spec)
        generator_spec.loader.exec_module(generator)
        repo = MODULE_PATH.parents[2]
        iam = repo / "modules/domains/iam/shiyu-iam-implementation/src/main/resources/db/baseline/h2/seed/iam"
        education = repo / "modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education"
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory) / "permissions.md"
            generator.generate_permission_matrix(
                repo, iam / "02_auth.sql", iam / "05_navigation.sql", output,
                [education / "01_auth.sql"], [education / "02_navigation.sql"],
            )
            document = output.read_text(encoding="utf-8")
            for code_id, code in ((211, "create"), (212, "update"), (213, "delete")):
                row = next(line for line in document.splitlines() if line.startswith(f"| {code_id} |"))
                self.assertIn(f"edu:student:{code}", row)
                self.assertIn("super, admin", row)
            self.assertIn("归属租户和当前租户均为 ID 1", document)

    def test_permission_generation_keeps_education_prefix_and_method_scope(self):
        generator_spec = importlib.util.spec_from_file_location(
            "generate_reference_docs", MODULE_PATH.parent / "generate_reference_docs.py"
        )
        generator = importlib.util.module_from_spec(generator_spec)
        generator_spec.loader.exec_module(generator)
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "modules/business/example/src/main/java/ExampleController.java"
            source.parent.mkdir(parents=True)
            source.write_text('''package com.shiyu.ai.education.implementation.web.controller.nested;
@RequestMapping("/examples")
@SaCheckPermission("example:read")
public class ExampleController {
    @PostMapping
    @SaCheckPermission("example:create")
    public void create() {}
    @GetMapping("/{id}")
    public void get() {}
}
''', encoding="utf-8")
            rows = generator.parse_controller_permissions(root)
            self.assertEqual(
                ["example:read AND example:create", "example:read"],
                [row["permission"] for row in rows],
            )
            self.assertEqual("/api/education/examples/{id}", rows[1]["path"])

    def test_permission_matrix_requires_class_and_method_grants(self):
        generator_spec = importlib.util.spec_from_file_location(
            "generate_reference_docs", MODULE_PATH.parent / "generate_reference_docs.py"
        )
        generator = importlib.util.module_from_spec(generator_spec)
        generator_spec.loader.exec_module(generator)
        repo = MODULE_PATH.parents[2]
        iam = repo / "modules/domains/iam/shiyu-iam-implementation/src/main/resources/db/baseline/h2/seed/iam"
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "modules/example/src/main/java/ExampleController.java"
            source.parent.mkdir(parents=True)
            source.write_text('''@RequestMapping("/examples")
@SaCheckPermission("platform:usage:read")
public class ExampleController {
    @PostMapping
    @SaCheckPermission("system:user:list")
    public void create() {}
}
''', encoding="utf-8")
            output = root / "permissions.md"
            generator.generate_permission_matrix(
                root, iam / "02_auth.sql", iam / "05_navigation.sql", output
            )
            endpoint = next(
                line for line in output.read_text(encoding="utf-8").splitlines()
                if line.startswith("| POST | `/examples` |")
            )
            self.assertIn("`platform:usage:read AND system:user:list`", endpoint)
            self.assertIn("| super |", endpoint)
            self.assertNotIn("super, admin", endpoint)

    def test_missing_extra_and_method_change(self):
        expected = {("GET", "/api/a"), ("POST", "/api/b")}
        actual = {("POST", "/api/a"), ("POST", "/api/c")}
        self.assertEqual(4, len(contract.differences(expected, actual)))

    def test_regex_path_variables(self):
        for path in ("/api/education/files/{fileName:.+}", "/api/education/files/{fileName:[a-z]{2,4}}"):
            self.assertEqual("/api/education/files/{fileName}", contract.normalize_path(path))

    def test_document_same_count_wrong_route_and_duplicates(self):
        spec = {"paths": {"/api/a": {"get": {}}, "/api/b": {"post": {}}}}
        wrong = "| GET | `/api/a` | ok |\n| GET | `/api/a` | duplicate |"
        self.assertEqual(2, len(contract.document_differences(spec, wrong)))
        good = "| POST | `/api/b` | ok |\n| GET | `/api/a` | ok |"
        self.assertEqual([], contract.document_differences(spec, good))

    def test_missing_snapshot_fails_and_explicit_export_is_stable(self):
        spec = {"paths": {"/api/education/files/{fileName:.+}": {"get": {}}}}
        with tempfile.TemporaryDirectory() as directory:
            snapshot = Path(directory) / "contract.json"
            with self.assertRaises(FileNotFoundError):
                contract.sync_snapshot(spec, snapshot)
            contract.sync_snapshot(spec, snapshot, True)
            first = snapshot.read_bytes()
            contract.sync_snapshot(spec, snapshot, True)
            self.assertEqual(first, snapshot.read_bytes())
            other = {"paths": {"/api/missing": {"get": {}}}}
            with self.assertRaisesRegex(ValueError, "missing: GET"):
                contract.sync_snapshot(other, snapshot)

    def test_invalid_or_empty_export_fails(self):
        for spec in ({}, {"paths": {}}, {"paths": {"/api/a": {"parameters": []}}}):
            with self.assertRaises(ValueError):
                contract.operations(spec)

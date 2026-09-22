"""验证旧版 Spring 路由字面量扫描的范围、检出能力和误报过滤。"""

import contextlib
import importlib.util
import io
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

SCRIPT = Path(__file__).resolve().parents[1] / "check_legacy_routes.py"
SPEC = importlib.util.spec_from_file_location("legacy_routes", SCRIPT)
scanner = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(scanner)


class LegacyRouteTests(unittest.TestCase):
    def scan(self, files):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            for name, content in files.items():
                source = root / name
                source.parent.mkdir(parents=True, exist_ok=True)
                source.write_text(content, encoding="utf-8")
            output = io.StringIO()
            with patch.object(scanner, "ROOT", root), contextlib.redirect_stderr(output), contextlib.redirect_stdout(output):
                status = scanner.main()
            return status, output.getvalue()

    def test_all_module_categories(self):
        for category in ("applications/web", "domains/orders", "business/education", "infrastructure/storage"):
            with self.subTest(category=category):
                status, output = self.scan({f"modules/{category}/src/main/java/Controller.java": '@GetMapping("/v1/orders")'})
                self.assertEqual(1, status)
                self.assertIn("Controller.java:1", output)

    def test_comments_strings_and_non_path_attributes_are_ignored(self):
        source = '''// @GetMapping("/v1/a")
/* @PostMapping("/v1/b") */
class Example {
 String normal = "@GetMapping(\\"/v1/c\\")";
 String block = """
 @GetMapping("/v1/d")
 """;
 @RequestMapping(path="/orders", headers="/v1/header", name="/v1/name")
 @GetMapping("https://provider.example/v1/chat")
 @GetMapping("/v3/api-docs")
 @GetMapping("/v10/orders")
 @GetMapping("/orders/v1")
}'''
        self.assertEqual(0, self.scan({"modules/domains/a/src/main/java/Example.java": source})[0])

    def test_multiline_arrays_class_and_method_mappings(self):
        source = '''@RequestMapping(
 path = {"/current", "/v1"})
class Example {
 @org.springframework.web.bind.annotation.GetMapping(
 value = {"/okay", "v1/orders"}, produces = "application/json")
 void get() {}
 @PatchMapping(/* ignored ) */ path = "/v1/patch")
 void patch() {}
}'''
        status, output = self.scan({"modules/domains/a/src/main/java/Example.java": source})
        self.assertEqual(1, status)
        for line in (1, 4, 7):
            self.assertIn(f"Example.java:{line}", output)
        self.assertEqual(3, output.count("legacy /v1 route:"))

    def test_only_production_java_sources_are_scanned(self):
        paths = (
            "modules/domains/a/src/test/java/Test.java",
            "modules/domains/a/target/generated-sources/src/main/java/Generated.java",
            "modules/platform/a/generated/src/main/java/Generated.java",
            "modules/platform/a/src/main/java/generated/Generated.java",
            "modules/platform/a/src/main/resources/Example.java",
            "modules/platform/a/other/Example.java",
            "outside/src/main/java/Example.java",
        )
        self.assertEqual(0, self.scan({name: '@GetMapping("/v1")' for name in paths})[0])

    def test_literal_in_array_with_constants_is_detected(self):
        status, _ = self.scan({"modules/domains/a/src/main/java/Example.java": '@GetMapping({CURRENT, "/v1/orders"})'})
        self.assertEqual(1, status)

    def test_composed_expressions_are_out_of_scope(self):
        status, _ = self.scan({"modules/domains/a/src/main/java/Example.java": '@GetMapping("/v1" + "0/orders")'})
        self.assertEqual(0, status)


if __name__ == "__main__":
    unittest.main()

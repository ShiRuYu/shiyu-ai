import importlib.util
import sys
import tempfile
import unittest
from pathlib import Path


SCRIPT = Path(__file__).parents[1] / "check_mixed_package_roles.py"
SPEC = importlib.util.spec_from_file_location("check_mixed_package_roles", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = MODULE
SPEC.loader.exec_module(MODULE)


class MixedPackageRolesTest(unittest.TestCase):
    def add_source(self, root: Path, package: str, name: str, content: str) -> None:
        source = root / "modules/sample/src/main/java" / Path(package.replace(".", "/")) / name
        source.parent.mkdir(parents=True, exist_ok=True)
        source.write_text(f"package {package};\n\n{content}\n", encoding="utf-8")

    def test_controller_and_configuration_in_one_package_is_reported(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            package = "com.example.web"
            self.add_source(root, package, "DemoController.java", "@RestController\nclass DemoController {}")
            self.add_source(
                root,
                package,
                "DemoConfig.java",
                "@Configuration\nclass DemoConfig {}",
            )

            reports = MODULE.find_violations([root])

            self.assertEqual(1, len(reports))
            self.assertEqual(package, reports[0].package)
            self.assertIn("controller", reports[0].roles)
            self.assertIn("configuration", reports[0].roles)
            self.assertEqual("P0", reports[0].mixed_level)
            self.assertIn("按职责拆分", reports[0].migration_suggestion)

    def test_multiline_annotation_and_nested_type_are_classified(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(
                root,
                "com.example.web",
                "Demo.java",
                """@RestController(
    value = \"demo\"
)
class Demo {
    @Configuration
    static class NestedConfig {}
}
""",
            )

            report = MODULE.scan_paths([root])[0]

            self.assertEqual({"controller", "configuration"}, set(report.roles))
            self.assertEqual("P0", report.mixed_level)

    def test_allowlisted_mixed_package_is_not_reported(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            package = "com.example.intentional"
            self.add_source(root, package, "DemoController.java", "@RestController\nclass DemoController {}")
            self.add_source(root, package, "DemoConfig.java", "@Configuration\nclass DemoConfig {}")

            reports = MODULE.find_violations([root], allowlist={package})

            self.assertEqual([], reports)
            report = MODULE.scan_paths([root])[0]
            rendered = MODULE.render_report([report], {package})
            self.assertIn("status=allowlisted", rendered)

    def test_single_role_package_is_clean(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(root, "com.example.service", "DemoService.java", "@Service\nclass DemoService {}")

            self.assertEqual([], MODULE.find_violations([root]))
            report = MODULE.scan_paths([root])[0]
            self.assertEqual("clean", report.mixed_level)
            self.assertEqual("保持当前包", report.migration_suggestion)


if __name__ == "__main__":
    unittest.main()

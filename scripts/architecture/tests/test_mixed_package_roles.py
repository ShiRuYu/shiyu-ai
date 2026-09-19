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
    def test_configuration_allowlist_does_not_hide_runtime_tenant_factory(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            package = "com.example.config"
            self.add_source(root, package, "ContextTenantFactory.java",
                            "@Component class ContextTenantFactory implements TenantFactory {}")
            self.assertEqual(1, len(MODULE.find_violations([root], {package})))

    def test_reviewed_types_live_in_responsibility_packages(self):
        root = Path(__file__).resolve().parents[3]
        expected = {
            "ModelRouter": "infrastructure.gateway.service",
            "ModelRoutePolicy": "infrastructure.gateway.model",
            "ModelCostSnapshot": "infrastructure.gateway.model",
            "ModelProviderCapabilities": "infrastructure.gateway.model",
            "ProviderHealth": "infrastructure.gateway.model",
            "MediaProvider": "infrastructure.media.port",
            "MediaProviderRegistry": "infrastructure.media.service",
            "ContextTenantFactory": "mybatis.tenant",
            "StorageKeys": "storage.file.key",
            "AuthContextService": "implementation.application.service",
        }
        for name, suffix in expected.items():
            with self.subTest(type=name):
                files = [p for p in (root / "modules").rglob(name + ".java")
                         if "/src/main/java/" in p.as_posix()]
                self.assertEqual(1, len(files))
                self.assertIn(suffix + ";", files[0].read_text(encoding="utf-8"))

    def test_configuration_allowlist_does_not_hide_storage_or_runner(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            package = "com.example.config"
            self.add_source(root, package, "SessionDaoImpl.java", "class SessionDaoImpl {}")
            self.add_source(root, package, "Warmup.java", "class Warmup implements ApplicationRunner {}")
            violations = MODULE.find_violations([root], {package})
            self.assertEqual(1, len(violations))
            self.assertEqual({"SessionDaoImpl.java", "Warmup.java"}, set(violations[0].misplaced_types))

    def test_plain_service_is_not_hidden_without_spring_annotation(self):
        roles = MODULE.classify_source("class PricingService {}")
        self.assertIn("service", roles)

    def test_service_interface_remains_a_port(self):
        roles = MODULE.classify_source("interface PricingService {}")
        self.assertEqual({"port/interface"}, roles)

    def test_adapter_interface_remains_a_port(self):
        self.assertEqual({"port/interface"}, MODULE.classify_source("interface ModelAdapter {}"))

    def test_jdk_executor_wrapper_is_not_a_business_service(self):
        roles = MODULE.classify_source("class SafeExecutorService extends AbstractExecutorService {}")
        self.assertNotIn("service", roles)

    def test_plain_repository_implementations_are_persistence(self):
        for name in ("OrderRepositoryImpl", "SessionDaoImpl", "InMemoryOrderRepository"):
            with self.subTest(name=name):
                self.assertIn("persistence", MODULE.classify_source(f"class {name} {{}}"))

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

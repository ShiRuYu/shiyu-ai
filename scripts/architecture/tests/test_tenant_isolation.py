import importlib.util
import sys
import tempfile
import unittest
from pathlib import Path


ARCH = Path(__file__).parents[1]
if str(ARCH) not in sys.path:
    sys.path.insert(0, str(ARCH))
SCRIPT = ARCH / "check_tenant_isolation.py"
SPEC = importlib.util.spec_from_file_location("check_tenant_isolation", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = MODULE
SPEC.loader.exec_module(MODULE)


class TenantIsolationScannerTest(unittest.TestCase):
    def test_tenant_column_with_other_options_is_reported(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(root, "com.example.persistence", "Record.java",
                '@Table("records") class Record {\n'
                '@Column(value = "tenant_id",\n tenantId = true) private Long tenantId; }')
            reports, _ = MODULE.scan([root])
            self.assertTrue(reports[0].has_tenant_mapping)

    def add_source(self, root: Path, package: str, name: str, content: str) -> Path:
        source = root / "modules/sample/src/main/java" / Path(package.replace(".", "/")) / name
        source.parent.mkdir(parents=True, exist_ok=True)
        source.write_text(f"package {package};\n\n{content}\n", encoding="utf-8")
        return source

    def test_reports_roles_and_tenant_mapping(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            source = self.add_source(
                root,
                "com.example.persistence",
                "TenantRecord.java",
                """@Mapper
class TenantRecord {
    @Select("SELECT * FROM tenant_record WHERE tenant_id = #{tenantId}")
    Object find(long tenantId) { return null; }
    @Column(tenantId = true)
    private Long tenantId;
}
""",
            )

            reports, violations = MODULE.scan([root])

            self.assertEqual([], violations)
            report = next(report for report in reports if report.path == source.resolve().as_posix())
            self.assertIn("persistence", report.roles)
            self.assertTrue(report.has_tenant_mapping)
            self.assertTrue(report.has_explicit_sql)

    def test_direct_bypass_requires_an_explicit_allowlist(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            source = self.add_source(
                root,
                "com.example.lookup",
                "Lookup.java",
                "class Lookup { void read() { TenantManager.withoutTenantCondition(() -> {}); } }",
            )

            _, violations = MODULE.scan([root])
            self.assertEqual("direct-tenant-bypass", violations[0].kind)

            _, allowed = MODULE.scan([root], {source.resolve().as_posix()})
            self.assertEqual([], allowed)

    def test_extra_tenant_factory_cannot_bypass_rules_by_copying_the_name(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(
                root,
                "com.example.config",
                "OtherTenantFactory.java",
                "class OtherTenantFactory implements TenantFactory {}",
            )
            self.add_source(
                root,
                "com.example.config",
                "ContextTenantFactory.java",
                "class ContextTenantFactory implements TenantFactory {}",
            )

            _, violations = MODULE.scan([root])
            self.assertEqual(["extra-tenant-factory"] * 2, [violation.kind for violation in violations])

    def test_direct_ignore_and_factory_registration_are_rejected(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(root, "com.example", "Unsafe.java",
                "class Unsafe { void run() { TenantManager.ignoreTenantCondition(); TenantManager.setTenantFactory(null); } }")
            _, violations = MODULE.scan([root])
            self.assertEqual({"direct-tenant-bypass", "extra-tenant-factory"}, {v.kind for v in violations})

    def test_static_import_of_filter_bypass_is_rejected(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(root, "com.example", "Unsafe.java",
                "import static com.mybatisflex.core.tenant.TenantManager.ignoreTenantCondition;\nclass Unsafe { void run() { ignoreTenantCondition(); } }")
            _, violations = MODULE.scan([root])
            self.assertEqual(["direct-tenant-bypass"], [v.kind for v in violations])

    def test_static_factory_registration_is_rejected(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(root, "com.example", "Unsafe.java",
                "import static com.mybatisflex.core.tenant.TenantManager.setTenantFactory;\n"
                "class Unsafe { void run() { setTenantFactory(null); } }")
            _, violations = MODULE.scan([root])
            self.assertEqual(["extra-tenant-factory"], [v.kind for v in violations])

    def test_domain_layer_cannot_bind_scope(self):
        for code in (
            "class Service { void run() { TenantScope.set(null); } }",
            "class Service { void run() { TenantScope.withTenant(null, null); } }",
            "import static com.shiyu.ai.kernel.context.TenantScope.set;\nclass Service {}",
        ):
            with self.subTest(code=code), tempfile.TemporaryDirectory() as raw_root:
                root = Path(raw_root)
                self.add_source(root, "com.example.domain.service", "Service.java", code)
                _, violations = MODULE.scan([root])
                self.assertEqual(["domain-tenant-scope-mutation"], [v.kind for v in violations])

    def test_qualified_and_secondary_factory_interfaces_are_rejected(self):
        for declaration in (
            "implements java.io.Serializable, TenantFactory",
            "implements com.mybatisflex.core.tenant.TenantFactory",
        ):
            with self.subTest(declaration=declaration), tempfile.TemporaryDirectory() as raw_root:
                root = Path(raw_root)
                self.add_source(root, "com.example", "Unsafe.java",
                    f"class Unsafe {declaration} {{}}")
                _, violations = MODULE.scan([root])
                self.assertEqual(["extra-tenant-factory"], [v.kind for v in violations])


if __name__ == "__main__":
    unittest.main()

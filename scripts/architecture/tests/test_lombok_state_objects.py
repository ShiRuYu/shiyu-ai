import importlib.util
import sys
import tempfile
import unittest
from pathlib import Path


SCRIPT = Path(__file__).parents[1] / "check_lombok_state_objects.py"
SPEC = importlib.util.spec_from_file_location("check_lombok_state_objects", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
sys.modules[SPEC.name] = MODULE
SPEC.loader.exec_module(MODULE)


class LombokStateObjectsTest(unittest.TestCase):
    def scan(self, source: str):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "ExampleProperties.java"
            path.write_text(source, encoding="utf-8")
            return MODULE.scan_file(path)

    def test_reports_mutable_properties_without_lombok_accessors(self):
        violations = self.scan(
            """
            @ConfigurationProperties(prefix = "sample")
            public class ExampleProperties {
                private String value;
            }
            """
        )

        self.assertEqual(["ExampleProperties"], [item.type_name for item in violations])

    def test_accepts_getter_and_setter_annotations(self):
        violations = self.scan(
            """
            import lombok.Getter;
            import lombok.Setter;
            @Getter
            @Setter
            public class ExampleProperties {
                private String value;
            }
            """
        )

        self.assertEqual([], violations)

    def test_allowlist_excludes_a_justified_exception(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "ExampleProperties.java"
            path.write_text(
                "public class ExampleProperties { private String value; }",
                encoding="utf-8",
            )
            self.assertEqual([], MODULE.scan_file(path, {"ExampleProperties"}))

    def test_ignores_records_enums_utilities_and_configuration_classes(self):
        source = """
        public record ExampleProperties(String value) {}
        public enum OtherProperties { VALUE }
        public final class UtilityProperties {
            private UtilityProperties() {}
        }
        @Configuration
        public class SpringConfiguration {
            private String value;
        }
        """

        self.assertEqual([], self.scan(source))


if __name__ == "__main__":
    unittest.main()

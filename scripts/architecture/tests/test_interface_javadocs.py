import importlib.util
import sys
import tempfile
import unittest
from pathlib import Path


SCRIPT = Path(__file__).parents[1] / "check_interface_javadocs.py"
SPEC = importlib.util.spec_from_file_location("check_interface_javadocs", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
sys.modules[SPEC.name] = MODULE
SPEC.loader.exec_module(MODULE)


class InterfaceJavadocsTest(unittest.TestCase):
    def scan(self, source: str):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "Example.java"
            path.write_text(source, encoding="utf-8")
            return MODULE.scan_file(path)

    def test_reports_interface_method_without_javadoc(self):
        violations = self.scan(
            """package sample;
            /** 示例契约。 */
            public interface Example {
                /** 查询数据。 */
                String find(String id);

                void save(String id);
            }
            """
        )

        self.assertEqual(["save"], [item.method_name for item in violations])

    def test_ignores_annotations_and_supports_multiline_and_default_methods(self):
        violations = self.scan(
            """package sample;
            /** 示例契约。 */
            public interface Example {
                @Deprecated
                /** 已有说明。 */
                void oldMethod();

                @Query("select 1")
                int count(
                    String value
                );

                default String display(String value) {
                    return value;
                }
            }
            """
        )

        self.assertEqual(["count", "display"], [item.method_name for item in violations])

    def test_ignores_parameter_annotations(self):
        source = """
        public interface Sample {
            String find(@Param(\"id\") Long id);
        }
        """

        methods = MODULE.interface_methods(source)
        self.assertEqual([method.method_name for method in methods], ["find"])

    def test_javadoc_before_multiline_annotation_documents_method(self):
        violations = self.scan(
            """
            public interface Sample {
                /** 查询数据。 */
                @Select(
                    \"select 1\"
                )
                String find();
            }
            """
        )

        self.assertEqual([], violations)

    def test_scan_paths_includes_test_sources(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            path = root / "src" / "test" / "java" / "Sample.java"
            path.parent.mkdir(parents=True)
            path.write_text(
                "public interface Sample { void run(); }\n",
                encoding="utf-8",
            )

            violations = MODULE.scan_paths([root])

        self.assertEqual(["run"], [item.method_name for item in violations])

    def test_nested_interface_methods_are_scanned_once(self):
        violations = self.scan(
            """package sample;
            /** 外层契约。 */
            public interface Outer {
                /** 外层方法。 */
                void outerMethod();

                /** 内层契约。 */
                interface Inner {
                    void innerMethod();
                }
            }
            """
        )

        self.assertEqual(["innerMethod"], [item.method_name for item in violations])

    def test_renders_functional_javadoc_for_method_contract(self):
        rendered = MODULE.render_method_javadoc(
            "findById",
            "KnowledgeResponse",
            ["actor", "id"],
            "    ",
        )

        self.assertIn("根据标识查询对应的数据", rendered)
        self.assertIn("@param actor 当前操作主体上下文", rendered)
        self.assertIn("@param id 目标对象标识", rendered)
        self.assertIn("@return 操作结果", rendered)


if __name__ == "__main__":
    unittest.main()

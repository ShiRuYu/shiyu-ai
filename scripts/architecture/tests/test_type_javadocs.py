import importlib.util
import sys
import tempfile
import unittest
from pathlib import Path


SCRIPT = Path(__file__).parents[1] / "check_type_javadocs.py"
SPEC = importlib.util.spec_from_file_location("check_type_javadocs", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = MODULE
SPEC.loader.exec_module(MODULE)


class TypeJavadocsTest(unittest.TestCase):
    def test_documented_types_and_nested_types_pass(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(
                root,
                """@Deprecated
/** 管理租户知识空间的应用服务。 */
public class KnowledgeService {
    /** 表示嵌套的租户查询结果。 */
    static class Result {}
}
""",
            )

            self.assertEqual([], MODULE.scan_paths([root]))

    def test_missing_type_javadoc_is_reported(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(root, "public class UndocumentedService {}\n")

            issues = MODULE.scan_paths([root])

            self.assertEqual(1, len(issues))
            self.assertEqual("UndocumentedService", issues[0].type_name)

    def test_tests_are_separate_until_explicitly_included(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root) / "modules" / "demo"
            production = root / "src" / "main" / "java"
            tests = root / "src" / "test" / "java"
            production.mkdir(parents=True)
            tests.mkdir(parents=True)
            (production / "Documented.java").write_text(
                "/** 已记录的生产类型。 */\npublic class Documented {}\n",
                encoding="utf-8",
            )
            (tests / "Missing.java").write_text(
                "public class Missing {}\n", encoding="utf-8"
            )

            self.assertEqual([], MODULE.scan_paths([root]))
            self.assertEqual(1, len(MODULE.scan_paths([root], include_tests=True)))

    @staticmethod
    def add_source(root: Path, source: str) -> None:
        root.mkdir(parents=True, exist_ok=True)
        (root / "Sample.java").write_text(source, encoding="utf-8")


if __name__ == "__main__":
    unittest.main()

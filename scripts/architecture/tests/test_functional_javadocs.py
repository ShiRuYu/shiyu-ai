import importlib.util
import json
import sys
import tempfile
import unittest
from pathlib import Path


SCRIPT = Path(__file__).parents[1] / "check_functional_javadocs.py"
SPEC = importlib.util.spec_from_file_location("check_functional_javadocs", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = MODULE
SPEC.loader.exec_module(MODULE)


class FunctionalJavadocsTest(unittest.TestCase):
    def test_functional_class_method_param_and_return_description_passes(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(
                root,
                """/** 管理租户知识空间，并执行租户范围内的检索。 */
public class KnowledgeService {
    /** 根据租户标识查询可用知识空间。 @param tenantId 当前租户标识。 @return 当前租户可用的知识空间。 */
    public String findSpace(long tenantId) { return "space"; }
}
""",
            )

            self.assertEqual([], MODULE.scan_paths([root]))

    def test_template_type_method_param_and_return_are_reported(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(
                root,
                """/** 承载知识模块的领域状态或协作行为，负责维护本类型的职责边界。 */
public class KnowledgeService {
    /** 执行当前类型定义的业务操作。 @param tenantId 参数值，用于执行当前操作。 @return 返回当前操作产生的结果。 */
    public String findSpace(long tenantId) { return "space"; }
}
""",
            )

            issues = MODULE.scan_paths([root])

            self.assertEqual(2, len(issues))
            self.assertEqual(
                {"template-type", "template-method+template-param+template-return"},
                {issue.category for issue in issues},
            )

    def test_name_only_method_description_is_reported_once(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(
                root,
                """public class KnowledgeService {
    /** {@code findSpace} 执行当前类型定义的业务操作。 */
    public String findSpace(long tenantId) { return "space"; }
}
""",
            )

            issues = MODULE.scan_paths([root])

            self.assertEqual(1, len(issues))
            self.assertEqual("name-only", issues[0].category)

    def test_javadoc_marker_inside_string_is_not_parsed_as_comment(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(
                root,
                """/** 配置资源访问并返回跨域过滤器。 */
public class ResourcesConfig {
    /** 创建跨域过滤器并注册资源路径。 @return 已配置的过滤器。 */
    public Object corsFilter() {
        return \"/uploads/**\";
    }
}
""",
            )

            self.assertEqual([], MODULE.scan_paths([root]))

    def test_service_interface_and_interface_operation_templates_are_reported(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(
                root,
                """/**
 * AiModelService 服务接口，负责执行模型领域相关业务操作。
 */
public interface AiModelService {
    /** 执行 {@code pageResponse} 定义的接口操作。 */
    String pageResponse();
}
""",
            )

            issues = MODULE.scan_paths([root])

            self.assertEqual(2, len(issues))
            self.assertEqual({"template-type", "name-only"}, {issue.category for issue in issues})

    def test_multiline_annotations_nested_types_and_overloads_are_scanned(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(
                root,
                """@Deprecated(
        since = "1.0"
)
public class Outer {
    /** {@code load} 执行当前类型定义的业务操作。 */
    public String load(String id) { return id; }

    public static class Nested {
        /** {@code load} 执行当前类型定义的业务操作。 */
        public String load(long id) { return String.valueOf(id); }
    }
}
""",
            )

            issues = MODULE.scan_paths([root])

            self.assertEqual(2, len([issue for issue in issues if issue.category == "name-only"]))
            self.assertEqual(
                {"sample.Outer", "sample.Outer.Nested"},
                {issue.owner for issue in issues},
            )

    def test_records_enums_overrides_and_technical_identifiers_do_not_create_false_positives(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(
                root,
                """/** 表示模型调用能力及其协议版本。 */
public record ModelCapability(String provider) {}

/** 表示模型适配器的生命周期状态。 */
enum AdapterState { READY, FAILED }

interface Parent { String id(); }

class Adapter implements Parent {
    @Override
    public String id() { return "OPENAI:gpt-4o"; }
}
""",
            )

            self.assertEqual([], MODULE.scan_paths([root]))

    def test_empty_javadoc_is_reported_as_missing_description(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(root, "/** */\npublic class Empty {}\n")

            issues = MODULE.scan_paths([root])

            self.assertEqual(1, len(issues))
            self.assertEqual("missing-description", issues[0].category)

    def test_empty_throws_description_is_reported(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(
                root,
                """public class ThrowsExample {
    /** 执行租户切换。 @throws IllegalStateException */
    public void switchTenant() {}
}
""",
            )

            issues = MODULE.scan_paths([root])

            self.assertEqual(1, len(issues))
            self.assertEqual("missing-description", issues[0].category)

    def test_multiline_tags_keep_description_and_detect_empty_return(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(
                root,
                """public class MultilineDoc {
    /**
     * 根据租户标识加载知识空间。
     * @param tenantId 当前租户的唯一标识。
     * @return
     */
    public String load(long tenantId) { return \"space\"; }
}
""",
            )

            issues = MODULE.scan_paths([root])

            self.assertEqual(1, len(issues))
            self.assertEqual("missing-description", issues[0].category)
            self.assertNotIn("{", issues[0].member)

    def test_generic_type_parameter_is_parsed_as_a_parameter(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(
                root,
                """public class GenericDoc {
    /** 转换模型值。 @param <T> 参数值，用于执行当前操作。 @return 转换后的模型值。 */
    public <T> T convert(T value) { return value; }
}
""",
            )

            issues = MODULE.scan_paths([root])

            self.assertEqual(1, len(issues))
            self.assertEqual("template-param", issues[0].category)

    def test_generic_throws_description_is_marked_for_review(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(
                root,
                """public class ThrowsReview {
    /** 查询租户知识空间。 @throws IllegalStateException 异常。 */
    public String load() { return \"space\"; }
}
""",
            )

            issues = MODULE.scan_paths([root])

            self.assertEqual(1, len(issues))
            self.assertEqual("review", issues[0].category)

    def test_production_and_test_sources_are_marked_separately(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(root, "/** 执行当前类型定义的业务操作。 */\nclass Production {}")
            test_source = root / "modules/sample/src/test/java/sample/ProductionTest.java"
            test_source.parent.mkdir(parents=True, exist_ok=True)
            test_source.write_text(
                "package sample;\n/** 验证 Production 的功能、边界条件和集成行为。 */\nclass ProductionTest {}\n",
                encoding="utf-8",
            )

            issues = MODULE.scan_paths([root], include_tests=True)

            self.assertEqual({"production", "test"}, {issue.source_set for issue in issues})

    def test_baseline_allows_existing_issue_but_rejects_new_issue(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(root, "/** 执行当前类型定义的业务操作。 */\nclass Existing {}")
            issues = MODULE.scan_paths([root])
            baseline = {issues[0].key: issues[0].to_dict()}

            self.assertEqual([], MODULE.new_issues(issues, baseline))
            self.add_source(root, "/** 执行当前类型定义的业务操作。 */\nclass New {}", "New.java")
            new_issues = MODULE.new_issues(MODULE.scan_paths([root]), baseline)
            self.assertTrue(any(issue.owner.endswith(".New") for issue in new_issues))

    def test_report_is_sorted_and_contains_module_and_locations(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            self.add_source(root, "/** 执行当前类型定义的业务操作。 */\nclass Zed {}", "Zed.java")
            self.add_source(root, "/** 执行当前类型定义的业务操作。 */\nclass Alpha {}", "Alpha.java")

            report = MODULE.render_report(MODULE.scan_paths([root]))

            self.assertLess(report.index("Alpha"), report.index("Zed"))
            self.assertIn("sample", report)
            self.assertIn("template-type", report)

    def add_source(self, root: Path, content: str, name: str = "KnowledgeService.java") -> Path:
        source = root / "modules/sample/src/main/java/sample" / name
        source.parent.mkdir(parents=True, exist_ok=True)
        source.write_text("package sample;\n" + content, encoding="utf-8")
        return source


if __name__ == "__main__":
    unittest.main()

import importlib.util
import sys
import tempfile
import unittest
from pathlib import Path


SCRIPT = Path(__file__).parents[1] / "check_chinese_comments.py"
SPEC = importlib.util.spec_from_file_location("check_chinese_comments", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = MODULE
SPEC.loader.exec_module(MODULE)


class ChineseCommentsTest(unittest.TestCase):
    def test_interface_without_type_javadoc_is_reported(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            source = root / "Missing.java"
            source.write_text("package sample;\npublic interface Missing {}\n", encoding="utf-8")

            violations = MODULE.scan_file(source)

            self.assertTrue(any("接口缺少中文类型说明" in item.message for item in violations))

    def test_interface_with_chinese_type_javadoc_passes(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            source = root / "Documented.java"
            source.write_text(
                "/**\n * 该接口负责演示接口契约。\n */\npublic interface Documented {}\n",
                encoding="utf-8",
            )

            self.assertEqual(MODULE.scan_file(source), [])

    def test_interface_with_multiline_annotation_and_chinese_javadoc_passes(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            source = root / "Annotated.java"
            source.write_text(
                "/**\n"
                " * 该接口负责演示多行注解场景。\n"
                " */\n"
                "@Target(\n"
                "        value = {ElementType.METHOD, ElementType.FIELD}\n"
                ")\n"
                "public @interface Annotated {}\n",
                encoding="utf-8",
            )

            self.assertEqual(MODULE.scan_file(source), [])

    def test_natural_language_english_comment_is_reported(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            source = root / "English.java"
            source.write_text("// This value is used for the cache.\nclass English {}\n", encoding="utf-8")

            violations = MODULE.scan_file(source)

            self.assertTrue(any("英文自然语言注释" in item.message for item in violations))

    def test_short_english_sentence_is_reported(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            source = root / "ShortEnglish.java"
            source.write_text(
                "/** Starts the reusable AI platform without loading a business implementation. */\n"
                "class ShortEnglish {}\n",
                encoding="utf-8",
            )

            violations = MODULE.scan_file(source)

            self.assertTrue(any("英文自然语言注释" in item.message for item in violations))

    def test_english_heading_is_reported(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            source = root / "EnglishHeading.java"
            source.write_text("// Runtime event\nclass EnglishHeading {}\n", encoding="utf-8")

            violations = MODULE.scan_file(source)

            self.assertTrue(any("英文自然语言注释" in item.message for item in violations))

    def test_template_prefix_is_reported(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            source = root / "Prefixed.java"
            source.write_text(
                "/** 中文接口说明：定义 Prefixed 的职责边界。 */\n"
                "interface Prefixed {}\n",
                encoding="utf-8",
            )

            violations = MODULE.scan_file(source)

            self.assertTrue(any("模板前缀" in item.message for item in violations))

    def test_technical_identifiers_are_not_reported(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            source = root / "Technical.java"
            source.write_text("// Class-Path opennlp-tools-2.5.9.jar\nclass Technical {}\n", encoding="utf-8")

            self.assertEqual(MODULE.scan_file(source), [])

    def test_generated_and_target_paths_are_excluded(self):
        with tempfile.TemporaryDirectory() as raw_root:
            root = Path(raw_root)
            generated = root / "target" / "Generated.java"
            generated.parent.mkdir()
            generated.write_text("// This should be ignored.\n", encoding="utf-8")

            self.assertEqual(MODULE.scan_paths([root]), [])

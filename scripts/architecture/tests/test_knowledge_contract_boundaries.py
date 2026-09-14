"""Documentation rules for knowledge contract APIs."""

from pathlib import Path
import unittest


REPOSITORY_ROOT = Path(__file__).parents[3]
CONTRACT_ROOT = (
    REPOSITORY_ROOT
    / "modules/domains/knowledge/shiyu-knowledge-contract/src/main/java/com/shiyu/ai/knowledge/contract"
)


class KnowledgeContractBoundaryTest(unittest.TestCase):
    def test_knowledge_ports_have_class_and_method_documentation(self):
        api_package = CONTRACT_ROOT / "api"

        method_names = {
            "KnowledgePathPort": ("generatePath", "findMissingPrerequisites"),
            "KnowledgePointPort": ("getResponse",),
            "KnowledgeRelationPort": ("getPrerequisites",),
        }
        for interface_name, methods in method_names.items():
            with self.subTest(interface_name=interface_name):
                source = (api_package / f"{interface_name}.java").read_text(encoding="utf-8")
                self.assertIn("package com.shiyu.ai.knowledge.contract.api;", source)
                self.assertRegex(
                    source,
                    rf"/\*\*[\s\S]*?\*/\s*public interface {interface_name}\b",
                )
                for method_name in methods:
                    with self.subTest(method_name=method_name):
                        self.assertRegex(
                            source,
                            rf"/\*\*[\s\S]*?\*/\s*(?:[\w<>, ?\[\].]+\s+)+{method_name}\s*\(",
                        )

        self.assertTrue((api_package / "KnowledgeRetrievalService.java").is_file())


if __name__ == "__main__":
    unittest.main()

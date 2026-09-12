import tempfile
import unittest
from pathlib import Path

from scripts.architecture.check_domain_module_dependencies import analyze_repository


POM = """<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.shiyu.ai</groupId>
  <artifactId>{artifact_id}</artifactId>
  <version>1</version>
  <dependencies>{dependencies}</dependencies>
</project>
"""
DEPENDENCY = """<dependency><groupId>com.shiyu.ai</groupId><artifactId>{artifact_id}</artifactId><version>1</version></dependency>"""


class DomainBoundaryTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)

    def tearDown(self):
        self.temp.cleanup()

    def add_module(self, relative: str, artifact: str, dependencies=(), sources=None):
        module = self.root / relative
        module.mkdir(parents=True)
        deps = "".join(DEPENDENCY.format(artifact_id=item) for item in dependencies)
        (module / "pom.xml").write_text(POM.format(artifact_id=artifact, dependencies=deps), encoding="utf-8")
        for path, content in (sources or {}).items():
            source = module / "src/main/java" / path
            source.parent.mkdir(parents=True, exist_ok=True)
            source.write_text(content, encoding="utf-8")

    def add_required_web_module(self):
        self.add_module("modules/applications/shiyu-ai-web", "shiyu-ai-web")

    def test_rejects_cross_domain_implementation_dependency_with_real_leaf_names(self):
        self.add_module(
            "modules/domains/alpha/shiyu-alpha-implementation",
            "shiyu-alpha-implementation",
            dependencies=["shiyu-beta-implementation"],
        )
        self.add_module("modules/domains/beta/shiyu-beta-implementation", "shiyu-beta-implementation")
        self.add_required_web_module()

        violations = analyze_repository(self.root)

        self.assertTrue(any("shiyu-beta-implementation" in item for item in violations))

    def test_rejects_cross_business_implementation_dependency(self):
        self.add_module(
            "modules/business/education/shiyu-education-implementation",
            "shiyu-education-implementation",
            dependencies=["shiyu-knowledge-implementation"],
        )
        self.add_module(
            "modules/domains/knowledge/shiyu-knowledge-implementation",
            "shiyu-knowledge-implementation",
        )
        self.add_required_web_module()

        violations = analyze_repository(self.root)

        self.assertTrue(any("shiyu-knowledge-implementation" in item for item in violations))

    def test_allows_an_implementation_to_reference_its_own_artifact(self):
        self.add_module(
            "modules/domains/alpha/shiyu-alpha-contract",
            "shiyu-alpha-contract",
        )
        self.add_module(
            "modules/domains/alpha/shiyu-alpha-implementation",
            "shiyu-alpha-implementation",
            dependencies=["shiyu-alpha-implementation"],
        )
        self.add_required_web_module()

        self.assertEqual([], analyze_repository(self.root))

    def test_rejects_framework_dependency_in_contract_pom(self):
        self.add_module(
            "modules/domains/alpha/shiyu-alpha-contract",
            "shiyu-alpha-contract",
        )
        pom = self.root / "modules/domains/alpha/shiyu-alpha-contract/pom.xml"
        pom.write_text(
            POM.format(
                artifact_id="shiyu-alpha-contract",
                dependencies=(
                    "<dependency><groupId>org.springframework</groupId>"
                    "<artifactId>spring-context</artifactId><version>1</version></dependency>"
                ),
            ),
            encoding="utf-8",
        )
        self.add_module(
            "modules/domains/alpha/shiyu-alpha-implementation",
            "shiyu-alpha-implementation",
        )
        self.add_required_web_module()

        violations = analyze_repository(self.root)

        self.assertTrue(any("spring-context" in item for item in violations))

    def test_rejects_empty_domain_scan(self):
        self.assertTrue(any("No implementation modules" in item for item in analyze_repository(self.root)))

    def test_missing_actual_web_pom_is_a_violation(self):
        self.add_module("modules/domains/alpha/shiyu-alpha-contract", "shiyu-alpha-contract")

        violations = analyze_repository(self.root)

        self.assertTrue(any("modules/applications/shiyu-ai-web/pom.xml" in item for item in violations))

    def test_rejects_import_of_another_domain_implementation_type(self):
        self.add_module(
            "modules/domains/alpha/shiyu-alpha-implementation",
            "shiyu-alpha-implementation",
            sources={
                "com/example/alpha/UseBeta.java": (
                    "package com.example.alpha;\n"
                    "import com.example.beta.BetaInternal;\n"
                    "public class UseBeta {}\n"
                )
            },
        )
        self.add_module(
            "modules/domains/beta/shiyu-beta-implementation",
            "shiyu-beta-implementation",
            sources={
                "com/example/beta/BetaInternal.java": (
                    "package com.example.beta;\npublic class BetaInternal {}\n"
                )
            },
        )
        self.add_required_web_module()

        violations = analyze_repository(self.root)

        self.assertTrue(any("com.example.beta.BetaInternal" in item for item in violations))

    def test_rejects_contract_import_of_implementation_type(self):
        self.add_module(
            "modules/domains/alpha/shiyu-alpha-contract",
            "shiyu-alpha-contract",
            sources={
                "com/example/alpha/AlphaApi.java": (
                    "package com.example.alpha;\n"
                    "import com.example.beta.BetaInternal;\n"
                    "public interface AlphaApi {}\n"
                )
            },
        )
        self.add_module(
            "modules/domains/beta/shiyu-beta-implementation",
            "shiyu-beta-implementation",
            sources={
                "com/example/beta/BetaInternal.java": (
                    "package com.example.beta;\npublic class BetaInternal {}\n"
                )
            },
        )
        self.add_required_web_module()

        violations = analyze_repository(self.root)

        self.assertTrue(any("com.example.beta.BetaInternal" in item for item in violations))

    def test_rejects_legacy_package_in_conversation_implementation(self):
        self.add_module(
            "modules/domains/conversation/shiyu-conversation-implementation",
            "shiyu-conversation-implementation",
            sources={
                "com/shiyu/ai/conversation/LegacyService.java": (
                    "package com.shiyu.ai.conversation;\npublic class LegacyService {}\n"
                )
            },
        )
        self.add_required_web_module()

        violations = analyze_repository(self.root)

        self.assertTrue(any("LegacyService.java" in item and "implementation" in item for item in violations))

    def test_agent_contract_has_no_langgraph_dependency(self):
        pom = Path(__file__).parents[3] / "modules/domains/agent/shiyu-agent-contract/pom.xml"
        self.assertNotIn("langgraph4j", pom.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()

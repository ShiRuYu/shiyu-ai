import json
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
COMMON_CORE = ROOT / "modules" / "infrastructure" / "shiyu-common-core"
EVENT_MODULE = ROOT / "modules" / "infrastructure" / "shiyu-common-event"
BOOTSTRAP = ROOT / "modules" / "applications" / "shiyu-ai-bootstrap"


class CommonCoreBoundaryTest(unittest.TestCase):
    def test_business_module_properties_binding_is_removed(self):
        properties = (
            COMMON_CORE
            / "src"
            / "main"
            / "java"
            / "com"
            / "shiyu"
            / "ai"
            / "common"
            / "core"
            / "module"
            / "BusinessModuleProperties.java"
        )
        composition = (
            ROOT
            / "modules"
            / "applications"
            / "shiyu-platform-composition"
            / "src"
            / "main"
            / "java"
            / "com"
            / "shiyu"
            / "ai"
            / "composition"
            / "config"
            / "PlatformCompositionAutoConfiguration.java"
        )

        self.assertFalse(properties.exists())
        source = composition.read_text(encoding="utf-8")
        self.assertNotIn("BusinessModuleProperties", source)
        self.assertNotIn("EnableConfigurationProperties", source)

    def test_state_properties_use_lombok_accessors(self):
        paths = [
            EVENT_MODULE
            / "src/main/java/com/shiyu/ai/common/core/event/EventInfrastructureProperties.java",
            ROOT
            / "modules/infrastructure/shiyu-common-storage/src/main/java/com/shiyu/ai/common/storage/config/StorageMigrationProperties.java",
            ROOT
            / "modules/infrastructure/shiyu-common-storage/src/main/java/com/shiyu/ai/common/storage/config/RedisInfrastructureProperties.java",
            ROOT
            / "modules/infrastructure/shiyu-common-storage/src/main/java/com/shiyu/ai/common/storage/config/FileInfrastructureProperties.java",
            ROOT
            / "modules/infrastructure/shiyu-common-vector/src/main/java/com/shiyu/ai/common/vector/config/VectorInfrastructureProperties.java",
            ROOT
            / "modules/infrastructure/shiyu-common-mybatis/src/main/java/com/shiyu/ai/common/mybatis/config/DatabaseInfrastructureProperties.java",
        ]

        for path in paths:
            source = path.read_text(encoding="utf-8")
            self.assertRegex(source, r"import lombok\.Getter;")
            self.assertRegex(source, r"import lombok\.Setter;")
            self.assertRegex(source, r"@(Getter|Setter)")

    def test_common_core_does_not_carry_optional_stacks(self):
        pom = (COMMON_CORE / "pom.xml").read_text(encoding="utf-8")
        for artifact in (
            "spring-boot-starter-webmvc",
            "spring-boot-starter-log4j2",
            "jsoup",
            "spring-kafka",
        ):
            self.assertNotIn(f"<artifactId>{artifact}</artifactId>", pom)

    def test_log4j2_is_owned_by_the_application_boundary(self):
        core_pom = (COMMON_CORE / "pom.xml").read_text(encoding="utf-8")
        bootstrap_pom = (BOOTSTRAP / "pom.xml").read_text(encoding="utf-8")

        self.assertNotIn("<artifactId>spring-boot-starter-log4j2</artifactId>", core_pom)
        self.assertIn("<artifactId>spring-boot-starter-log4j2</artifactId>", bootstrap_pom)
        self.assertIn("<artifactId>spring-boot-starter-logging</artifactId>", core_pom)

    def test_event_module_contains_event_sources_and_is_reactor_module(self):
        root_pom = (ROOT / "pom.xml").read_text(encoding="utf-8")
        event_pom = EVENT_MODULE / "pom.xml"
        event_source = EVENT_MODULE / "src/main/java/com/shiyu/ai/common/core/event"

        self.assertIn(
            "<module>modules/infrastructure/shiyu-common-event</module>", root_pom
        )
        self.assertTrue(event_pom.is_file())
        self.assertTrue(event_source.is_dir())
        self.assertGreaterEqual(len(list(event_source.glob("*.java"))), 8)

        event_imports = "\n".join(
            path.read_text(encoding="utf-8")
            for path in event_source.glob("*.java")
        )
        self.assertNotIn("BusinessModuleProperties", event_imports)

    def test_education_module_metadata_describes_toggle(self):
        metadata = (
            ROOT
            / "modules/business/education/shiyu-education-implementation/src/main/resources/META-INF/additional-spring-configuration-metadata.json"
        )
        document = json.loads(metadata.read_text(encoding="utf-8"))
        properties = {item["name"]: item for item in document["properties"]}
        self.assertEqual("java.lang.Boolean", properties["shiyu.modules.education.enabled"]["type"])
        self.assertTrue(properties["shiyu.modules.education.enabled"]["defaultValue"])


if __name__ == "__main__":
    unittest.main()

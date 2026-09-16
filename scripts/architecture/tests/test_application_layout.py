import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]


class ApplicationLayoutTest(unittest.TestCase):
    def test_only_bootstrap_is_the_executable_application(self):
        root_pom = (ROOT / "pom.xml").read_text(encoding="utf-8")
        composition_module = ROOT / "modules" / "applications" / "shiyu-platform-composition"
        composition_pom = composition_module / "pom.xml"
        bootstrap_pom = (
            ROOT / "modules" / "applications" / "shiyu-ai-bootstrap" / "pom.xml"
        )

        self.assertIn(
            "<module>modules/applications/shiyu-platform-composition</module>",
            root_pom,
        )
        self.assertNotIn("modules/applications/shiyu-application", root_pom)
        self.assertTrue(composition_pom.is_file())
        self.assertIn(
            "<artifactId>shiyu-platform-composition</artifactId>",
            composition_pom.read_text(encoding="utf-8"),
        )
        self.assertIn(
            "<mainClass>com.shiyu.ai.bootstrap.ShiyuBootstrapApplication</mainClass>",
            bootstrap_pom.read_text(encoding="utf-8"),
        )


if __name__ == "__main__":
    unittest.main()

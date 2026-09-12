import csv
import tempfile
import unittest
from pathlib import Path

from scripts.architecture.inventory_java_packages import build_inventory, write_inventory


POM = """<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example</groupId>
  <artifactId>{artifact_id}</artifactId>
  <version>1</version>
</project>
"""


class PackageInventoryTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)

    def tearDown(self):
        self.temp.cleanup()

    def add_module(self, name: str, sources: dict[str, str]) -> Path:
        module = self.root / "modules" / name
        module.mkdir(parents=True)
        (module / "pom.xml").write_text(POM.format(artifact_id=name), encoding="utf-8")
        for relative, content in sources.items():
            path = module / relative
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding="utf-8")
        return module

    def test_reports_split_packages_between_modules(self):
        self.add_module(
            "alpha-contract",
            {"src/main/java/com/example/shared/Alpha.java": "package com.example.shared; public class Alpha {}"},
        )
        self.add_module(
            "beta-implementation",
            {"src/main/java/com/example/shared/Beta.java": "package com.example.shared; public class Beta {}"},
        )

        inventory = build_inventory(self.root)

        self.assertEqual(
            {"alpha-contract", "beta-implementation"},
            set(inventory.split_packages["com.example.shared"]),
        )

    def test_duplicate_fqcn_is_a_fatal_problem(self):
        source = "package com.example.duplicate; public class Same {}"
        self.add_module("alpha", {"src/main/java/com/example/duplicate/Same.java": source})
        self.add_module("beta", {"src/main/java/com/example/duplicate/Same.java": source})

        inventory = build_inventory(self.root)

        self.assertIn("com.example.duplicate.Same", inventory.duplicate_fqcns)
        self.assertTrue(inventory.has_errors)

    def test_excludes_test_and_target_sources_from_production_inventory(self):
        self.add_module(
            "alpha",
            {
                "src/main/java/com/example/Main.java": "package com.example; public class Main {}",
                "src/test/java/com/example/MainTest.java": "package com.example; public class MainTest {}",
                "target/generated-sources/com/example/Generated.java": "package com.example; public class Generated {}",
            },
        )

        inventory = build_inventory(self.root)

        self.assertEqual(["com.example.Main"], [item.old_fqcn for item in inventory.entries])

    def test_writes_stable_csv_with_module_and_migration_columns(self):
        self.add_module(
            "alpha-contract",
            {"src/main/java/com/example/api/Greeting.java": "package com.example.api; public interface Greeting {}"},
        )
        destination = self.root / "inventory.csv"

        write_inventory(build_inventory(self.root), destination)

        with destination.open(encoding="utf-8", newline="") as stream:
            rows = list(csv.DictReader(stream))
        self.assertEqual(
            ["source_path", "module", "old_fqcn", "target_fqcn", "role", "phase"],
            list(rows[0]),
        )
        self.assertEqual("alpha-contract", rows[0]["module"])
        self.assertEqual("com.example.api.Greeting", rows[0]["target_fqcn"])
        self.assertEqual("contract", rows[0]["role"])

    def test_reports_cross_module_imports_and_wildcards_for_review(self):
        self.add_module(
            "alpha-contract",
            {"src/main/java/com/example/api/Alpha.java": "package com.example.api; public class Alpha {}"},
        )
        self.add_module(
            "beta-implementation",
            {
                "src/main/java/com/example/beta/Beta.java": (
                    "package com.example.beta;\n"
                    "import com.example.api.Alpha;\n"
                    "import static com.example.api.Alpha.*;\n"
                    "public class Beta {}\n"
                )
            },
        )

        inventory = build_inventory(self.root)

        self.assertTrue(any(item.imported == "com.example.api.Alpha" for item in inventory.cross_module_imports))
        self.assertTrue(any("com.example.api.Alpha.*" in item for item in inventory.manual_review))

    def test_reports_third_party_dependencies_declared_by_contract_modules(self):
        module = self.add_module(
            "alpha-contract",
            {"src/main/java/com/example/api/Alpha.java": "package com.example.api; public class Alpha {}"},
        )
        (module / "pom.xml").write_text(
            POM.format(artifact_id="alpha-contract").replace(
                "</project>",
                "<dependencies><dependency><groupId>org.external</groupId>"
                "<artifactId>framework-core</artifactId><version>1</version>"
                "</dependency></dependencies></project>",
            ),
            encoding="utf-8",
        )

        inventory = build_inventory(self.root)

        self.assertEqual(["alpha-contract -> org.external:framework-core"], inventory.contract_external_dependencies)

    def test_reports_package_path_mismatches_as_errors(self):
        self.add_module(
            "alpha",
            {"src/main/java/com/example/wrong/Alpha.java": "package com.example.expected; public class Alpha {}"},
        )

        inventory = build_inventory(self.root)

        self.assertTrue(inventory.has_errors)
        self.assertEqual(1, len(inventory.path_mismatches))
        self.assertIn("com/example/expected/Alpha.java", inventory.path_mismatches[0])

    def test_classifies_business_education_as_the_education_migration_phase(self):
        self.add_module(
            "business/education/shiyu-education-contract",
            {
                "src/main/java/com/example/education/api/EducationApi.java": (
                    "package com.example.education.api; public interface EducationApi {}"
                )
            },
        )

        inventory = build_inventory(self.root)

        self.assertEqual("4F", inventory.entries[0].phase)


if __name__ == "__main__":
    unittest.main()

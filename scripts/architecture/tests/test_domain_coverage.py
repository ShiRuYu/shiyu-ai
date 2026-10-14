"""Regression tests for implementation coverage discovery and fail-closed behavior."""

from __future__ import annotations

import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from scripts.architecture.check_domain_coverage import implementation_modules


SCRIPT = Path(__file__).resolve().parents[1] / "check_domain_coverage.py"


class DomainCoverageDiscoveryTest(unittest.TestCase):
    def test_discovers_renamed_domain_and_business_modules(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            expected = [
                root / "modules/domains/agent/shiyu-agent-implementation",
                root / "modules/business/education/shiyu-education-implementation",
            ]
            for module in expected:
                (module / "src/main/java").mkdir(parents=True)
            self.assertEqual(sorted(expected), implementation_modules(root))

    def test_zero_modules_is_failure_even_when_missing_reports_are_allowed(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            result = subprocess.run(
                [sys.executable, str(SCRIPT), "--root", directory, "--allow-missing"],
                capture_output=True,
                text=True,
                check=False,
            )
            self.assertNotEqual(0, result.returncode)
            self.assertIn("no implementation modules discovered", result.stderr)


if __name__ == "__main__":
    unittest.main()

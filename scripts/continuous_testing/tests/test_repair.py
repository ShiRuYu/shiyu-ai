import sys, tempfile, unittest
from pathlib import Path
from scripts.continuous_testing.repair import RepairAdapter, RepairAttempt, RepairPolicy

class RepairTests(unittest.TestCase):
    def test_executes_only_in_isolated_worktree(self):
        with tempfile.TemporaryDirectory() as d:
            path = Path(d); request = RepairAttempt("f1", "abc", path, (sys.executable, "-c", "print('repair')"), 0)
            result = RepairAdapter().execute(request)
            self.assertEqual(result.returncode, 0); self.assertIn("repair", result.stdout)
    def test_stops_after_three_attempts(self):
        self.assertTrue(RepairPolicy.allowed(2)); self.assertFalse(RepairPolicy.allowed(3))
        with tempfile.TemporaryDirectory() as d:
            request = RepairAttempt("f1", "abc", Path(d), (sys.executable, "-c", "pass"), 3)
            with self.assertRaises(RuntimeError): RepairAdapter().execute(request)

if __name__ == "__main__": unittest.main()

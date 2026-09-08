import tempfile, unittest
from pathlib import Path
from scripts.continuous_testing.resources import Limits, admission, directory_size

class ResourceTests(unittest.TestCase):
    def test_rejects_too_many_heavy_tasks(self):
        with tempfile.TemporaryDirectory() as d:
            allowed, reason = admission(Path(d), 2)
            self.assertFalse(allowed); self.assertEqual(reason, "heavy-task-limit")
    def test_runtime_budget_is_enforced(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d); (root / ".testing").mkdir(); (root / ".testing" / "x").write_bytes(b"1234")
            allowed, reason = admission(root, 0, Limits(max_runtime_bytes=1))
            self.assertFalse(allowed); self.assertEqual(reason, "runtime-budget"); self.assertEqual(directory_size(root / ".testing"), 4)

if __name__ == "__main__": unittest.main()

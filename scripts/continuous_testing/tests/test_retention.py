import tempfile, time, unittest
from pathlib import Path
from scripts.continuous_testing.retention import prune

class RetentionTests(unittest.TestCase):
    def test_prunes_success_and_failure_but_keeps_regression(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d); (root / "runs").mkdir(); (root / "failures").mkdir(); (root / "regressions").mkdir()
            old = time.time() - 40 * 86400
            for directory in ("runs", "failures", "regressions"):
                path = root / directory / "old.txt"; path.write_text("x"); import os; os.utime(path, (old, old))
            removed = prune(root, time.time())
            self.assertEqual({p.parent.name for p in removed}, {"runs", "failures"})
            self.assertTrue((root / "regressions" / "old.txt").exists())

if __name__ == "__main__": unittest.main()

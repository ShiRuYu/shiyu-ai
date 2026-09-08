import subprocess, tempfile, unittest
from pathlib import Path
from scripts.continuous_testing.worktree import repository_fingerprint, create_worktree, remove_worktree

class WorktreeTests(unittest.TestCase):
    def test_fingerprint_changes_with_commit(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d); subprocess.run(["git", "init"], cwd=root, check=True, capture_output=True)
            subprocess.run(["git", "config", "user.email", "test@example.invalid"], cwd=root, check=True)
            subprocess.run(["git", "config", "user.name", "test"], cwd=root, check=True)
            (root / "a").write_text("1"); subprocess.run(["git", "add", "a"], cwd=root, check=True); subprocess.run(["git", "commit", "-m", "one"], cwd=root, check=True, capture_output=True)
            first = repository_fingerprint(root); (root / "a").write_text("2")
            self.assertNotEqual(first, repository_fingerprint(root))
    def test_creates_and_removes_detached_snapshot(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d); subprocess.run(["git", "init"], cwd=root, check=True, capture_output=True)
            subprocess.run(["git", "config", "user.email", "test@example.invalid"], cwd=root, check=True); subprocess.run(["git", "config", "user.name", "test"], cwd=root, check=True)
            (root / "a").write_text("1"); subprocess.run(["git", "add", "a"], cwd=root, check=True); subprocess.run(["git", "commit", "-m", "one"], cwd=root, check=True, capture_output=True)
            target = root.parent / (root.name + "-snapshot"); create_worktree(root, target)
            self.assertEqual((target / "a").read_text(), "1"); remove_worktree(root, target)
            self.assertFalse(target.exists())

if __name__ == "__main__": unittest.main()

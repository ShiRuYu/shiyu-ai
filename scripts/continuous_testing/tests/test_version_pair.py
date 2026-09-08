import tempfile, unittest
from pathlib import Path
from scripts.continuous_testing.version_pair import VersionPair

class VersionPairTests(unittest.TestCase):
    def test_promotes_one_atomic_pointer(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d); pointer = root / ".testing" / "current.json"
            pair = VersionPair(Path.cwd(), Path.cwd(), pointer); version = pair.promote()
            self.assertEqual(version["backend_sha"], version["frontend_sha"]); self.assertTrue(pointer.exists())

if __name__ == "__main__": unittest.main()

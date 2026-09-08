import tempfile, unittest
from pathlib import Path
from scripts.continuous_testing.daemon import Daemon

class DaemonTests(unittest.TestCase):
    def test_tick_detects_stable_then_changed_snapshot(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d); state = root / ".testing"
            daemon = Daemon(Path.cwd(), Path.cwd(), state)
            self.assertFalse(daemon.tick()); self.assertFalse(daemon.tick())
            self.assertFalse((state / "version.json").exists())

if __name__ == "__main__": unittest.main()

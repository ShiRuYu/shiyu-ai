import tempfile, unittest
from pathlib import Path
from scripts.continuous_testing.daemon import Daemon

class DaemonTests(unittest.TestCase):
    def test_tick_detects_stable_then_changed_snapshot(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d); state = root / ".testing"
            daemon = Daemon(Path.cwd(), Path.cwd(), state)
            self.assertTrue(daemon.tick()); self.assertFalse(daemon.tick())
            (state / "version.json").write_text("{}")
            self.assertTrue(daemon.tick())

if __name__ == "__main__": unittest.main()

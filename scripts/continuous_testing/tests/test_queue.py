import tempfile, unittest, json
from pathlib import Path
from scripts.continuous_testing.daemon import Daemon

class QueueTests(unittest.TestCase):
    def test_stable_change_creates_pending_version_record(self):
        with tempfile.TemporaryDirectory() as d:
            state = Path(d) / ".testing"; daemon = Daemon(Path.cwd(), Path.cwd(), state)
            daemon.detector.stable_seconds = 0
            self.assertTrue(daemon.tick())
            pending = json.loads((state / "pending-version.json").read_text())
            self.assertEqual(pending["status"], "QUEUED")

if __name__ == "__main__": unittest.main()

import tempfile, unittest, json
from pathlib import Path
from scripts.continuous_testing.daemon import Daemon

class QueueTests(unittest.TestCase):
    def test_stable_change_creates_pending_version_record(self):
        with tempfile.TemporaryDirectory() as d:
            state = Path(d) / ".testing"; daemon = Daemon(Path.cwd(), Path.cwd(), state)
            daemon.detector.stable_seconds = 0
            daemon.tick(); self.assertTrue(daemon.tick())
            pending = json.loads((state / "pending-version.json").read_text())
            self.assertEqual(pending["status"], "QUEUED")

    def test_new_snapshot_does_not_replace_queued_version(self):
        with tempfile.TemporaryDirectory() as d:
            state = Path(d) / ".testing"; daemon = Daemon(Path.cwd(), Path.cwd(), state)
            daemon.detector.stable_seconds = 0
            daemon.tick(); daemon.tick()
            original = json.loads((state / "pending-version.json").read_text())
            daemon.detector.fingerprint = "different"
            daemon.tick(); daemon.tick()
            self.assertEqual(original, json.loads((state / "pending-version.json").read_text()))

if __name__ == "__main__": unittest.main()

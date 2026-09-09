import json, tempfile, unittest
from pathlib import Path
from scripts.continuous_testing.queue import VersionQueue

class QueueStateTests(unittest.TestCase):
    def test_claim_is_idempotent_until_complete(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d); root.mkdir(exist_ok=True); (root / "pending-version.json").write_text(json.dumps({"status":"QUEUED","version":{"backend_sha":"a"}}))
            queue = VersionQueue(root); first = queue.claim(); self.assertEqual(first["status"], "RUNNING"); self.assertIsNone(queue.claim()); queue.complete(); self.assertIsNone(queue.claim())

if __name__ == "__main__": unittest.main()

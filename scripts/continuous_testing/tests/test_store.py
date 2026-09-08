import json
import unittest
from pathlib import Path
from tempfile import TemporaryDirectory
from scripts.continuous_testing.store import StateStore

class StoreTests(unittest.TestCase):
    def test_recovers_run_and_task(self):
        with TemporaryDirectory() as d:
            path = Path(d) / "state.sqlite"
            store = StateStore(path); store.initialize()
            run_id = store.create_run("v1", {"backend_sha": "abc"})
            store.update_run(run_id, "RUNNING")
            store.add_task(run_id, "gate", ["python", "-V"], "QUEUED")
            reopened = StateStore(path)
            self.assertEqual(reopened.get_run(run_id)["status"], "RUNNING")
            self.assertEqual(reopened.list_tasks(run_id)[0]["status"], "QUEUED")
    def test_snapshot_is_rebuildable(self):
        with TemporaryDirectory() as d:
            store = StateStore(Path(d) / "state.sqlite"); store.initialize()
            run_id = store.create_run("v1", {"backend_sha": "abc"})
            target = Path(d) / "state.json"; store.write_snapshot(target)
            data = json.loads(target.read_text())
            self.assertEqual(data["runs"][0]["id"], run_id)
    def test_rejects_invalid_status(self):
        with TemporaryDirectory() as d:
            store = StateStore(Path(d) / "state.sqlite"); store.initialize()
            run_id = store.create_run("v1", {})
            with self.assertRaises(ValueError): store.update_run(run_id, "INVALID")

if __name__ == "__main__": unittest.main()

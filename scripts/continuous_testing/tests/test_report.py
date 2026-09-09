import tempfile, unittest
from pathlib import Path
from scripts.continuous_testing.store import StateStore
from scripts.continuous_testing.report import build_report, write_report

class ReportTests(unittest.TestCase):
    def test_keeps_outcomes_separate(self):
        with tempfile.TemporaryDirectory() as d:
            store = StateStore(Path(d) / "state.sqlite"); store.initialize()
            run_id = store.create_run("v1", {}); store.add_task(run_id, "a", ["a"], "PASS"); store.add_task(run_id, "b", ["b"], "TIMEOUT")
            report = build_report(store, run_id)
            self.assertEqual(report["outcomes"]["PASS"], 1); self.assertEqual(report["outcomes"]["TIMEOUT"], 1)
            target = Path(d) / "reports" / "cycle.json"; write_report(store, run_id, target)
            self.assertTrue(target.exists())

if __name__ == "__main__": unittest.main()

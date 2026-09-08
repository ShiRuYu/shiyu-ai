import tempfile, unittest
from pathlib import Path
from scripts.continuous_testing.resources import Limits
from scripts.continuous_testing.scheduler import run_baseline

class SchedulerTests(unittest.TestCase):
    def test_resource_block_pauses_without_running_gate(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d); state = root / ".testing"
            run_id = run_baseline(Path.cwd(), Path.cwd(), state, limits=Limits(max_heavy_tasks=0))
            self.assertEqual((state / "state.sqlite").exists(), True)
            self.assertEqual((state / "pause-reason.txt").read_text(), "heavy-task-limit")

if __name__ == "__main__": unittest.main()

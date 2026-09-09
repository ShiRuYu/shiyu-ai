import tempfile, unittest, json
from pathlib import Path
from scripts.continuous_testing.process_control import stop_owned

class ProcessControlTests(unittest.TestCase):
    def test_missing_lease_is_safe(self):
        with tempfile.TemporaryDirectory() as d: self.assertFalse(stop_owned(Path(d) / "lock"))
    def test_self_pid_is_never_killed(self):
        with tempfile.TemporaryDirectory() as d:
            lock = Path(d) / "lock"; lock.write_text(json.dumps({"pid": __import__('os').getpid()}))
            self.assertFalse(stop_owned(lock))

if __name__ == "__main__": unittest.main()

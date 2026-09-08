import tempfile, unittest
from pathlib import Path
from scripts.continuous_testing.lease import Lease

class LeaseTests(unittest.TestCase):
    def test_only_one_owner(self):
        with tempfile.TemporaryDirectory() as d:
            path = Path(d) / "scheduler.lock"; first = Lease(path); second = Lease(path)
            self.assertTrue(first.acquire()); self.assertFalse(second.acquire()); first.release(); self.assertTrue(second.acquire()); second.release()
    def test_reclaims_dead_owner(self):
        with tempfile.TemporaryDirectory() as d:
            path = Path(d) / "scheduler.lock"; path.write_text('{"pid": 2147483647}')
            lease = Lease(path); self.assertTrue(lease.acquire()); lease.release()

if __name__ == "__main__": unittest.main()

import sys, unittest
from scripts.continuous_testing.runner import run

class RunnerTests(unittest.TestCase):
    def test_records_success(self):
        result = run([sys.executable, "-c", "print('ok')"], ".")
        self.assertEqual(result.status, "PASS")
        self.assertIn("ok", result.output)
    def test_records_timeout(self):
        result = run([sys.executable, "-c", "import time; time.sleep(2)"], ".", timeout_seconds=0)
        self.assertEqual(result.status, "TIMEOUT")

if __name__ == "__main__": unittest.main()

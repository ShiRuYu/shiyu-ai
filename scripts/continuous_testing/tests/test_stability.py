import unittest
from scripts.continuous_testing.daemon import StableChangeDetector

class StabilityTests(unittest.TestCase):
    def test_requires_thirty_seconds_of_same_snapshot(self):
        now = [0.0]
        detector = StableChangeDetector(30, lambda: now[0])
        self.assertFalse(detector.observe("a")); now[0] = 29; self.assertFalse(detector.observe("a"))
        now[0] = 30; self.assertTrue(detector.observe("a")); now[0] = 31; self.assertTrue(detector.observe("a"))
        self.assertFalse(detector.observe("b"))

if __name__ == "__main__": unittest.main()

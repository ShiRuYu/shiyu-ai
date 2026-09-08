import unittest
from scripts.continuous_testing.exploration import Scenario, ScenarioLedger, dispatch_plan

class ExplorationTests(unittest.TestCase):
    def test_seed_is_not_part_of_semantic_identity(self):
        ledger = ScenarioLedger()
        first = Scenario("api-fuzz", "upload", "member", "empty", 100, "serial", "none")
        self.assertTrue(ledger.add(first)); self.assertFalse(ledger.add(first))
    def test_dispatch_ratio(self):
        plan = dispatch_plan()
        self.assertEqual(len(plan), 20)
        self.assertEqual(plan.count("risk-gap"), 12)
        self.assertEqual(plan.count("stale-area"), 5)
        self.assertEqual(plan.count("historical-failure"), 3)

if __name__ == "__main__": unittest.main()

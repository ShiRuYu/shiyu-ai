import unittest, tempfile
from pathlib import Path
from scripts.continuous_testing.exploration import Scenario, ScenarioLedger, dispatch_plan, generate_batch

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

    def test_ledger_survives_restart(self):
        with tempfile.TemporaryDirectory() as d:
            path = Path(d) / "explored-space.json"
            scenario = Scenario("api-fuzz", "search", "member", "empty", 100, "serial", "none")
            self.assertTrue(ScenarioLedger(path).add(scenario))
            self.assertFalse(ScenarioLedger(path).add(scenario))

    def test_batch_keeps_required_purpose_mix_and_is_unique(self):
        ledger = ScenarioLedger()
        batch = generate_batch(ledger)
        self.assertEqual(len(batch), 20)
        self.assertEqual([s.purpose for s in batch].count("risk-gap"), 12)
        self.assertEqual(len({s.key() for s in batch}), 20)

    def test_exhausted_space_fails_explicitly(self):
        ledger = ScenarioLedger()
        exhausted = False
        for _ in range(12):
            try:
                generate_batch(ledger, 20)
            except RuntimeError:
                exhausted = True
                break
        self.assertTrue(exhausted)

if __name__ == "__main__": unittest.main()

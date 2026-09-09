import unittest
from pathlib import Path
from scripts.continuous_testing.gates import all_gates

class GateTests(unittest.TestCase):
    def test_defines_both_real_entrypoints(self):
        gates = all_gates(Path("backend"), Path("frontend"))
        self.assertEqual([g[0] for g in gates], ["backend-gate", "frontend-gate"])
        self.assertIn("verify", gates[0][2])
        self.assertEqual(gates[1][2], ["pnpm", "test:unit"])

if __name__ == "__main__": unittest.main()

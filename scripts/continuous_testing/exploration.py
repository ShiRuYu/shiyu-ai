"""Semantic exploration scenario identity and weighted dispatch."""
from __future__ import annotations
import hashlib, json
from dataclasses import dataclass, asdict

@dataclass(frozen=True)
class Scenario:
    strategy: str
    operation: str
    permission: str
    boundary: str
    data_size: int
    timing: str
    fault: str
    purpose: str = "exploration"

    def key(self) -> str:
        canonical = json.dumps(asdict(self), sort_keys=True, separators=(",", ":"))
        return hashlib.sha256(canonical.encode()).hexdigest()

class ScenarioLedger:
    def __init__(self):
        self._seen: dict[str, str] = {}

    def add(self, scenario: Scenario) -> bool:
        key = scenario.key()
        if key in self._seen:
            return False
        self._seen[key] = scenario.purpose
        return True

    def contains(self, scenario: Scenario) -> bool:
        return scenario.key() in self._seen

def dispatch_plan(total: int = 20) -> list[str]:
    """Return purpose slots with the required 12/5/3 ratio."""
    high = round(total * 12 / 20); stale = round(total * 5 / 20)
    return ["risk-gap"] * high + ["stale-area"] * stale + ["historical-failure"] * (total - high - stale)

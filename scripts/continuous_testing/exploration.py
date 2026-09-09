"""Semantic exploration scenario identity and weighted dispatch."""
from __future__ import annotations
import hashlib, json, os
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
    def __init__(self, path=None):
        self.path = path
        self._seen: dict[str, str] = {}
        if path and path.exists():
            self._seen = json.loads(path.read_text(encoding="utf-8"))

    def add(self, scenario: Scenario) -> bool:
        key = scenario.key()
        if key in self._seen:
            return False
        self._seen[key] = scenario.purpose
        self._save()
        return True

    def _save(self) -> None:
        if not self.path:
            return
        self.path.parent.mkdir(parents=True, exist_ok=True)
        temp = self.path.with_suffix(".tmp")
        temp.write_text(json.dumps(self._seen, ensure_ascii=False, indent=2), encoding="utf-8")
        os.replace(temp, self.path)

    def contains(self, scenario: Scenario) -> bool:
        return scenario.key() in self._seen

def dispatch_plan(total: int = 20) -> list[str]:
    """Return purpose slots with the required 12/5/3 ratio."""
    high = round(total * 12 / 20); stale = round(total * 5 / 20)
    return ["risk-gap"] * high + ["stale-area"] * stale + ["historical-failure"] * (total - high - stale)

def generate_batch(ledger: ScenarioLedger, total: int = 20) -> list[Scenario]:
    """Generate and record a deterministic semantic batch without seed-only duplicates."""
    purposes = dispatch_plan(total)
    operations = ("upload", "search", "delete", "refresh", "stream")
    boundaries = ("empty", "unicode", "max-length", "pagination")
    result: list[Scenario] = []
    index = 0
    attempts = 0
    max_attempts = max(total * 20, 100)
    while len(result) < total and attempts < max_attempts:
        purpose = purposes[len(result)]
        scenario = Scenario("adaptive-v1", operations[index % len(operations)], "member", boundaries[index % len(boundaries)], 100 * (1 + index % 3), "serial", "none", purpose)
        index += 1
        attempts += 1
        if ledger.add(scenario): result.append(scenario)
    if len(result) < total:
        raise RuntimeError("exploration semantic space exhausted; change strategy or expand dimensions")
    return result

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

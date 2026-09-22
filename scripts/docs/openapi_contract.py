"""比较 OpenAPI、运行时导出和文档中的 HTTP 方法及路径。"""

from __future__ import annotations

import argparse
import json
import re
import urllib.request
from pathlib import Path

METHODS = frozenset({"get", "post", "put", "patch", "delete", "head", "options", "trace"})
VARIABLE = re.compile(r"\{([^{}:]+):[^{}]*(?:\{[^{}]*\}[^{}]*)*\}")


def normalize_path(path: str) -> str:
    return VARIABLE.sub(r"{\1}", path)


def operations(spec: dict) -> set[tuple[str, str]]:
    if not isinstance(spec.get("paths"), dict) or not spec["paths"]:
        raise ValueError("OpenAPI paths must be a non-empty object")
    result = {
        (method.upper(), normalize_path(path))
        for path, item in spec["paths"].items()
        for method in item
        if method.lower() in METHODS
    }
    if not result:
        raise ValueError("OpenAPI contains no HTTP operations")
    return result


def differences(expected: set, actual: set) -> list[str]:
    return [f"missing: {method} {path}" for method, path in sorted(expected - actual)] + [
        f"extra: {method} {path}" for method, path in sorted(actual - expected)
    ]


def document_differences(spec: dict, document: str) -> list[str]:
    rows = re.findall(r"^\| (GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS|TRACE) \| `([^`]+)` \|", document, re.M)
    actual = [(method, normalize_path(path)) for method, path in rows]
    duplicate = sorted({item for item in actual if actual.count(item) > 1})
    return differences(operations(spec), set(actual)) + [
        f"duplicate: {method} {path}" for method, path in duplicate
    ]


def fetch_spec(url: str) -> dict:
    with urllib.request.urlopen(url, timeout=60) as response:
        spec = json.load(response)
    operations(spec)
    return spec


def sync_snapshot(spec: dict, snapshot: Path, update: bool = False) -> None:
    current = operations(spec)
    if update:
        snapshot.parent.mkdir(parents=True, exist_ok=True)
        snapshot.write_text(json.dumps(spec, ensure_ascii=False, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    saved = json.loads(snapshot.read_text(encoding="utf-8"))
    errors = differences(current, operations(saved))
    if errors:
        raise ValueError("Snapshot differs from runtime:\n" + "\n".join(errors))
    print(f"OpenAPI snapshot verified: {len(current)} operations")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--url", required=True)
    parser.add_argument("--snapshot", type=Path, required=True)
    parser.add_argument("--update", action="store_true", help="显式更新快照；默认只校验")
    args = parser.parse_args()
    sync_snapshot(fetch_spec(args.url), args.snapshot, args.update)


if __name__ == "__main__":
    main()

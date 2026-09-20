"""Verify that every discovered Java type has a functional Javadoc block.

The functional scanner checks the meaning of comments that exist.  This
companion gate checks the coverage boundary: every class, interface, enum and
record in the selected source sets must have a type-level Javadoc comment.
"""

from __future__ import annotations

import argparse
import importlib.util
import sys
from dataclasses import dataclass
from pathlib import Path


SCRIPT = Path(__file__).with_name("check_functional_javadocs.py")
SPEC = importlib.util.spec_from_file_location("functional_javadocs", SCRIPT)
if SPEC is None or SPEC.loader is None:
    raise RuntimeError(f"无法加载功能注释扫描器: {SCRIPT}")
SCANNER = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = SCANNER
SPEC.loader.exec_module(SCANNER)


@dataclass(frozen=True)
class TypeJavadocIssue:
    path: str
    line: int
    source_set: str
    type_name: str
    owner: str


def find_type_doc(source: str, declaration_offset: int):
    candidate = None
    for match in SCANNER.iter_javadocs(source, declaration_offset):
        candidate = match
    if candidate is None:
        return None
    if not SCANNER.annotation_lines_only(source[candidate.end() : declaration_offset]):
        return None
    return candidate


def scan_paths(paths: list[Path], include_tests: bool = False) -> list[TypeJavadocIssue]:
    issues: list[TypeJavadocIssue] = []
    for path in SCANNER.iter_java_files(paths):
        source_set = SCANNER.source_set(path)
        if source_set == "test" and not include_tests:
            continue
        source = path.read_text(encoding="utf-8")
        masked = SCANNER.mask_java(source)
        for type_info in SCANNER.discover_types(source, masked):
            if find_type_doc(source, type_info.declaration_offset) is None:
                issues.append(
                    TypeJavadocIssue(
                        path=SCANNER.module_relative(path),
                        line=type_info.line,
                        source_set=source_set,
                        type_name=type_info.name,
                        owner=type_info.owner,
                    )
                )
    return sorted(issues, key=lambda issue: (issue.path, issue.line, issue.owner))


def main() -> int:
    parser = argparse.ArgumentParser(description="检查所有 Java 类型是否有 Javadoc")
    parser.add_argument("paths", nargs="+", help="待扫描的源码目录或文件")
    parser.add_argument("--include-tests", action="store_true")
    args = parser.parse_args()

    issues = scan_paths([Path(value) for value in args.paths], include_tests=args.include_tests)
    for issue in issues:
        print(f"{issue.path}:{issue.line}: missing-type-javadoc: {issue.owner}")
    production = [issue for issue in issues if issue.source_set == "production"]
    tests = [issue for issue in issues if issue.source_set == "test"]
    print(f"生产类型缺失 {len(production)} 个，测试类型缺失 {len(tests)} 个。")
    return 1 if production else 0


if __name__ == "__main__":
    sys.exit(main())

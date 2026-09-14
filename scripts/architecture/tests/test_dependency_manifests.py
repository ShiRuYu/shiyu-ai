import importlib.util
import io
import sys
import unittest
import zipfile
from pathlib import Path
from contextlib import redirect_stdout


SCRIPT = Path(__file__).parents[1] / "check_dependency_manifests.py"
SPEC = importlib.util.spec_from_file_location("check_dependency_manifests", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = MODULE
SPEC.loader.exec_module(MODULE)


def create_jar(path: Path, class_path: str | None = None) -> None:
    manifest = "Manifest-Version: 1.0\n"
    if class_path is not None:
        manifest += f"Class-Path: {class_path}\n"
    manifest += "\n"
    with zipfile.ZipFile(path, "w") as archive:
        archive.writestr("META-INF/MANIFEST.MF", manifest)


class DependencyManifestTest(unittest.TestCase):
    def test_manifest_without_class_path_has_no_missing_entries(self) -> None:
        with self.subTest("无 Class-Path 清单"):
            with self._temporary_directory() as raw_path:
                tmp_path = Path(raw_path)
                jar = tmp_path / "plain.jar"
                create_jar(jar)

                self.assertEqual(MODULE.find_missing_entries(jar), [])

    def test_manifest_reports_missing_relative_target(self) -> None:
        with self._temporary_directory() as raw_path:
            tmp_path = Path(raw_path)
            jar = tmp_path / "broken.jar"
            create_jar(jar, "missing.jar present.jar")
            (tmp_path / "present.jar").touch()

            self.assertEqual(
                MODULE.find_missing_entries(jar),
                [("missing.jar", tmp_path / "missing.jar")],
            )

    def test_manifest_checker_returns_non_zero_and_chinese_diagnostic(self) -> None:
        with self._temporary_directory() as raw_path:
            tmp_path = Path(raw_path)
            jar = tmp_path / "broken.jar"
            create_jar(jar, "missing.jar")
            output = io.StringIO()

            with redirect_stdout(output):
                result = MODULE.run([str(jar)])

            self.assertEqual(result, 1)
            self.assertIn("清单路径缺失", output.getvalue())
            self.assertIn("missing.jar", output.getvalue())

    def test_registered_missing_entry_is_allowed_explicitly(self) -> None:
        with self._temporary_directory() as raw_path:
            tmp_path = Path(raw_path)
            jar = tmp_path / "broken.jar"
            create_jar(jar, "known-missing.jar")

            self.assertEqual(MODULE.run([str(jar)], {"known-missing.jar"}), 0)

    @staticmethod
    def _temporary_directory():
        import tempfile

        return tempfile.TemporaryDirectory()

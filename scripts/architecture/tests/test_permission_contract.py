import re
import unittest
from pathlib import Path


class PermissionContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.repo = Path(__file__).resolve().parents[3]
        cls.iam_seed = cls.repo / (
            "modules/domains/iam/shiyu-iam-implementation/"
            "src/main/resources/db/baseline/h2/seed/iam/02_auth.sql"
        )
        cls.student_controller = cls.repo / (
            "modules/business/education/shiyu-education-implementation/"
            "src/main/java/com/shiyu/ai/education/implementation/web/controller/"
            "StudentController.java"
        )
        cls.intent_controller = cls.repo / (
            "modules/domains/agent/shiyu-agent-implementation/"
            "src/main/java/com/shiyu/ai/agent/implementation/web/IntentDefController.java"
        )
        cls.knowledge_space_controller = cls.repo / (
            "modules/domains/knowledge/shiyu-knowledge-implementation/"
            "src/main/java/com/shiyu/ai/knowledge/implementation/web/controller/"
            "KnowledgeSpaceController.java"
        )
        cls.knowledge_evaluation_controller = cls.repo / (
            "modules/domains/knowledge/shiyu-knowledge-implementation/"
            "src/main/java/com/shiyu/ai/knowledge/implementation/web/controller/"
            "KnowledgeEvaluationController.java"
        )
        cls.knowledge_operations_controller = cls.repo / (
            "modules/domains/knowledge/shiyu-knowledge-implementation/"
            "src/main/java/com/shiyu/ai/knowledge/implementation/web/controller/"
            "KnowledgeOperationsController.java"
        )

    def test_education_student_writes_use_education_permissions(self):
        source = self.student_controller.read_text(encoding="utf-8")
        self.assertNotIn('system:user:create', source)
        self.assertNotIn('system:user:update', source)
        self.assertNotIn('system:user:delete', source)
        for code in (
            'edu:student:create',
            'edu:student:update',
            'edu:student:delete',
        ):
            self.assertIn(code, source)

    def test_intent_update_has_an_edit_permission(self):
        source = self.intent_controller.read_text(encoding="utf-8")
        update_block = re.search(
            r'@SaCheckPermission\("agent:intent:edit"\)\s*'
            r'@PutMapping\("/\{id\}"\)(?P<body>.*?)(?=\n\s*@(?:Delete|Get|Post)Mapping|\Z)',
            source,
            re.S,
        )
        self.assertIsNotNone(update_block)
        self.assertIn('agent:intent:edit', update_block.group(0))

    def test_gateway_and_intent_edit_permissions_are_seeded(self):
        seed = self.iam_seed.read_text(encoding="utf-8")
        self.assertIn("'model:admin'", seed)
        self.assertIn("'agent:intent:edit'", seed)

    def test_knowledge_default_space_requires_create_permission(self):
        source = self.knowledge_space_controller.read_text(encoding="utf-8")
        method = re.search(
            r'(?P<permission>@SaCheckPermission\("[^"]+"\))\s*'
            r'@PostMapping\("/default"\)\s*'
            r'public\s+Result<[^>]+>\s+ensureDefault\s*\(',
            source,
            re.S,
        )
        self.assertIsNotNone(method)
        self.assertEqual(
            '@SaCheckPermission("knowledge:create")',
            method.group("permission"),
        )

    def test_knowledge_evaluation_run_requires_edit_permission(self):
        source = self.knowledge_evaluation_controller.read_text(encoding="utf-8")
        method = re.search(
            r'(?P<annotations>(?:@SaCheckPermission\("[^"]+"\)\s*'
            r'@PostMapping\("/run"\)|'
            r'@PostMapping\("/run"\)\s*'
            r'@SaCheckPermission\("[^"]+"\)))\s*'
            r'public\s+Result<[^>]+>\s+run\s*\(',
            source,
            re.S,
        )
        self.assertIsNotNone(method)
        self.assertIn('@SaCheckPermission("knowledge:edit")', method.group("annotations"))

    def test_knowledge_operations_use_method_level_read_write_permissions(self):
        source = self.knowledge_operations_controller.read_text(encoding="utf-8")
        self.assertNotRegex(source, r'@SaCheckPermission\([^)]*\)\s*public\s+class\s+KnowledgeOperationsController')
        for mapping, method_name, permission in (
            ("/status", "status", "knowledge:list"),
            ("/backup", "backup", "knowledge:edit"),
            ("/restore-check", "restoreCheck", "knowledge:list"),
        ):
            method = re.search(
                rf'(?P<permission>@SaCheckPermission\("[^"]+"\))\s*'
                rf'@(?:Get|Post)Mapping\("{re.escape(mapping)}"\)\s*'
                rf'public\s+[^\n]+\s+{method_name}\s*\(',
                source,
                re.S,
            )
            self.assertIsNotNone(method, mapping)
            self.assertEqual(
                f'@SaCheckPermission("{permission}")',
                method.group("permission"),
                mapping,
            )


if __name__ == "__main__":
    unittest.main()

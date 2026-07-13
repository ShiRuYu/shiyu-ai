-- ============================================
-- V015: 修复教育空间菜单角色权限
-- 补充 role_id=0(super) 和 role_id=1(admin) 在 workspace_id=0
-- 缺失的 EducationCenter 新子菜单权限
-- ============================================

-- 教育空间新增子菜单ID列表
--  1508: 学习资源
--  1510: 题库练习, 1511: 错题本
--  1520: 在线考试, 1521: AI组卷
--  1530: 今日复习, 1531: 复习历史
--  1540: AI讲解, 1541: AI出题, 1542: AI规划, 1543: AI对话, 1544: AI报告
--  1550: 学习报告, 1551: 能力雷达, 1552: 学习趋势, 1553: 薄弱分析

-- 1. role_id=0 (super, workspace_id=0) — 补充缺失的菜单权限
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
SELECT 0, 0, m.id, 1, 1, 0, 'system', 'system'
FROM `menu` m
WHERE m.id IN (1508, 1510, 1511, 1520, 1521, 1530, 1531,
               1540, 1541, 1542, 1543, 1544,
               1550, 1551, 1552, 1553)
  AND NOT EXISTS (
    SELECT 1 FROM `role_workspace_menu` rwm
    WHERE rwm.role_id = 0 AND rwm.workspace_id = 0 AND rwm.menu_id = m.id
  );

-- 2. role_id=1 (admin, workspace_id=0) — 补充全部教育空间菜单权限（含 EducationCenter 目录本身）
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
SELECT 1, 0, m.id, 1, 1, 0, 'system', 'system'
FROM `menu` m
WHERE m.id IN (1500, 1501, 1502, 1503, 1508,
               1510, 1511, 1520, 1521,
               1530, 1531,
               1540, 1541, 1542, 1543, 1544,
               1550, 1551, 1552, 1553)
  AND NOT EXISTS (
    SELECT 1 FROM `role_workspace_menu` rwm
    WHERE rwm.role_id = 1 AND rwm.workspace_id = 0 AND rwm.menu_id = m.id
  );

-- 3. 清理 role_id=0 (super, workspace_id=0) 中已被删除的旧菜单引用 (1504-1507)
DELETE FROM `role_workspace_menu`
WHERE role_id = 0 AND workspace_id = 0 AND menu_id IN (1504, 1505, 1506, 1507);

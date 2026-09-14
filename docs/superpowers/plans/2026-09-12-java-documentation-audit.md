# Java Documentation Audit Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:verification-before-completion to verify the documentation audit before reporting completion.

**Goal:** 为生产代码中的每个 Java 类型和对象字段补齐描述职责、边界和数据含义的 Javadoc，并以可重复扫描器验证没有遗漏。

**Architecture:** 只修改 `src/main/java` 下的注释，不改变类、字段、注解、方法签名或运行逻辑。按应用、业务域、基础设施、共享内核分批补齐；类级注释说明职责边界，字段级注释说明属性承载的数据或依赖用途。

**Tech Stack:** Java 17、Maven 多模块构建、PowerShell、Python 只用于只读扫描和生成补丁。

## Global Constraints

- 不删除或重排任何 Java 代码，只在已有声明前插入 Javadoc。
- 类、接口、枚举、记录使用 `/** ... */`，内容描述类型职责；字段注释描述属性含义或依赖用途。
- 不为测试源代码强制补齐生产 API 文档；测试行为保持不变。
- 每个批次完成后运行文档清单和 Maven 编译，最终再运行全部既有架构测试与注释检查。

### Task 1: 建立生产 Java 注释清单

**Files:**
- Read: `modules/**/src/main/java/**/*.java`
- Modify: `docs/superpowers/plans/2026-09-12-java-documentation-audit.md`

- [ ] 扫描类型声明和类体字段，记录已有 Javadoc 与缺失项。
- [ ] 排除 `target`、局部变量、方法参数和测试源，保留记录组件清单供人工复核。

### Task 2: 补齐应用与业务模块

**Files:**
- Modify: `modules/applications/**/src/main/java/**/*.java`
- Modify: `modules/business/**/src/main/java/**/*.java`

- [ ] 为缺失的控制器、服务、配置、节点、领域对象、DO/BO/DTO 和仓储类型添加职责说明。
- [ ] 为对象字段和构造依赖添加含义说明，不修改 Lombok、Spring 或持久化注解。
- [ ] 运行相关模块的 Maven 编译并重新扫描缺失项。

### Task 3: 补齐领域、基础设施与共享内核

**Files:**
- Modify: `modules/domains/**/src/main/java/**/*.java`
- Modify: `modules/infrastructure/**/src/main/java/**/*.java`
- Modify: `modules/shared/**/src/main/java/**/*.java`

- [ ] 为剩余类型及字段补充职责和数据语义说明。
- [ ] 复核枚举常量、记录组件和序列化字段，避免把常量或组件误写成业务实体字段。
- [ ] 运行全量 Maven 编译和既有测试。

### Task 4: 最终验证

**Files:**
- Read: `scripts/architecture/check_chinese_comments.py`
- Read: `scripts/architecture/tests/test_*.py`

- [ ] 运行注释扫描，确认生产类型和字段缺失数均为零。
- [ ] 运行 Python 架构测试、脚本编译、Maven 严格编译和 `git diff --check`。
- [ ] 仅在所有检查通过后报告完成。

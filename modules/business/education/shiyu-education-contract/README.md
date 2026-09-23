# shiyu-education-contract 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-education-contract`
- **分类**：领域模块 · Contract

## 作用

定义教育能力与其他领域协作时使用的稳定标识和契约入口，不包含教育业务流程实现。

## 契约内容

- `EducationContractModule` 标识教育 contract 边界，供组合与模块发现代码引用。
- `EducationNodeTypes` 定义教育能力接入 Agent 图编排时使用的节点类型标识。
- 本模块不提供课程、题库、学习计划、Controller、Repository 或数据库 schema；这些行为属于 `shiyu-education-implementation`。
- 契约保持轻量，不依赖 Spring、Web、ORM 或外部供应商实现。

## 边界

Agent 等协作方可依赖本模块识别教育扩展类型；模块不反向依赖教育 implementation。

## 主要包

`com.shiyu.ai.education.contract`

## 内部模块依赖

`shiyu-agent-contract`、`shiyu-shared-kernel`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/business/education/shiyu-education-contract -am test -Ddependency-check.skip=true`

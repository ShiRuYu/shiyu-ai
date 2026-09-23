# shiyu-governance-contract 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-governance-contract`
- **分类**：领域模块 · Contract

## 作用

定义生成准入、配额判断和用量记录的跨域类型，供会话、模型等模块与治理实现协作。

## 契约内容

- `QuotaRequest`、`QuotaDecision`、`QuotaUsage` 与 `QuotaGovernance` 描述配额申请、判断结果、用量和治理接口。
- `UsageMeasurement`、`UsageRecordResult`、`UsageGovernance` 与 `UsageSourceType` 描述上报内容、记录结果、用量写入协作和来源类别。
- 这些类型使调用方能请求准入或提交用量，而无需依赖治理的数据库模型、价格计算实现和 HTTP 层。
- 本模块不执行配额判定、不计费、不查询统计，也不定义数据库表。

## 边界

模型、会话等调用方可依赖这些接口；contract 不依赖治理 implementation。

## 主要包

`com.shiyu.ai.governance.contract`

## 内部模块依赖

`shiyu-shared-kernel`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/governance/shiyu-governance-contract -am test -Ddependency-check.skip=true`

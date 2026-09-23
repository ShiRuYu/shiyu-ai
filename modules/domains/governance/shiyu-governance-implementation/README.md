# shiyu-governance-implementation 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-governance-implementation`
- **分类**：领域模块 · Implementation

## 作用

实现模型/生成用量记录、定价快照、租户配额治理和用量统计，并提供平台级只读统计入口。

## 功能说明

- 用量采集：接受模型等调用方上报的测量值，记录来源、用量和关联信息，并为后续统计保留可查询数据。
- 价格与配额：解析适用的模型价格并保存价格快照；按租户配额契约给生成请求提供准入决策。
- 租户统计：`/api/governance/usage/**` 汇总当前租户的使用概览、周期、模型及 LLM/Embedding 用量。
- 平台统计：`/api/governance/platform/usage/**` 提供全平台汇总只读接口，由 IAM contract 的 `PlatformUsageAccess` 校验平台访问条件；统计 SQL 由治理持久化层显式实现。
- 实时与存储：用量 WebSocket 服务推送治理更新；实体与 Repository 将记录/价格等数据写入数据库，H2 schema/seed 提供开发基线。
- 治理模块维护计量与统计，不负责模型供应商执行，也不对外暴露 IAM implementation 细节。

## 计量与统计链路

1. 调用方经治理 contract 提交用量或请求配额；`UsageRecordService` 记录测量数据，`UsageServiceImpl` 汇总查询，配额逻辑在 `quota` 包中处理预留、结算和释放。
2. `UsageController` 仅查询当前租户数据；其仓储和手写统计 SQL 必须使用可信租户条件，不能因模型或时间过滤条件而丢失租户限制。
3. `PlatformUsageController` 调用 IAM contract 的 `PlatformUsageAccess` 后，再由 `PlatformUsageService` 和 `PlatformUsageRepositoryImpl` 执行全平台只读汇总；普通租户接口不会通过参数切换为平台视图。
4. `UsageWebSocketService` 推送用量更新；WebSocket 建连与消息订阅仍要服从应用安全配置，不能把实时推送视为绕过权限检查的路径。

持久化由治理域维护价格和用量事实，平台组合模块把会话生成准入及用量事件接入这些接口。统计入口与计量写入入口承担不同的授权职责。

## 使用前提与示例

- **前提**：治理 schema 和价格/配额规则已准备；普通统计请求须有可信当前租户。平台统计还需 IAM 实现提供 `PlatformUsageAccess`，并满足默认租户身份、有效角色、非委派及 `platform:usage:read` 专用权限。
- **使用**：`/api/governance/usage/**` 只返回当前租户的概览、周期、模型与 LLM/Embedding 用量；有平台权限的主体改用 `/api/governance/platform/usage/**` 获取全局只读统计。业务生成通过治理 contract 申请、结算和记录用量，不直接写统计表。
- **注意**：普通接口不存在通过客户端参数切换全平台视图的能力；平台统计权限与租户内 `super` 不是同一件事。

## 边界

其他领域使用配额、测量和用量治理契约；平台只读权限通过 IAM contract 协作。租户和平台统计是不同授权边界，不由客户端开关决定数据范围。

## 主要包

`com.shiyu.ai.governance.implementation`、`com.shiyu.ai.governance.implementation.application`、`com.shiyu.ai.governance.implementation.persistence`、`com.shiyu.ai.governance.implementation.quota`、`com.shiyu.ai.governance.implementation.usage`、`com.shiyu.ai.governance.implementation.web`

## 内部模块依赖

`shiyu-governance-contract`、`shiyu-common-foundation`、`shiyu-common-web`、`shiyu-common-mybatis`、`shiyu-iam-contract`、`shiyu-model-contract`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/governance/shiyu-governance-implementation -am test '-Ddependency-check.skip=true'`

# shiyu-conversation-contract 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-conversation-contract`
- **分类**：领域模块 · Contract

## 作用

定义生成任务生命周期及会话域与配额/用量治理协作时使用的契约，不包含聊天接口和持久化实现。

## 契约内容

- `GenerationRun` 与 `GenerationStatus` 描述一次生成运行及其生命周期状态。
- `GenerationAdmission` 表达生成请求进入执行前的准入结果；`GenerationUsageSink` 提供记录执行用量的协作边界。
- 会话模块标识为组合代码提供稳定识别方式。
- 本模块不实现会话/消息 CRUD、SSE 传输、模型调用或数据库访问；这些由 conversation implementation 与其协作者完成。

## 生成生命周期协议

`GenerationRun` 是一次生成的领域记录，`GenerationStatus` 表示其状态。执行前可调用 `GenerationAdmission.reserve` 申请准入，成功结束后调用 `settle`，取消或失败时调用 `release`；`GenerationUsageSink` 则接收最终生成用量。接口的默认方法为空实现，因此**接入 contract 本身不等于已经启用配额控制**，实际限制取决于运行应用装配的实现。

这个契约使组合层能够把会话生成与治理模块连接起来，而不用让 conversation implementation 依赖治理的 Repository 或 HTTP DTO。

## 边界

治理等协作方通过这些类型参与生成准入和用量记录；契约不依赖 conversation implementation。

## 主要包

`com.shiyu.ai.conversation.contract`、`com.shiyu.ai.conversation.contract.api`、`com.shiyu.ai.conversation.contract.model`

## 内部模块依赖

`shiyu-shared-kernel`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/conversation/shiyu-conversation-contract -am test -Ddependency-check.skip=true`

# shiyu-common-event 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-common-event`
- **分类**：可选基础设施模块

## 作用

提供领域事件发布的基础设施实现，使应用可按部署要求选择进程内发布或持久化消息投递。

## 功能说明

- `DomainEventPublisher` 为发布侧提供统一入口；进程内 provider 通过 Spring 事件机制分发事件，适用于无需跨进程可靠投递的部署。
- JDBC outbox 将事件记录持久化，再由 relay 投递到 Kafka；Kafka provider 复用 outbox 流程，并提供消费去重支撑。
- `shiyu.infrastructure.event` 配置选择并设置 provider；数据库与 Kafka broker 必须由应用环境提供。
- 该模块是显式引入的可选基础设施，不代表所有事件默认持久化，也不替代领域事件契约。

## 事件投递路径

1. 调用方通过 `DomainEventPublisher` 发布 shared-kernel 的事件信封；`EventInfrastructureConfiguration` 根据 `EventInfrastructureProperties` 选择实际发布实现。
2. `InProcessEventPublisher` 在单进程内分发，不提供进程崩溃后的持久重放保证。
3. `JdbcOutboxEventPublisher` 先把待投递事件写入数据库；`KafkaOutboxEventPublisher` 在 Kafka 路径中接续投递，消费者可使用 `EventConsumptionDeduplicator` 处理重复消息。
4. 业务处理方仍必须定义自己的事件语义、事务边界与幂等行为；选择 Kafka 也不能把“至少一次投递”误写成“恰好一次业务执行”。

默认 Bootstrap 不依赖该模块。启用时需要应用显式引入依赖、配置事件 provider，并准备对应数据库/Kafka 基础设施。

包职责按实现边界划分：

- `com.shiyu.ai.common.event.api`：事件发布契约。
- `com.shiyu.ai.common.event.config`：Spring 自动配置和事件运行参数。
- `com.shiyu.ai.common.event.publisher`：进程内事件发布实现。
- `com.shiyu.ai.common.event.outbox`：JDBC outbox 和 Kafka relay 实现。
- `com.shiyu.ai.common.event.support`：事件消费去重等支撑能力。

## 使用前提与示例

- **前提**：默认 Bootstrap 不依赖本模块，先显式加入 Maven 依赖及自动配置入口；使用 JDBC outbox 要准备表和数据源，Kafka relay 还要准备 broker 与消费者幂等策略。
- **使用**：设置 `shiyu.infrastructure.event.provider` 选择 provider；业务方注入 `DomainEventPublisher` 发布 `DomainEventEnvelope`。单进程通知可选进程内 provider；需要可恢复投递时选 outbox 路径并配置 relay。
- **限制**：进程内事件没有崩溃重放保证；outbox/Kafka 也不消除重复投递，消费者必须按事件标识去重。仅在 `application.yml` 写 provider、但不引入该模块，不会启用事件实现。

## 边界

- 依赖 `shiyu-common-foundation` 的稳定接口和 `shiyu-shared-kernel` 的事件契约。
- 直接携带 Spring JDBC、Spring Kafka；这些可选技术栈不再进入 common-foundation。
- 不加入默认 `shiyu-ai-bootstrap` 依赖。应用需要事件 provider 时，显式依赖本模块并通过自动配置入口装配。

## 内部模块依赖

`shiyu-common-foundation`、`shiyu-shared-kernel`

## 验证

`mvn --batch-mode --no-transfer-progress -pl modules/infrastructure/shiyu-common-event -am test '-Ddependency-check.skip=true'`

Kafka 容器测试在没有 Docker 时会跳过；JDBC、进程内和属性回归测试仍必须通过。

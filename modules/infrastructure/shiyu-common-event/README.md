# shiyu-common-event 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-common-event`
- **分类**：可选基础设施模块
- **源码规模**：生产 Java 8 个，测试 Java 4 个

## 作用

承载进程内事件、JDBC outbox、Kafka outbox/relay 和消费去重实现。

包职责按实现边界划分：

- `com.shiyu.ai.common.event.api`：事件发布契约。
- `com.shiyu.ai.common.event.config`：Spring 自动配置和事件运行参数。
- `com.shiyu.ai.common.event.publisher`：进程内事件发布实现。
- `com.shiyu.ai.common.event.outbox`：JDBC outbox 和 Kafka relay 实现。
- `com.shiyu.ai.common.event.support`：事件消费去重等支撑能力。

## 边界

- 依赖 `shiyu-common-foundation` 的稳定接口和 `shiyu-shared-kernel` 的事件契约。
- 直接携带 Spring JDBC、Spring Kafka；这些可选技术栈不再进入 common-foundation。
- 不加入默认 `shiyu-ai-bootstrap` 依赖。应用需要事件 provider 时，显式依赖本模块并通过自动配置入口装配。

## 验证

`mvn --batch-mode --no-transfer-progress -pl modules/infrastructure/shiyu-common-event -am test -Ddependency-check.skip=true`

Kafka 容器测试在没有 Docker 时会跳过；JDBC、进程内和属性回归测试仍必须通过。

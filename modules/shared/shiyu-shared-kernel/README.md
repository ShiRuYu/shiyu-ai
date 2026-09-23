# shiyu-shared-kernel 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-shared-kernel`
- **分类**：共享内核模块

## 作用

提供跨领域 contract 共享的少量、稳定、框架无关的标识和基础模型，作为业务模块之间可安全复用的内核。

## 功能说明

- `TenantId`、`UserId`、`RoleId` 和 `CorrelationId` 为租户、主体、角色及请求关联标识提供值类型。
- `ActorContext` 表达经过认证后可供请求处理使用的主体信息；`TenantScope` 表达当前租户访问作用域。它们定义上下文数据，不负责从 HTTP、Session 或线程池中提取/传播上下文。
- `DomainException`、`DomainEvent` 和 `DomainEventEnvelope` 分别提供框架无关的领域错误与事件表达；事件发布实现位于基础设施模块。
- `PageRequest` 提供可跨模块传递的分页参数模型。
- 只保留变化频率较低且确有跨领域消费者的概念，不放业务 DTO、Spring Bean、持久化实体、Web 类型或工具集合。

## 边界

领域 contract 可以依赖；本模块不依赖 Spring、数据库/ORM、Web 或任何业务实现。需要线程、HTTP、Session 或租户持久化机制的适配时，由上层模块实现。

## 主要包

`com.shiyu.ai.kernel.context`、`com.shiyu.ai.kernel.error`、`com.shiyu.ai.kernel.event`、`com.shiyu.ai.kernel.page`

## 内部模块依赖

无内部 Maven 模块依赖

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/shared/shiyu-shared-kernel -am test -Ddependency-check.skip=true`

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

## 类型如何被使用

- 认证适配层构造 `ActorContext`，业务服务从中取得经过服务端验证的主体、当前租户和角色；客户端传入的租户 ID 不能代替该上下文。
- `TenantScope` 表达当前执行线程的可信租户范围，MyBatis 等基础设施读取它来约束数据访问；异步线程必须在自己的执行边界显式绑定并清理。
- `DomainEventEnvelope` 把事件与租户、关联标识等元数据一起传递；真正的投递、持久化和重试由上层事件基础设施完成。
- `PageRequest` 为跨模块查询携带页码与每页大小，不把数据库分页方言或 HTTP 请求对象带入契约。

这些类型是跨模块协议的一部分，修改其字段或语义需同步检查所有 contract 消费者，而不能仅按单一领域需要扩张内核。

## 使用前提与示例

- **前提**：调用方通过 Maven 依赖引入本模块；`TenantId`、`UserId` 等标识必须来自已验证的服务端上下文。`TenantScope` 是线程局部状态，不会自动跨线程传播。
- **使用**：在已授权的后台任务中，以 `TenantScope.withTenant(new TenantId(tenantId), () -> repository.findById(id))` 包裹一次目标租户访问；退出时会恢复原作用域。普通 HTTP 请求由认证适配层绑定作用域，不应把请求参数直接传给 `withTenant`。
- **分页**：跨领域查询可传 `new PageRequest(pageNumber, pageSize)`；它只表达分页请求，实际 SQL 分页仍由仓储实现。

## 边界

领域 contract 可以依赖；本模块不依赖 Spring、数据库/ORM、Web 或任何业务实现。需要线程、HTTP、Session 或租户持久化机制的适配时，由上层模块实现。

## 主要包

`com.shiyu.ai.kernel.context`、`com.shiyu.ai.kernel.error`、`com.shiyu.ai.kernel.event`、`com.shiyu.ai.kernel.page`

## 内部模块依赖

无内部 Maven 模块依赖

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/shared/shiyu-shared-kernel -am test '-Ddependency-check.skip=true'`

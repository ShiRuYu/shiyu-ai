# shiyu-common-thread 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-common-thread`
- **分类**：基础设施模块

## 作用

为异步工作提供受管线程池、执行器工厂和可选的线程观测集成。

## 功能说明

- `ThreadPoolManager` 管理带名称和生命周期的线程池；工厂按调用场景构造平台线程、虚拟线程或受保护的执行器。
- `TaskContext` 装饰器传播本模块定义的任务上下文；OpenTelemetry 与 Micrometer 集成按 classpath/configuration 可选启用。
- 默认线程池参数可通过模块配置调整，并由 profile 配置提供运行基线。
- 该上下文装饰器不等于租户上下文传播。异步任务需要租户数据时，任务必须显式携带可信租户，并在工作线程绑定和清理租户作用域。

## 边界

业务模块可以依赖公共基础设施；基础设施不反向依赖领域 implementation。

## 主要包

`com.shiyu.ai.common.thread.api`、`com.shiyu.ai.common.thread.config`、`com.shiyu.ai.common.thread.context`、`com.shiyu.ai.common.thread.executor`、`com.shiyu.ai.common.thread.metrics`、`com.shiyu.ai.common.thread.otel`

## 内部模块依赖

`shiyu-common-foundation`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/infrastructure/shiyu-common-thread -am test -Ddependency-check.skip=true`

# shiyu-common-web 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-common-web`
- **分类**：基础设施模块

## 作用

为应用和领域 Controller 提供 Spring Web/Servlet 横切适配，不拥有登录流程或领域级权限策略。

## 功能说明

- `ActorContextHttpAdapter.currentActor()` 将 `UserContextHolder` 中的用户、租户和角色信息组装为 shared-kernel 的 `ActorContext`；`runWithContext()` 在任务内绑定并恢复用户上下文与 `TenantScope`。客户端 IP 解析和 Servlet 工具处理容器层适配。
- `WebInvokeInterceptor` 负责请求开始/结束日志、traceId、耗时和敏感字段脱敏，并在结束时清理请求日志与用户上下文；可重复读取请求体过滤器供日志/校验链路使用。
- 提供通用异常响应、参数校验、XSS 过滤器和验证注解；项目认证与 Sa-Token 具体配置位于 `shiyu-ai-web`。
- `WebPublicPathContributor` 供应用和基础设施注册公开路径模式，再由应用安全配置汇总；本模块不自行决定具体业务接口权限。
- 不根据 URL 与权限码维护第二套路由映射，也不替代 Controller 声明的操作权限和服务层的资源/租户授权。
- 仅承载可复用 Web 技术能力；不放具体领域 Controller、请求流程或业务数据访问代码。

## 边界

业务模块可以依赖公共基础设施；基础设施不反向依赖领域 implementation。

## 主要包

`com.shiyu.ai.common.web.auth`、`com.shiyu.ai.common.web.exception`、`com.shiyu.ai.common.web.file`、`com.shiyu.ai.common.web.filter`、`com.shiyu.ai.common.web.interceptor`、`com.shiyu.ai.common.web.servlet`、`com.shiyu.ai.common.web.validation`

## 内部模块依赖

`shiyu-common-foundation`、`shiyu-shared-kernel`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/infrastructure/shiyu-common-web -am test -Ddependency-check.skip=true`

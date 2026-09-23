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

## 请求横切处理

- 请求进入时，Servlet 适配负责取得可重复读取的请求体、请求关联信息和客户端地址；`WebInvokeInterceptor` 在请求结束时记录耗时、清理日志上下文并避免敏感参数原文进入日志。
- `ActorContextHttpAdapter` 把应用认证上下文转换为 shared-kernel 的 `ActorContext`，在受控回调内绑定/恢复上下文；它不凭请求参数自行选择租户。
- `ShiYuDefaultExceptionHandler` 处理通用 Web 错误；数据库异常、Sa-Token 异常和业务域异常的具体映射分别由应用或领域模块处理。
- `WebPublicPathContributor` 仅声明公开路径，应用层汇总后接入安全配置；新增业务公开接口仍需审查其认证和对象权限要求。

通用过滤器、校验注解和异常适配可由多个 Controller 复用；这个模块不拥有 URL 到权限码的业务映射表。

## 使用前提与示例

- **前提**：仅用于 Servlet Web 应用，需由应用层配置认证拦截器并在请求线程填充 `UserContextHolder`；普通后台任务不能直接假设存在当前 HTTP 主体。
- **使用**：Controller/适配器调用 `ActorContextHttpAdapter.currentActor()` 获取可信 `ActorContext`，再传给领域服务；新增允许匿名访问的基础设施路径时，实现 `WebPublicPathContributor` 并交由应用安全配置汇总。
- **限制**：`runWithContext()` 只在受控回调内绑定并恢复上下文，不代表线程池会自动继承租户；公开路径声明不是业务授权。

## 边界

业务模块可以依赖公共基础设施；基础设施不反向依赖领域 implementation。

## 主要包

`com.shiyu.ai.common.web.auth`、`com.shiyu.ai.common.web.exception`、`com.shiyu.ai.common.web.file`、`com.shiyu.ai.common.web.filter`、`com.shiyu.ai.common.web.interceptor`、`com.shiyu.ai.common.web.servlet`、`com.shiyu.ai.common.web.validation`

## 内部模块依赖

`shiyu-common-foundation`、`shiyu-shared-kernel`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/infrastructure/shiyu-common-web -am test '-Ddependency-check.skip=true'`

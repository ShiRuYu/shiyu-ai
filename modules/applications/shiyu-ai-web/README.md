# shiyu-ai-web 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-ai-web`
- **分类**：应用模块

## 作用

把 Servlet Web、安全认证和身份上下文接入可执行应用，为各领域 Controller 提供统一的请求入口支撑。

## 功能说明

- 装配 Sa-Token Web 支持、登录态及认证相关配置，并将已认证请求映射为应用可读取的主体上下文。
- 配置审计拦截和与权限校验协作的 Web 行为；领域对象的数据范围与资源归属仍由相应领域服务负责。
- 集中配置 API 文档分组和 Controller 扫描入口，使各领域实现按自己的 Controller 包参与 OpenAPI 描述。
- 在应用 Web 层统一处理数据库唯一约束和 MyBatis 系统异常，向客户端返回不包含底层连接信息的提示。
- 依赖通用 Web/基础设施能力处理公共适配；具体接口、HTTP 路径和业务响应由领域实现模块提供。本模块本身不定义课程、知识、模型等业务 Controller。

## 请求处理链路

1. `SaInterceptorConfig` 与 `SaTokenConfig` 组织登录态校验；`WebPublicPathPatterns` 汇总允许匿名访问的路径贡献者。
2. `UserContextInterceptor` 将认证后的身份接入请求上下文，供 `ActorContextHttpAdapter` 生成领域服务可用的 `ActorContext`；请求结束必须清理线程上下文。
3. `BusinessModuleAccessInterceptor` 拦截已关闭业务模块的入口，`AuditInterceptor` 记录请求审计；它们不能替代 Controller 操作权限及服务层资源归属校验。
4. `OpenApiConfig` 定义 API 文档分组；`ApiExceptionHandler`、`SaTokenExceptionHandler` 与优先处理数据库异常的 `MybatisExceptionHandler` 统一响应边界，避免把底层异常详情返回客户端。
5. `AgentWebMvcConfig` 与 `ResourcesConfig` 分别承接 Agent Web 适配和静态资源映射，具体业务路由仍由领域 Controller 声明。

## 协作边界

本模块依赖 IAM 实现来完成应用级认证装配，但其他业务领域应通过 IAM contract 获取授权能力。公共 Servlet 工具和通用过滤器属于 `shiyu-common-web`；本模块负责把它们接入唯一可运行应用。

## 边界

本模块是应用层 Web 组合，不是独立启动程序；认证配置不能替代领域服务中的对象权限和租户范围校验。

## 主要包

`com.shiyu.ai.web.auth`、`com.shiyu.ai.web.common`、`com.shiyu.ai.web.config`、`com.shiyu.ai.web.config.properties`、`com.shiyu.ai.web.exception`、`com.shiyu.ai.web.interceptor`

## 内部模块依赖

`shiyu-agent-implementation`、`shiyu-common-foundation`、`shiyu-common-mybatis`、`shiyu-common-web`、`shiyu-iam-contract`、`shiyu-iam-implementation`、`shiyu-shared-kernel`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/applications/shiyu-ai-web -am test -Ddependency-check.skip=true`

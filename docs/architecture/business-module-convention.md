# 业务模块自动配置与开关约定

后端长期只维护一个可执行应用：`modules/applications/shiyu-ai-bootstrap`。平台能力由
`shiyu-application` 统一组合，业务能力以独立的 `contract` / `implementation` 模块接入，
不为每个业务复制 application、端口、默认配置或根包扫描。

## 模块实现形态

每个业务 implementation 都应提供一个与业务同名的自动配置入口：

```java
@AutoConfiguration
@ConditionalOnBusinessModule(value = "education", matchIfMissing = true)
@ComponentScan(basePackages = "com.shiyu.ai.education.implementation")
public class EducationModuleAutoConfiguration {

    @Bean
    BusinessModuleDescriptor educationModuleDescriptor() {
        return BusinessModuleDescriptor.of(
                "education", "教育", "/api/education", "education:");
    }
}
```

入口类必须登记到 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。
模块内部的 Controller、配置、路径贡献者、数据库基线贡献者和领域服务都放在自己的
implementation 包中；平台 composition 不直接引用具体业务包名。每个模块应有架构测试，检查
自动配置、imports、开关条件和描述符同时存在。

## 两层开关

进程级开关决定模块是否装配：

```yaml
shiyu:
  modules:
    education:
      enabled: ${SHIYU_MODULE_EDUCATION_ENABLED:true}
```

属性格式是 `shiyu.modules.<module-id>.enabled`，环境变量格式是
`SHIYU_MODULE_<MODULE_ID>_ENABLED`。值只能是布尔值；错误值必须在启动期间失败。模块关闭时不
注册它的 Bean、路由、数据库贡献者或初始化逻辑；已有表和历史数据不删除，重新启用后仍可使用。

租户级开关由 IAM 的 `TenantModuleAccessPort` 提供，是进程装配后的第二道访问门。新租户创建时，
IAM 使用当前进程已装配的 `BusinessModuleDescriptor` 初始化默认启用记录；关闭租户模块只更新
状态，不删除业务数据。缺少租户作用域或模块授权记录默认拒绝。

请求顺序固定为：

```text
认证上下文 → TenantScope → 进程模块是否装配 → 租户模块是否启用 → 领域权限 → Controller/Service
```

模块开关不能绕过租户隔离、角色权限或 `@SaCheckPermission`。非 HTTP 的任务、初始化和跨租户
操作必须在编排入口显式绑定目标 `TenantScope`。

## 新业务接入清单

新增课程、企业、工作流、插件等业务时：

1. 新建独立的 `contract` 与 `implementation`，只通过 contract、事件和组合入口使用平台能力。
2. 增加自动配置、条件、描述符、imports、数据库基线贡献者和模块架构测试。
3. 在单一 bootstrap 的默认配置中加入开关（默认值需说明）；不创建第二个 application。
4. 为路由、租户默认状态、权限、关闭/重新启用、跨租户拒绝和数据保留增加测试与文档。
5. 更新 OpenAPI、权限矩阵、数据字典和使用文档，明确区分已实现行为、开发约束和待验证项。

平台代码不得把业务字段写入平台表，不得扩大启动类根包扫描，也不得通过客户端参数绕过
进程或租户模块开关。

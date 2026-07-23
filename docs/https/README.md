# HTTP API 请求文件

本目录包含 [REST Client](https://marketplace.visualstudio.com/items?itemName=humao.rest-client) 格式的 `.http` 请求文件，
可直接在 VS Code / JetBrains IDEA 中点击 `Send Request` 执行。

## 文件索引

| 文件 | 模块 | 说明 |
|------|------|------|
| `00-global-variables.http` | — | 全局变量：`@baseUrl`、`@authToken` |
| `01-auth.http` | Auth | 登录/注册/登出/刷新/切换租户空间 |
| `02-user.http` | User | 用户 CRUD、密码重置与修改 |
| `03-role.http` | Role | 角色 CRUD、用户角色分配/移除 |
| `04-tenant.http` | Tenant | 租户 CRUD（需 tenant:admin 权限） |
| `05-workspace.http` | Workspace | 工作空间树形管理 |
| `06-menu.http` | Menu | 菜单/权限树 CRUD、路由菜单查询 |
| `07-captcha.http` | Captcha | 验证码获取与校验 |
| `08-dict.http` | Dict | 字典项 CRUD、按类型查询 |
| `09-timezone.http` | Timezone | 时区选项与设置 |
| `10-auth-code.http` | AuthCode | 历史兼容的权限码管理 |
| `11-user-context-test.http` | Test | 用户上下文调试接口 |

## 使用方式

1. 安装 VS Code 的 **REST Client** 插件或 IntelliJ IDEA 的 **HTTP Client**
2. 打开任意 `.http` 文件
3. 先执行 **01-auth.http** 中的 `### Login` 获取 token
4. 将返回的 `accessToken` 值复制到 `00-global-variables.http` 的 `@authToken` 中
5. 或直接在请求头中手动替换 `{{authToken}}` 变量

## 变量说明

- `@baseUrl` — 服务地址，默认 `http://localhost:9000`
- `@authToken` — Bearer Token，从登录接口获取

> 也可以在 VS Code 的 `settings.json` 中配置 `rest-client.environmentVariables` 来管理环境变量。

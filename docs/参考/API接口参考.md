# API 接口参考

> 本文档由 `scripts/docs/generate_reference_docs.py` 从 SpringDoc OpenAPI 自动生成。
> 生成源：`../shiyu-ui/tests/contracts/shiyu-ai-openapi.json`；OpenAPI：`3.1.0`；服务版本：`0.1`。

## 契约约定

- 浏览器开发环境使用 `/api` 作为 Vite 代理前缀；后端控制器路径本身不包含 `/api`。
- 除登录、注册、验证码等公开入口外，请求使用 `Authorization: Bearer <accessToken>`。
- 普通 JSON 接口通常返回 `Result<T>`；流式接口按 OpenAPI 标注返回 SSE 或二进制内容。
- `requestBody` 与响应栏保留 OpenAPI schema 名称，具体字段见“组件模型”。

## 接口清单

### Agent Definition

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/agent/definition/create` | Create | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:AgentRequest | 200=*/*:ResultAgentVO |
| POST | `/agent/definition/delete` | Delete | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| POST | `/agent/definition/delete/by-agent-id` | Delete Agent | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string | 200=*/*:ResultVoid |
| GET | `/agent/definition/detail` | Get by Id | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultAgentDetailVO |
| GET | `/agent/definition/detail/by-agent-id` | Get Agent | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string | 200=*/*:ResultAgentDefinition |
| GET | `/agent/definition/list` | List Agents | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListAgentDefinition |
| GET | `/agent/definition/node-types` | Get Node Types | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListNodeTypeMetaVO |
| GET | `/agent/definition/node-types/detail` | Get Node Type | 登录态；细粒度权限见权限矩阵 | nodeType[query,必填]:string | 200=*/*:ResultNodeTypeMetaVO |
| GET | `/agent/definition/options` | List All Options | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListIdNameOptionVO |
| GET | `/agent/definition/page` | Get Page | 登录态；细粒度权限见权限矩阵 | pageNo[query,可选]:integer/int32; pageSize[query,可选]:integer/int32; name[query,可选]:string; status[query,可选]:integer/int32 | 200=*/*:ResultPageDataAgentVO |
| POST | `/agent/definition/register` | Register Agent | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:RegisterAgentRequest | 200=*/*:ResultMapStringObject |
| POST | `/agent/definition/status` | Update Status | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; status[query,必填]:integer/int32 | 200=*/*:ResultVoid |
| POST | `/agent/definition/update` | Update | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:AgentRequest | 200=*/*:ResultAgentVO |
| POST | `/agent/definition/version/switch` | Switch Version | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; version[query,必填]:string | 200=*/*:ResultVoid |

### Agent Version

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/agent/version/activate` | Activate | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| POST | `/agent/version/archive` | Archive | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| POST | `/agent/version/copy` | Copy | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; body[必填]=application/json:VersionRequest | 200=*/*:ResultAgentVersionVO |
| POST | `/agent/version/create` | Create Version | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; body[必填]=application/json:VersionRequest | 200=*/*:ResultAgentVersionVO |
| POST | `/agent/version/delete` | Delete Version | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/agent/version/detail` | Get Version Detail | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64 | 200=*/*:ResultAgentVersionDetailVO |
| GET | `/agent/version/graph/canvas` | Get Canvas | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64 | 200=*/*:ResultString |
| POST | `/agent/version/graph/canvas-update` | Update Canvas | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64; body[必填]=application/json:string | 200=*/*:ResultVoid |
| GET | `/agent/version/graph/detail` | Get Graph | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64 | 200=*/*:ResultAgentVersionDetailVO |
| POST | `/agent/version/graph/edge/create` | Add Edge | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64; body[必填]=application/json:EdgeRequest | 200=*/*:ResultVoid |
| POST | `/agent/version/graph/edge/delete` | Delete Edge | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64; sourceNodeId[query,必填]:string; targetNodeId[query,必填]:string | 200=*/*:ResultVoid |
| POST | `/agent/version/graph/node/create` | Add Node | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64; body[必填]=application/json:NodeConfigRequest | 200=*/*:ResultVoid |
| POST | `/agent/version/graph/node/delete` | Delete Node | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64; nodeId[query,必填]:string | 200=*/*:ResultVoid |
| POST | `/agent/version/graph/node/update` | Update Node | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64; nodeId[query,必填]:string; body[必填]=application/json:NodeConfigRequest | 200=*/*:ResultVoid |
| POST | `/agent/version/graph/update` | Update Graph | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64; body[必填]=application/json:GraphConfigRequest | 200=*/*:ResultAgentVersionDetailVO |
| POST | `/agent/version/graph/validate` | Validate Graph | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64; body[必填]=application/json:GraphConfigRequest | 200=*/*:ResultGraphValidationVO |
| GET | `/agent/version/list` | Get Versions | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string | 200=*/*:ResultListAgentVersionVO |
| POST | `/agent/version/publish` | Publish | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| POST | `/agent/version/update` | Update Version | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64; body[必填]=application/json:VersionRequest | 200=*/*:ResultAgentVersionVO |

### Ai Model

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/agent/model/batch-delete` | Delete Batch | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:array<integer/int64> | 200=*/*:ResultVoid |
| POST | `/agent/model/create` | Create | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:AiModelRequest | 200=*/*:ResultAiModelVO |
| POST | `/agent/model/delete` | Delete | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/agent/model/detail` | Get by Id | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultAiModelVO |
| GET | `/agent/model/options` | Get Options | 登录态；细粒度权限见权限矩阵 | platformId[query,可选]:integer/int64 | 200=*/*:ResultListIdNameOptionVO |
| GET | `/agent/model/page` | Get Page | 登录态；细粒度权限见权限矩阵 | platformId[query,可选]:integer/int64; pageNo[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataAiModelVO |
| GET | `/agent/model/platform` | Get by Platform Id | 登录态；细粒度权限见权限矩阵 | platformId[query,必填]:integer/int64 | 200=*/*:ResultListAiModelVO |
| GET | `/agent/model/platform/by-code` | Get by Platform Code | 登录态；细粒度权限见权限矩阵 | platformCode[query,必填]:string | 200=*/*:ResultListAiModelResponse |
| GET | `/agent/model/platform/default` | Get Default By Platform Id | 登录态；细粒度权限见权限矩阵 | platformId[query,必填]:integer/int64 | 200=*/*:ResultAiModelVO |
| POST | `/agent/model/set-default` | Set Default | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultAiModelVO |
| POST | `/agent/model/update` | Update | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:AiModelRequest | 200=*/*:ResultAiModelVO |

### Ai Platform

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/admin/platform/code` | Get by Code | 登录态；细粒度权限见权限矩阵 | code[query,必填]:string | 200=*/*:ResultAiPlatformResponse |
| POST | `/admin/platform/create` | Create | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:AiPlatformRequest | 200=*/*:ResultAiPlatformVO |
| GET | `/admin/platform/default` | Get Default | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultAiPlatformResponse |
| POST | `/admin/platform/delete` | Delete | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/admin/platform/detail` | Get by Id | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultAiPlatformResponse |
| GET | `/admin/platform/enabled` | Get All Enabled | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListAiPlatformVO |
| GET | `/admin/platform/options` | Get Options | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListIdNameOptionVO |
| GET | `/admin/platform/page` | Get Page | 登录态；细粒度权限见权限矩阵 | name[query,可选]:string; code[query,可选]:string; pageNo[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataAiPlatformVO |
| POST | `/admin/platform/reload` | Reload | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultVoid |
| POST | `/admin/platform/set-default` | Set Default | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultAiPlatformVO |
| POST | `/admin/platform/update` | Update | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:AiPlatformRequest | 200=*/*:ResultAiPlatformVO |

### Auth

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/auth/code-login` | Code Login | 公开 | body[必填]=application/json:CodeLoginRequest | 200=*/*:ResultLoginResponseVO |
| GET | `/auth/codes` | Get Auth Codes | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListString |
| POST | `/auth/current-role` | Switch Current Role | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:SwitchRoleRequest | 200=*/*:ResultSwitchContextResponse |
| POST | `/auth/forget-password` | Forget Password | 公开 | body[必填]=application/json:ForgetPasswordRequest | 200=*/*:ResultBoolean |
| POST | `/auth/login` | Login | 公开 | body[必填]=application/json:LoginRequest | 200=*/*:ResultLoginResponseVO |
| POST | `/auth/logout` | Logout | 登录态；细粒度权限见权限矩阵 | Authorization[header,可选]:string | 200=*/*:ResultString |
| POST | `/auth/refresh` | Refresh Token | 公开 | body[必填]=application/json:RefreshTokenRequest | 200=*/*:ResultString |
| POST | `/auth/register` | Register | 公开 | body[必填]=application/json:LoginRequest | 200=*/*:ResultLoginResponseVO |
| POST | `/auth/switch-tenant` | Switch Tenant | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:SwitchTenantRequest | 200=*/*:ResultSwitchContextResponse |
| GET | `/auth/tenants` | Get User Tenants | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListTenantInfoVO |

### Auth Code

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/auth-code/create` | Create Auth Code | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:AuthCodeRequest | 200=*/*:ResultAuthCodeResponse |
| POST | `/auth-code/delete` | Delete Auth Code | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/auth-code/list` | List Auth Codes | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListAuthCodeOptionVO |
| GET | `/auth-code/options` | Auth code options | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListAuthCodeOptionVO |
| GET | `/auth-code/page` | Page Auth Codes | 登录态；细粒度权限见权限矩阵 | request[query,必填]:AuthCodePageRequest | 200=*/*:ResultPageDataAuthCodeOptionVO |
| POST | `/auth-code/roles/grant` | Grant role auth codes | 登录态；细粒度权限见权限矩阵 | roleId[query,必填]:integer/int64; tenantId[query,必填]:integer/int64; body[必填]=application/json:array<integer/int64> | 200=*/*:ResultVoid |
| GET | `/auth-code/roles/list` | List role auth codes | 登录态；细粒度权限见权限矩阵 | roleId[query,必填]:integer/int64; tenantId[query,必填]:integer/int64 | 200=*/*:ResultListString |
| POST | `/auth-code/roles/replace` | Replace role auth codes | 登录态；细粒度权限见权限矩阵 | roleId[query,必填]:integer/int64; tenantId[query,必填]:integer/int64; body[必填]=application/json:array<string> | 200=*/*:ResultVoid |
| POST | `/auth-code/roles/revoke` | Revoke role auth code | 登录态；细粒度权限见权限矩阵 | roleId[query,必填]:integer/int64; tenantId[query,必填]:integer/int64; authCodeId[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| POST | `/auth-code/update` | Update Auth Code | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:AuthCodeRequest | 200=*/*:ResultVoid |

### Captcha

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/captcha` | Get Captcha | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultCaptchaVO |
| POST | `/captcha/validate` | Validate Captcha | 公开 | body[必填]=application/json:ValidateCaptchaRequest | 200=*/*:ResultValidateCaptchaResponse |

### Chat Demo

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/chat/default-model` | Get Default Model | 登录态；细粒度权限见权限矩阵 | platform[query,必填]:string | 200=*/*:ResultMapStringString |
| GET | `/chat/platforms` | Get Available Platforms | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListString |
| POST | `/chat/send` | 操作: /send | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:DemoChatRequest | 200=*/*:ResultDemoChatResponse |
| POST | `/chat/send-stream` | Stream Chat | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:DemoChatRequest | 200=text/event-stream:array<ChatResponse> |
| POST | `/chat/send-with-memory` | Chat With Memory | 登录态；细粒度权限见权限矩阵 | sessionId[query,必填]:string; body[必填]=application/json:DemoChatRequest | 200=*/*:ResultDemoChatResponse |

### Dict

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/dict/batch-delete` | Delete Dicts | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:array<integer/int64> | 200=*/*:ResultVoid |
| POST | `/dict/create` | Create Dict | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:DictRequest | 200=*/*:ResultDictVO |
| POST | `/dict/delete` | Delete Dict | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/dict/list` | Get Dict List | 登录态；细粒度权限见权限矩阵 | request[query,必填]:DictPageRequest | 200=*/*:ResultPageDataDictVO |
| GET | `/dict/type` | Get Dict By Type | 登录态；细粒度权限见权限矩阵 | dictType[query,必填]:string | 200=*/*:ResultListDictVO |
| POST | `/dict/update` | Update Dict | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:DictRequest | 200=*/*:ResultDictVO |

### Execution

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/agent/execution/cancel` | Cancel Execution | 登录态；细粒度权限见权限矩阵 | executionId[query,必填]:string | 200=*/*:ResultVoid |
| GET | `/agent/execution/detail` | Get Execution Details | 登录态；细粒度权限见权限矩阵 | executionId[query,必填]:string | 200=*/*:ResultMapStringObject |
| POST | `/agent/execution/execute` | Execute Agent | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; body[可选]=application/json:object | 200=*/*:ResultMapStringObject |
| POST | `/agent/execution/execute-stream` | Execute Agent Stream | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; body[可选]=application/json:object | 200=text/event-stream:array<ResultMapStringObject> |
| GET | `/agent/execution/history` | Get Execution History | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; limit[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |
| POST | `/agent/execution/pause` | Pause Execution | 登录态；细粒度权限见权限矩阵 | executionId[query,必填]:string | 200=*/*:ResultVoid |
| POST | `/agent/execution/resume` | Resume Execution | 登录态；细粒度权限见权限矩阵 | executionId[query,必填]:string | 200=*/*:ResultMapStringObject |
| GET | `/agent/execution/status` | Get Execution Status | 登录态；细粒度权限见权限矩阵 | executionId[query,必填]:string | 200=*/*:ResultMapStringObject |

### File

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| DELETE | `/system/file` | 删除文件 | 登录态；细粒度权限见权限矩阵 | key[query,必填]:string | 200=*/*:ResultBoolean |
| GET | `/system/file/config` | 获取文件存储配置 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultMapStringObject |
| GET | `/system/file/download` | 下载文件 | 登录态；细粒度权限见权限矩阵 | key[query,必填]:string | 200=*/*:string/binary |
| GET | `/system/file/list` | 获取文件列表 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListFileView |
| POST | `/system/file/upload` | 上传文件 | 登录态；细粒度权限见权限矩阵 | body[可选]=application/json:object | 200=*/*:ResultFileView |

### MCP 工具市场

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/tool/mcp/categories` | 获取工具分类 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultSetString |
| GET | `/tool/mcp/stats` | 获取工具统计 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultMapStringObject |
| GET | `/tool/mcp/tools` | 列出所有工具 | 登录态；细粒度权限见权限矩阵 | category[query,可选]:string; tag[query,可选]:string; keyword[query,可选]:string | 200=*/*:ResultListMcpToolDescriptor |
| GET | `/tool/mcp/tools/detail` | 获取工具详情 | 登录态；细粒度权限见权限矩阵 | name[query,必填]:string | 200=*/*:ResultMcpToolDescriptor |
| POST | `/tool/mcp/tools/execute` | 执行工具 | 登录态；细粒度权限见权限矩阵 | name[query,必填]:string; body[可选]=application/json:object | 200=*/*:ResultObject |

### Menu

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/menu/all` | Get All Menus | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListRouteMenuVO |
| GET | `/menu/children` | Get Menu Children | 登录态；细粒度权限见权限矩阵 | parentId[query,必填]:integer/int64 | 200=*/*:ResultListRouteMenuVO |
| POST | `/menu/create` | Create Menu | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:MenuRequest | 200=*/*:ResultVoid |
| POST | `/menu/delete` | Delete Menu | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/menu/list` | Get System Menu List | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListMenuVO |
| GET | `/menu/name-exists` | Is Menu Name Exists | 登录态；细粒度权限见权限矩阵 | name[query,必填]:string; id[query,可选]:integer/int64 | 200=*/*:ResultBoolean |
| GET | `/menu/page` | Get System Menu Page | 登录态；细粒度权限见权限矩阵 | request[query,必填]:MenuPageRequest | 200=*/*:ResultPageDataMenuVO |
| GET | `/menu/path-exists` | Is Menu Path Exists | 登录态；细粒度权限见权限矩阵 | path[query,必填]:string; id[query,可选]:integer/int64 | 200=*/*:ResultBoolean |
| GET | `/menu/permissions` | Get Menu Permissions Tree | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListRouteMenuVO |
| GET | `/menu/roots` | Get Menu Roots | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListRouteMenuVO |
| GET | `/menu/tree` | Get All Tree | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListRouteMenuVO |
| POST | `/menu/update` | Update Menu | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:MenuRequest | 200=*/*:ResultVoid |

### Usage

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/usage/by-model` | LLM 按模型聚合 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListMapStringObject |
| GET | `/usage/daily` | 按日聚合（所有类型，按 usage_type 分组） | 登录态；细粒度权限见权限矩阵 | days[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |
| GET | `/usage/embedding/overview` | Embedding 用量概览 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultMapStringObject |
| GET | `/usage/llm/daily` | LLM 按日聚合（含 token/cost） | 登录态；细粒度权限见权限矩阵 | days[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |
| GET | `/usage/llm/monthly` | LLM 按月聚合（含 token/cost） | 登录态；细粒度权限见权限矩阵 | months[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |
| GET | `/usage/llm/weekly` | LLM 按周聚合（含 token/cost） | 登录态；细粒度权限见权限矩阵 | weeks[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |
| GET | `/usage/monthly` | 按月聚合（所有类型，按 usage_type 分组） | 登录态；细粒度权限见权限矩阵 | months[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |
| GET | `/usage/overview` | 用量概览（所有类型） | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultMapStringObject |
| GET | `/usage/weekly` | 按周聚合（所有类型，按 usage_type 分组） | 登录态；细粒度权限见权限矩阵 | weeks[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |

### User Context Test

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/test/user-context/demo` | Demo Usage | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultUserContextVO |
| GET | `/test/user-context/info` | Get Current User Info | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultUserContextVO |

### analytics-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/edu/analytics/ability-radar` | getAbilityRadar | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64; knowledgeId[query,必填]:integer/int64 | 200=*/*:ResultAbilityRadarResponse |
| GET | `/edu/analytics/overview` | getOverview_1 | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultOverviewResponse |
| POST | `/edu/analytics/record-create` | createRecord | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:StudyRecordRequest | 200=*/*:ResultStudyRecordResponse |
| GET | `/edu/analytics/records` | listRecordsByStudent | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListStudyRecordResponse |
| GET | `/edu/analytics/records/knowledge` | listRecordsByStudentAndKnowledge | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64; knowledgeId[query,必填]:integer/int64 | 200=*/*:ResultListStudyRecordResponse |
| GET | `/edu/analytics/trend` | getTrend | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultTrendResponse |
| GET | `/edu/analytics/weak-points` | getWeakPoints | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListWeakPointResponse |

### chapter-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/edu/chapter/children` | listByParentId | 登录态；细粒度权限见权限矩阵 | parentId[query,必填]:integer/int64 | 200=*/*:ResultListChapterResponse |
| POST | `/edu/chapter/create` | create_19 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ChapterRequest | 200=*/*:ResultChapterResponse |
| POST | `/edu/chapter/delete` | delete_17 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/edu/chapter/detail` | getById_15 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultChapterResponse |
| POST | `/edu/chapter/knowledge/bind` | replaceKnowledgeIds | 登录态；细粒度权限见权限矩阵 | chapterId[query,必填]:integer/int64; body[必填]=application/json:array<integer/int64> | 200=*/*:ResultVoid |
| GET | `/edu/chapter/knowledge/list` | listKnowledgeIds | 登录态；细粒度权限见权限矩阵 | chapterId[query,必填]:integer/int64 | 200=*/*:ResultListLong |
| GET | `/edu/chapter/textbook` | listByTextbookId | 登录态；细粒度权限见权限矩阵 | textbookId[query,必填]:integer/int64 | 200=*/*:ResultListChapterResponse |
| GET | `/edu/chapter/tree` | getChapterTree | 登录态；细粒度权限见权限矩阵 | textbookId[query,必填]:integer/int64 | 200=*/*:ResultListChapterResponse |
| POST | `/edu/chapter/update` | update_17 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:ChapterRequest | 200=*/*:ResultVoid |

### course-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/edu/course/create` | create_18 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:CourseRequest | 200=*/*:ResultCourseResponse |
| POST | `/edu/course/delete` | delete_16 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/edu/course/detail` | getById_14 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultCourseResponse |
| GET | `/edu/course/grade` | listByGrade | 登录态；细粒度权限见权限矩阵 | grade[query,必填]:integer/int32 | 200=*/*:ResultListCourseResponse |
| POST | `/edu/course/learn` | startLearning | 登录态；细粒度权限见权限矩阵 | courseId[query,必填]:integer/int64; studentId[query,必填]:integer/int64 | 200=*/*:ResultCourseResponse |
| GET | `/edu/course/list` | list_10 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataCourseResponse |
| GET | `/edu/course/subject` | listBySubjectCode_2 | 登录态；细粒度权限见权限矩阵 | subjectCode[query,必填]:string | 200=*/*:ResultListCourseResponse |
| POST | `/edu/course/update` | update_16 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:CourseRequest | 200=*/*:ResultVoid |

### education-resource-content-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/edu/education-resources/{fileName}` | open | 登录态；细粒度权限见权限矩阵 | fileName[path,必填]:string | 200=*/*:string/binary |

### exam-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/edu/exam/create` | create_17 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ExamRequest | 200=*/*:ResultExamResponse |
| POST | `/edu/exam/delete` | delete_15 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/edu/exam/detail` | getById_13 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultExamResponse |
| GET | `/edu/exam/list` | list_9 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataExamResponse |
| GET | `/edu/exam/subject` | listBySubjectCode_1 | 登录态；细粒度权限见权限矩阵 | subjectCode[query,必填]:string | 200=*/*:ResultListExamResponse |
| GET | `/edu/exam/teacher` | listByTeacherId | 登录态；细粒度权限见权限矩阵 | teacherId[query,必填]:integer/int64 | 200=*/*:ResultListExamResponse |
| POST | `/edu/exam/update` | update_15 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:ExamRequest | 200=*/*:ResultVoid |

### intent-def-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/admin/intent/batch-delete` | deleteBatch_1 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:array<integer/int64> | 200=*/*:ResultVoid |
| POST | `/admin/intent/create` | create_24 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:IntentDefRequest | 200=*/*:ResultIntentDefVO |
| POST | `/admin/intent/delete` | delete_22 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/admin/intent/detail` | detail | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultIntentDefVO |
| GET | `/admin/intent/options` | options_2 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListIdNameOptionVO |
| GET | `/admin/intent/page` | page_7 | 登录态；细粒度权限见权限矩阵 | agentId[query,可选]:string; name[query,可选]:string; code[query,可选]:string; category[query,可选]:string; pageNo[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataIntentDefVO |
| POST | `/admin/intent/update` | update_22 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:IntentDefRequest | 200=*/*:ResultIntentDefVO |

### question-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/edu/question/create` | create_16 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:QuestionRequest | 200=*/*:ResultQuestionResponse |
| POST | `/edu/question/delete` | delete_14 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/edu/question/detail` | getById_12 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultQuestionResponse |
| GET | `/edu/question/difficulty` | listByDifficulty | 登录态；细粒度权限见权限矩阵 | difficulty[query,必填]:integer/int32 | 200=*/*:ResultListQuestionResponse |
| GET | `/edu/question/list` | list_8 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataQuestionResponse |
| GET | `/edu/question/subject-grade` | listBySubjectAndGrade_1 | 登录态；细粒度权限见权限矩阵 | subjectCode[query,必填]:string; grade[query,必填]:integer/int32 | 200=*/*:ResultListQuestionResponse |
| GET | `/edu/question/type` | listByType_1 | 登录态；细粒度权限见权限矩阵 | type[query,必填]:string | 200=*/*:ResultListQuestionResponse |
| POST | `/edu/question/update` | update_14 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:QuestionRequest | 200=*/*:ResultVoid |

### resource-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/edu/resource/create` | create_15 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ResourceRequest | 200=*/*:ResultResourceResponse |
| POST | `/edu/resource/delete` | delete_13 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/edu/resource/detail` | getById_11 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultResourceResponse |
| GET | `/edu/resource/list` | list_7 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataResourceResponse |
| GET | `/edu/resource/subject` | listBySubjectCode | 登录态；细粒度权限见权限矩阵 | subjectCode[query,必填]:string | 200=*/*:ResultListResourceResponse |
| GET | `/edu/resource/type` | listByType | 登录态；细粒度权限见权限矩阵 | type[query,必填]:string | 200=*/*:ResultListResourceResponse |
| POST | `/edu/resource/update` | update_13 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:ResourceRequest | 200=*/*:ResultVoid |

### review-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/edu/review/complete` | complete | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:CompleteReviewRequest | 200=*/*:ResultVoid |
| POST | `/edu/review/create` | create_14 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ReviewRequest | 200=*/*:ResultReviewTaskResponse |
| POST | `/edu/review/delete` | delete_12 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/edu/review/detail` | getById_10 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultReviewTaskResponse |
| GET | `/edu/review/list` | list_6 | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64; status[query,必填]:integer/int32 | 200=*/*:ResultListReviewTaskResponse |
| GET | `/edu/review/today` | listTodayTasks | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListReviewTaskResponse |
| POST | `/edu/review/update` | update_12 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:ReviewRequest | 200=*/*:ResultVoid |

### role-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/role/all` | getAllRoles | 登录态；细粒度权限见权限矩阵 | status[query,可选]:string; tenantId[query,可选]:integer/int64 | 200=*/*:ResultListRoleVO |
| POST | `/role/create` | createRole | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:RoleRequest | 200=*/*:ResultVoid |
| POST | `/role/delete` | deleteRole | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/role/detail` | getRoleDetail | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; tenantId[query,必填]:integer/int64 | 200=*/*:ResultRoleVO |
| GET | `/role/list` | getRoleList | 登录态；细粒度权限见权限矩阵 | r[query,必填]:RolePageRequest | 200=*/*:ResultPageDataRoleVO |
| POST | `/role/menus/replace` | replaceRoleMenus | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; tenantId[query,必填]:integer/int64; body[必填]=application/json:array<integer/int64> | 200=*/*:ResultVoid |
| POST | `/role/update` | updateRole | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:RoleRequest | 200=*/*:ResultVoid |
| POST | `/role/users/add` | assignUserRoles | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:AssignUserRolesRequest | 200=*/*:ResultVoid |
| POST | `/role/users/remove` | removeUserRoles | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:AssignUserRolesRequest | 200=*/*:ResultVoid |

### student-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/edu/student/create` | create_13 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:StudentRequest | 200=*/*:ResultStudentResponse |
| POST | `/edu/student/delete` | delete_11 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/edu/student/detail` | getById_9 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultStudentResponse |
| GET | `/edu/student/list` | list_5 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataStudentResponse |
| POST | `/edu/student/update` | update_11 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:StudentRequest | 200=*/*:ResultVoid |
| GET | `/edu/student/user` | getByUserId | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64 | 200=*/*:ResultStudentResponse |

### study-plan-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/edu/study-plan/active` | listActiveByStudent | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListStudyPlanResponse |
| POST | `/edu/study-plan/create` | create_12 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:StudyPlanRequest | 200=*/*:ResultStudyPlanResponse |
| POST | `/edu/study-plan/delete` | delete_10 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/edu/study-plan/detail` | getById_8 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultStudyPlanResponse |
| GET | `/edu/study-plan/student` | listByStudentId_1 | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListStudyPlanResponse |
| GET | `/edu/study-plan/today-tasks` | getTodayTasks | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListDailyTaskResponse |
| POST | `/edu/study-plan/update` | update_10 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:StudyPlanRequest | 200=*/*:ResultVoid |

### subject-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/edu/subject/code` | getByCode | 登录态；细粒度权限见权限矩阵 | code[query,必填]:string | 200=*/*:ResultSubjectResponse |
| POST | `/edu/subject/create` | create_11 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:SubjectRequest | 200=*/*:ResultSubjectResponse |
| POST | `/edu/subject/delete` | delete_9 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/edu/subject/detail` | getById_7 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultSubjectResponse |
| GET | `/edu/subject/grade-level` | listByGradeLevel | 登录态；细粒度权限见权限矩阵 | gradeLevel[query,必填]:string | 200=*/*:ResultListSubjectResponse |
| GET | `/edu/subject/list` | list_4 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataSubjectResponse |
| POST | `/edu/subject/update` | update_9 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:SubjectRequest | 200=*/*:ResultVoid |

### tenant-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/tenant/create` | createTenant | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:TenantRequest | 200=*/*:ResultVoid |
| POST | `/tenant/delete` | deleteTenant | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/tenant/detail` | getTenantById | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultTenantVO |
| GET | `/tenant/list` | getAllTenants | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListTenantVO |
| GET | `/tenant/page` | getTenantPage | 登录态；细粒度权限见权限矩阵 | r[query,必填]:TenantPageRequest | 200=*/*:ResultPageDataTenantVO |
| POST | `/tenant/update` | updateTenant | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:TenantRequest | 200=*/*:ResultVoid |

### textbook-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/edu/textbook/create` | create_10 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:TextbookRequest | 200=*/*:ResultTextbookResponse |
| POST | `/edu/textbook/delete` | delete_8 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/edu/textbook/detail` | getById_6 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultTextbookResponse |
| GET | `/edu/textbook/list` | list_3 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataTextbookResponse |
| GET | `/edu/textbook/subject-grade` | listBySubjectAndGrade | 登录态；细粒度权限见权限矩阵 | subjectCode[query,必填]:string; grade[query,必填]:integer/int32 | 200=*/*:ResultListTextbookResponse |
| POST | `/edu/textbook/update` | update_8 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:TextbookRequest | 200=*/*:ResultVoid |

### timezone-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/system/timezone/current` | getTimezone | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultString |
| GET | `/system/timezone/options` | getTimezoneOptions | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListTimezoneOptionVO |
| POST | `/system/timezone/set` | setTimezone | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:SetTimezoneRequest | 200=*/*:ResultVoid |

### user-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/user/create` | createUser | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:UserRequest | 200=*/*:ResultMapStringObject |
| POST | `/user/delete` | deleteUser | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/user/detail` | getUserInfo | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultUserVO |
| GET | `/user/list` | getUserList | 登录态；细粒度权限见权限矩阵 | r[query,必填]:UserPageRequest | 200=*/*:ResultPageDataUserVO |
| POST | `/user/password/change` | changePassword | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64; body[必填]=application/json:ChangePasswordRequest | 200=*/*:ResultVoid |
| POST | `/user/password/reset` | resetPassword | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64; body[必填]=application/json:ResetPasswordRequest | 200=*/*:ResultVoid |
| GET | `/user/tenant-assignments` | getTenantAssignments | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64 | 200=*/*:ResultListUserTenantAssignmentVO |
| POST | `/user/tenant-assignments/replace` | replaceTenantAssignments | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64; body[必填]=application/json:array<UserTenantRoleRequest> | 200=*/*:ResultVoid |
| POST | `/user/update` | updateUser | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64; body[必填]=application/json:UserRequest | 200=*/*:ResultVoid |

### wrong-question-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/edu/wrong-question/create` | create_9 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:WrongQuestionRequest | 200=*/*:ResultWrongQuestionResponse |
| POST | `/edu/wrong-question/delete` | delete_7 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/edu/wrong-question/detail` | getById_5 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultWrongQuestionResponse |
| GET | `/edu/wrong-question/student` | listByStudentId | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListWrongQuestionResponse |
| POST | `/edu/wrong-question/update` | update_7 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:WrongQuestionRequest | 200=*/*:ResultVoid |

### 插件系统

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/plugin/list` | 列出所有插件 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListPluginInfoVO |
| POST | `/plugin/scan` | 重新扫描插件目录 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultVoid |
| POST | `/plugin/start` | 启动插件 | 登录态；细粒度权限见权限矩阵 | pluginId[query,必填]:string | 200=*/*:ResultVoid |
| POST | `/plugin/stop` | 停止插件 | 登录态；细粒度权限见权限矩阵 | pluginId[query,必填]:string | 200=*/*:ResultVoid |
| POST | `/plugin/uninstall` | 卸载插件 | 登录态；细粒度权限见权限矩阵 | pluginId[query,必填]:string | 200=*/*:ResultVoid |

### 时间线事件管理

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/record/timeline/create` | create | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:TimelineEventRequest | 200=*/*:ResultTimelineEventVO |
| POST | `/record/timeline/delete` | delete_2 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultBoolean |
| GET | `/record/timeline/detail` | getById | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultTimelineEventVO |
| GET | `/record/timeline/list` | getPage | 登录态；细粒度权限见权限矩阵 | query[query,必填]:PageQuery; profileId[query,可选]:integer/int64 | 200=*/*:ResultPageDataTimelineEventVO |
| GET | `/record/timeline/profile` | getTimelineByProfileId | 登录态；细粒度权限见权限矩阵 | profileId[query,必填]:integer/int64 | 200=*/*:ResultListTimelineEventVO |
| POST | `/record/timeline/update` | update_2 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:TimelineEventRequest | 200=*/*:ResultBoolean |

### 智能推荐

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/edu/api/v1/recommend/hybrid` | 混合推荐 — 聚合知识点/题目/资源/复习 + AI 综合学习建议 | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultHybridRecommendResponse |
| GET | `/edu/api/v1/recommend/knowledge` | 推荐薄弱知识点 — 基于能力差距 + 遗忘紧迫度 | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64; topK[query,可选]:integer/int32 | 200=*/*:ResultListKnowledgeRecommendResponse |
| GET | `/edu/api/v1/recommend/questions` | 推荐题目 — 基于薄弱知识点 + 难度匹配 + 能力维度 | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64; count[query,可选]:integer/int32 | 200=*/*:ResultListQuestionRecommendResponse |
| GET | `/edu/api/v1/recommend/resources` | 推荐学习资源 — 基于薄弱点 + 最近学习知识点 | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64; topK[query,可选]:integer/int32 | 200=*/*:ResultListResourceRecommendResponse |
| GET | `/edu/api/v1/recommend/review` | 推荐复习任务 — 基于遗忘曲线的到期/即将到期复习 | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64; count[query,可选]:integer/int32 | 200=*/*:ResultListQuestionRecommendResponse |

### 标签管理

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/record/tag/all` | getAll | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListTagVO |
| POST | `/record/tag/create` | create_1 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:TagRequest | 200=*/*:ResultTagVO |
| POST | `/record/tag/delete` | delete_3 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultBoolean |
| GET | `/record/tag/detail` | getById_1 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultTagVO |
| GET | `/record/tag/list` | getPage_1 | 登录态；细粒度权限见权限矩阵 | query[query,必填]:PageQuery; name[query,可选]:string | 200=*/*:ResultPageDataTagVO |
| POST | `/record/tag/update` | update_3 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:TagRequest | 200=*/*:ResultBoolean |

### 档案管理

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/record/profile/create` | create_3 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ProfileRequest | 200=*/*:ResultProfileVO |
| POST | `/record/profile/delete` | delete_5 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultBoolean |
| GET | `/record/profile/detail` | getById_3 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultProfileVO |
| GET | `/record/profile/list` | getPage_3 | 登录态；细粒度权限见权限矩阵 | query[query,必填]:PageQuery; createBy[query,可选]:string | 200=*/*:ResultPageDataProfileVO |
| POST | `/record/profile/update` | update_5 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:ProfileRequest | 200=*/*:ResultBoolean |

### 知识任务

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/knowledge/ingestion-jobs` | page_4 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32; spaceId[query,可选]:integer/int64; status[query,可选]:string; version[header,可选]:string | 200=*/*:ResultPageDataJobView |
| GET | `/knowledge/ingestion-jobs/{id}` | get_2 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultJobView |
| POST | `/knowledge/ingestion-jobs/{id}/cancel` | cancel | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultVoid |
| POST | `/knowledge/ingestion-jobs/{id}/retry` | retry | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultVoid |

### 知识关系

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/knowledge/points/{pointId}/relations` | list_1 | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListRelationView |
| POST | `/knowledge/points/{pointId}/relations` | create_7 | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:RelationRequest | 200=*/*:ResultVoid |
| DELETE | `/knowledge/points/{pointId}/relations/{targetId}` | delete_25 | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; targetId[path,必填]:integer/int64; type[query,必填]:string(PRE,NEXT,INCLUDE,RELATED,SIMILAR,BELONG); version[header,可选]:string | 200=*/*:ResultVoid |

### 知识平台审计

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/knowledge/audits` | page_5 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32; spaceId[query,可选]:integer/int64; version[header,可选]:string | 200=*/*:ResultPageDataKnowledgeAuditResponse |

### 知识引擎运维

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/knowledge/system/backup` | backup | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string | 200=*/*:ResultBackupResult |
| POST | `/knowledge/system/restore-check` | restoreCheck | 登录态；细粒度权限见权限矩阵 | fileName[query,必填]:string; version[header,可选]:string | 200=*/*:ResultRestoreCheckResult |
| GET | `/knowledge/system/status` | status | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string | 200=*/*:ResultMapStringObject |

### 知识文档

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/knowledge/documents/upload-sessions/{sessionId}` | uploadStatus | 登录态；细粒度权限见权限矩阵 | sessionId[path,必填]:string; version[header,可选]:string | 200=*/*:ResultUploadSession |
| DELETE | `/knowledge/documents/upload-sessions/{sessionId}` | cancelUpload | 登录态；细粒度权限见权限矩阵 | sessionId[path,必填]:string; version[header,可选]:string | 200=*/*:ResultVoid |
| POST | `/knowledge/documents/upload-sessions/{sessionId}/chunks/{index}` | uploadChunk | 登录态；细粒度权限见权限矩阵 | sessionId[path,必填]:string; index[path,必填]:integer/int32; totalChunks[query,必填]:integer/int32; version[header,可选]:string; body[可选]=multipart/form-data:object | 200=*/*:ResultUploadSession |
| POST | `/knowledge/documents/upload-sessions/{sessionId}/complete` | completeUpload | 登录态；细粒度权限见权限矩阵 | sessionId[path,必填]:string; version[header,可选]:string | 200=*/*:ResultUploadResult |
| GET | `/knowledge/documents/{id}` | get_3 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultDocumentView |
| DELETE | `/knowledge/documents/{id}` | delete_23 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultVoid |
| POST | `/knowledge/documents/{id}/approve` | approve | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; comment[query,可选]:string; version[header,可选]:string | 200=*/*:ResultDocumentView |
| POST | `/knowledge/documents/{id}/archive` | archive | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; comment[query,可选]:string; version[header,可选]:string | 200=*/*:ResultDocumentView |
| GET | `/knowledge/documents/{id}/preview` | preview | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:string/byte |
| POST | `/knowledge/documents/{id}/publish` | publish | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; comment[query,可选]:string; version[header,可选]:string | 200=*/*:ResultDocumentView |
| POST | `/knowledge/documents/{id}/reject` | reject | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; comment[query,可选]:string; version[header,可选]:string | 200=*/*:ResultDocumentView |
| POST | `/knowledge/documents/{id}/submit` | submit | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; comment[query,可选]:string; version[header,可选]:string | 200=*/*:ResultDocumentView |
| GET | `/knowledge/documents/{id}/versions` | versions | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListVersionView |
| POST | `/knowledge/documents/{id}/versions/{versionId}/rollback` | rollback | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; versionId[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultDocumentView |
| GET | `/knowledge/spaces/{spaceId}/documents` | page_2 | 登录态；细粒度权限见权限矩阵 | spaceId[path,必填]:integer/int64; pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32; keyword[query,可选]:string; lifecycleStatus[query,可选]:string; parseStatus[query,可选]:string; version[header,可选]:string | 200=*/*:ResultPageDataDocumentView |
| POST | `/knowledge/spaces/{spaceId}/documents` | upload_1 | 登录态；细粒度权限见权限矩阵 | spaceId[path,必填]:integer/int64; title[query,可选]:string; version[header,可选]:string; body[可选]=multipart/form-data:object | 200=*/*:ResultUploadResult |
| POST | `/knowledge/spaces/{spaceId}/documents/import-url` | importUrl | 登录态；细粒度权限见权限矩阵 | spaceId[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:ImportUrlRequest | 200=*/*:ResultUploadResult |
| POST | `/knowledge/spaces/{spaceId}/documents/upload-sessions` | beginUpload | 登录态；细粒度权限见权限矩阵 | spaceId[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:BeginRequest | 200=*/*:ResultUploadSession |

### 知识检索

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/knowledge/index-jobs/rebuild` | rebuild | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string; body[必填]=application/json:RebuildRequest | 200=*/*:ResultLong |
| POST | `/knowledge/search` | search | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string; body[必填]=application/json:SearchRequest | 200=*/*:ResultSearchResponse |

### 知识点

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/knowledge/points/{id}` | get_1 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultPointView |
| PUT | `/knowledge/points/{id}` | update_1 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:UpdatePointRequest | 200=*/*:ResultPointView |
| DELETE | `/knowledge/points/{id}` | delete_1 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultVoid |
| GET | `/knowledge/points/{id}/graph` | graph | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultKnowledgeGraphResponse |
| GET | `/knowledge/spaces/{spaceId}/points` | page_1 | 登录态；细粒度权限见权限矩阵 | spaceId[path,必填]:integer/int64; pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32; keyword[query,可选]:string; category[query,可选]:string; version[header,可选]:string | 200=*/*:ResultPageDataPointView |
| POST | `/knowledge/spaces/{spaceId}/points` | create_6 | 登录态；细粒度权限见权限矩阵 | spaceId[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:CreatePointRequest | 200=*/*:ResultPointView |

### 知识点文档关系

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/knowledge/documents/{documentId}/points` | listPoints | 登录态；细粒度权限见权限矩阵 | documentId[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListLong |
| PUT | `/knowledge/documents/{documentId}/points` | replacePoints | 登录态；细粒度权限见权限矩阵 | documentId[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:ReplacePointsRequest | 200=*/*:ResultVoid |
| GET | `/knowledge/documents/{documentId}/relations` | listDocumentRelations | 登录态；细粒度权限见权限矩阵 | documentId[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListDocumentRelationView |
| PUT | `/knowledge/documents/{documentId}/relations` | replaceDocumentRelations | 登录态；细粒度权限见权限矩阵 | documentId[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:ReplaceDocumentRelationsRequest | 200=*/*:ResultVoid |
| GET | `/knowledge/points/{pointId}/documents` | list | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListDocumentSummary |
| PUT | `/knowledge/points/{pointId}/documents` | replace | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:ReplaceRequest | 200=*/*:ResultVoid |

### 知识空间

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/knowledge/spaces` | page | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32; keyword[query,可选]:string; domainCode[query,可选]:string; version[header,可选]:string | 200=*/*:ResultPageDataSpaceView |
| POST | `/knowledge/spaces` | create_5 | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string; body[必填]=application/json:CreateSpaceRequest | 200=*/*:ResultSpaceView |
| POST | `/knowledge/spaces/default` | ensureDefault | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string | 200=*/*:ResultSpaceView |
| GET | `/knowledge/spaces/options` | options | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListSpaceView |
| GET | `/knowledge/spaces/{id}` | get | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultSpaceView |
| PUT | `/knowledge/spaces/{id}` | update | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:UpdateSpaceRequest | 200=*/*:ResultSpaceView |
| DELETE | `/knowledge/spaces/{id}` | delete | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultVoid |
| GET | `/knowledge/spaces/{id}/difficulty-scale` | difficultyScale | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultDifficultyScaleView |
| GET | `/knowledge/spaces/{id}/members` | members | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListMemberView |
| PUT | `/knowledge/spaces/{id}/members` | replaceMembers | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:array<MemberRequest> | 200=*/*:ResultVoid |

### 知识评测

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/knowledge/evaluations` | page_3 | 登录态；细粒度权限见权限矩阵 | spaceId[query,必填]:integer/int64; pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32; version[header,可选]:string | 200=*/*:ResultPageDataCaseView |
| POST | `/knowledge/evaluations` | create_8 | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string; body[必填]=application/json:CreateCaseRequest | 200=*/*:ResultCaseView |
| POST | `/knowledge/evaluations/run` | run | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string; body[必填]=application/json:RunRequest | 200=*/*:ResultRunResult |
| DELETE | `/knowledge/evaluations/{id}` | delete_26 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultVoid |

### 知识路径

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/knowledge/points/path` | findPath | 登录态；细粒度权限见权限矩阵 | fromId[query,必填]:integer/int64; toId[query,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListLong |
| GET | `/knowledge/points/{pointId}/path` | path | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListLong |
| GET | `/knowledge/points/{pointId}/prerequisites` | prerequisites | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; masteredIds[query,可选]:array<integer/int64>; version[header,可选]:string | 200=*/*:ResultListLong |

### 记录管理

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/record/record/create` | create_2 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:RecordRequest | 200=*/*:ResultRecordVO |
| POST | `/record/record/delete` | delete_4 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultBoolean |
| GET | `/record/record/detail` | getById_2 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultRecordVO |
| GET | `/record/record/list` | getPage_2 | 登录态；细粒度权限见权限矩阵 | query[query,必填]:PageQuery; eventId[query,可选]:integer/int64 | 200=*/*:ResultPageDataRecordVO |
| POST | `/record/record/update` | update_4 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:RecordRequest | 200=*/*:ResultBoolean |

### 附件管理

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/record/media/create` | create_4 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:MediaRequest | 200=*/*:ResultMediaVO |
| POST | `/record/media/delete` | delete_6 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultBoolean |
| GET | `/record/media/detail` | 查询附件 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultMediaVO |
| GET | `/record/media/list` | 分页查询附件列表 | 登录态；细粒度权限见权限矩阵 | query[query,必填]:PageQuery; recordId[query,可选]:integer/int64 | 200=*/*:ResultPageDataMediaVO |
| POST | `/record/media/update` | update_6 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:MediaRequest | 200=*/*:ResultBoolean |

## 组件模型

| Schema | 类型 | 必填字段 | 字段定义 |
|---|---|---|---|
| `AbilityRadarResponse` | object | - | studentId:integer/int64; knowledgeId:integer/int64; abilities:object; overallMastery:number/double |
| `AgentDefinition` | object | - | agentId:string; name:string; description:string; extInfo:object; currentVersion:string; createdAt:integer/int64; updatedAt:integer/int64; startNodeId:string; versions:object |
| `AgentDetailVO` | object | - | id:integer/int64; agentId:string; name:string; description:string; currentVersion:string; status:integer/int32; extInfo:object; versions:array<AgentVersionVO>; createTime:string/date-time; updateTime:string/date-time |
| `AgentRequest` | object | agentId, name | agentId*:string; name*:string; description:string; status:integer/int32 |
| `AgentStateFactoryAgentState` | - | - | - |
| `AgentVO` | object | - | id:integer/int64; agentId:string; name:string; description:string; currentVersion:string; status:integer/int32; extInfo:object; createTime:string/date-time; updateTime:string/date-time |
| `AgentVersion` | object | - | versionNumber:string; description:string; createdAt:integer/int64 |
| `AgentVersionDetailVO` | object | - | id:integer/int64; agentId:string; versionNumber:string; description:string; status:integer/int32; statusDesc:string; graphConfig:GraphConfigVO; canvasConfig:string; createTime:string/date-time; updateTime:string/date-time |
| `AgentVersionVO` | object | - | id:integer/int64; agentId:string; versionNumber:string; description:string; status:integer/int32; statusDesc:string; createTime:string/date-time; updateTime:string/date-time |
| `AiModelRequest` | object | modelName | platformId:integer/int64; modelName*:string; displayName:string; description:string; modelConfig:string; isDefault:string; sort:integer/int32; status:string |
| `AiModelResponse` | object | - | id:integer/int64; platformId:integer/int64; modelName:string; displayName:string; description:string; modelConfig:string; platformName:string; isDefault:string; sort:integer/int32; status:string; createTime:string/date-time; updateTime:string/date-time |
| `AiModelVO` | object | - | id:integer/int64; platformId:integer/int64; modelName:string; displayName:string; description:string; modelConfig:string; platformName:string; isDefault:string; sort:integer/int32; status:string; createTime:string/date-time; updateTime:string/date-time |
| `AiPlatformRequest` | object | code, name | name*:string; code*:string; baseUrl:string; apiKey:string; temperature:number/double; maxTokens:integer/int32; maxRetries:integer/int32; availableModels:string; extraConfig:string; isDefault:string; status:string; sort:integer/int32; remark:string |
| `AiPlatformResponse` | object | - | id:integer/int64; name:string; code:string; baseUrl:string; temperature:number/double; maxTokens:integer/int32; maxRetries:integer/int32; availableModels:string; extraConfig:string; isDefault:string; status:string; sort:integer/int32; remark:string; createTime:string/date-time; updateTime:string/date-time |
| `AiPlatformVO` | object | - | id:integer/int64; name:string; code:string; baseUrl:string; temperature:number/double; maxTokens:integer/int32; maxRetries:integer/int32; availableModels:string; extraConfig:string; isDefault:string; status:string; sort:integer/int32; remark:string; createTime:string/date-time; updateTime:string/date-time |
| `AssignUserRolesRequest` | object | tenantId | userIds:array<integer/int64>; tenantId*:integer/int64 |
| `AuthCodeOptionVO` | object | - | id:integer/int64; name:string; code:string; module:string; resource:string; action:string; status:integer/int32; createTime:string/date-time |
| `AuthCodePageRequest` | object | - | pageSize:integer/int32; pageNum:integer/int32; orderByColumn:string; isAsc:string; orderFields:array<OrderField>; filterConditions:array<FilterCondition>; code:string; name:string |
| `AuthCodeRequest` | object | code | code*:string; name:string |
| `AuthCodeResponse` | object | - | id:integer/int64; code:string; name:string; status:integer/int32; createTime:string/date-time; updateTime:string/date-time |
| `BackupResult` | object | - | fileName:string; size:integer/int64; createdAt:string |
| `BaseNode` | object | - | config:NodeConfig; executionHistoryService:ExecutionHistoryService; requiredInputs:array<NodeInputParam> |
| `BeginRequest` | object | fileName | fileName*:string; contentType:string; size:integer/int64; checksum:string; title:string |
| `CaptchaVO` | object | - | key:string; image:string; expireTime:integer/int64 |
| `CaseResult` | object | - | caseId:integer/int64; question:string; recallAtK:number/double; reciprocalRank:number/double; citationAccuracy:number/double; expectedDocumentIds:array<integer/int64>; returnedDocumentIds:array<integer/int64> |
| `CaseView` | object | - | id:integer/int64; spaceId:integer/int64; question:string; expectedDocIds:string; expectedAnswer:string |
| `ChangePasswordRequest` | object | newPassword, oldPassword | oldPassword*:string; newPassword*:string |
| `ChannelObject` | object | - | default:-; reducer:ReducerObject |
| `ChapterRequest` | object | name, textbookId | id:integer/int64; textbookId*:integer/int64; name*:string; parentId:integer/int64; chapterOrder:integer/int32; status:integer/int32 |
| `ChapterResponse` | object | - | id:integer/int64; textbookId:integer/int64; parentId:integer/int64; name:string; chapterOrder:integer/int32; children:array<ChapterResponse> |
| `ChatResponse` | object | - | success:boolean; content:string; platform:string; model:string; errorMessage:string |
| `CodeLoginRequest` | object | captchaKey, code, phone | phone*:string; code*:string; captchaKey*:string |
| `CompileConfig` | - | - | - |
| `CompiledGraphAgentState` | object | - | stateGraph:StateGraphAgentState; maxIterations:integer/int32; compileConfig:CompileConfig |
| `CompleteReviewRequest` | object | - | studentId:integer/int64; resultScore:number/double |
| `ConditionEdge` | object | - | from:string; defaultTarget:string; functionCondition:-; nodeMappings:object; predicateConditions:array<PredicateCondition> |
| `ConditionalEdgeDTO` | object | - | defaultTarget:string; nodeMappings:object; conditionType:string |
| `CourseRequest` | object | name | id:integer/int64; name*:string; subjectCode:string; grade:integer/int32; description:string; coverUrl:string; textbookId:integer/int64; teacherId:integer/int64; totalHours:integer/int32; status:integer/int32 |
| `CourseResponse` | object | - | id:integer/int64; name:string; description:string; subjectCode:string; grade:integer/int32; textbookId:integer/int64; teacherId:integer/int64; coverUrl:string; totalHours:integer/int32; status:integer/int32 |
| `CreateCaseRequest` | object | question | spaceId:integer/int64; question*:string; expectedDocIds:string; expectedAnswer:string |
| `CreatePointRequest` | object | code, name | code*:string; name*:string; description:string; difficultyLevel:integer/int32; category:string; tags:string |
| `CreateSpaceRequest` | object | code, name | code*:string; name*:string; domainCode:string; description:string; accessMode:string; reviewMode:string; bindingMode:string; difficultyScaleId:integer/int64; embeddingProfile:string; rerankProfile:string; chunkStrategy:string; chunkSize:integer/int32; chunkOverlap:integer/int32 |
| `DailyTaskResponse` | object | - | id:integer/int64; knowledgeId:integer/int64; knowledgeName:string; planDate:string; status:integer/int32; statusDesc:string; orderNo:integer/int32 |
| `DataSourceConfig` | object | - | type:string; url:string; dictType:string; labelKey:string; valueKey:string; dependsOn:string |
| `DemoChatRequest` | object | - | platform:string; model:string; prompt:string |
| `DemoChatResponse` | object | - | success:boolean; content:string |
| `DictPageRequest` | object | - | pageSize:integer/int32; pageNum:integer/int32; orderByColumn:string; isAsc:string; orderFields:array<OrderField>; filterConditions:array<FilterCondition> |
| `DictRequest` | object | - | dictType:string; dictLabel:string; dictValue:string; dictSort:integer/int32; cssClass:string; listClass:string; isDefault:string; remark:string; status:integer/int32 |
| `DictVO` | object | - | id:integer/int64; tenantId:integer/int64; dictType:string; dictLabel:string; dictValue:string; dictSort:integer/int32; cssClass:string; listClass:string; isDefault:string; status:string; remark:string; createTime:string/date-time; updateTime:string/date-time |
| `DifficultyLevelView` | object | - | level:integer/int32; label:string; description:string |
| `DifficultyScaleView` | object | - | id:integer/int64; code:string; name:string; description:string; levelCount:integer/int32; levels:array<DifficultyLevelView> |
| `DocumentRelationRequest` | object | - | documentId:integer/int64; relationType:string |
| `DocumentRelationView` | object | - | id:integer/int64; sourceDocumentId:integer/int64; targetDocumentId:integer/int64; relationType:string; targetTitle:string |
| `DocumentSummary` | object | - | id:integer/int64; spaceId:integer/int64; title:string; docType:string; lifecycleStatus:string; parseStatus:string |
| `DocumentView` | object | - | id:integer/int64; spaceId:integer/int64; currentVersionId:integer/int64; title:string; docType:string; source:string; lifecycleStatus:string; parseStatus:string; objectKey:string; mimeType:string; fileSize:integer/int64; checksum:string; createTime:string/date-time; updateTime:string/date-time |
| `EdgeRequest` | object | sourceNodeId, targetNodeId | sourceNodeId*:string; targetNodeId*:string; edgeType:string; conditionMappings:object; defaultTarget:string; conditionType:string |
| `ExamRequest` | object | durationMin, grade, name, subjectCode, totalScore, type | id:integer/int64; name*:string; type*:string; subjectCode*:string; grade*:integer/int32; teacherId:integer/int64; durationMin*:integer/int32; totalScore*:integer/int32; status:integer/int32 |
| `ExamResponse` | object | - | id:integer/int64; name:string; type:string; subjectCode:string; grade:integer/int32; teacherId:integer/int64; durationMin:integer/int32; totalScore:integer/int32; status:integer/int32 |
| `ExecutionHistoryService` | - | - | - |
| `FieldMeta` | object | - | key:string; label:string; type:string; defaultValue:-; required:boolean; options:object; description:string; source:DataSourceConfig |
| `FileView` | object | - | key:string; name:string; size:integer/int64; contentType:string; lastModified:string/date-time; url:string; storageType:string |
| `FilterCondition` | object | - | field:string; operator:string(EQ,NE,GT,GE,LT,LE,LIKE,IN,NOT_IN,IS_NULL,IS_NOT_NULL); value:- |
| `ForgetPasswordRequest` | object | captchaKey, code, email, newPassword | email*:string/email; newPassword*:string; code*:string; captchaKey*:string |
| `Graph` | object | - | name:string; description:string; nodes:object; edges:object; conditionalEdges:object; channels:object; startNode:string; endNode:string; compiledGraph:CompiledGraphAgentState; compiled:boolean |
| `GraphConfigRequest` | object | - | name:string; description:string; startNode:string; endNode:string; nodes:object; edges:object; conditionalEdges:object |
| `GraphConfigVO` | object | - | name:string; description:string; startNode:string; endNode:string; nodes:object; edges:object; conditionalEdges:object |
| `GraphValidationVO` | object | - | valid:boolean; errors:array<string>; warnings:array<string> |
| `HybridHit` | object | - | chunkId:integer/int64; documentId:integer/int64; content:string; highlight:string; bm25Score:number/double; vectorScore:number/double; rrfScore:number/double; rerankScore:number/double |
| `HybridRecommendResponse` | object | - | studentId:integer/int64; knowledgeTop:array<KnowledgeRecommendResponse>; questionTop:array<QuestionRecommendResponse>; resourceTop:array<ResourceRecommendResponse>; reviewTop:array<QuestionRecommendResponse>; overallAdvice:string; generateTime:integer/int64 |
| `IdNameOptionVO` | object | - | id:integer/int64; name:string; code:string; value:string |
| `ImportUrlRequest` | object | url | url*:string; title:string |
| `IntentDefRequest` | object | agentId, code, name | agentId*:string; code*:string; name*:string; description:string; category:string; priority:integer/int32; confidenceThreshold:number/double; targetNode:string; status:integer/int32 |
| `IntentDefVO` | object | - | id:integer/int64; agentId:string; name:string; code:string; category:string; description:string; status:string; createTime:string/date-time; updateTime:string/date-time |
| `JobView` | object | - | id:integer/int64; jobKey:string; jobType:string; spaceId:integer/int64; documentId:integer/int64; versionId:integer/int64; status:string; stage:string; progress:integer/int32; attempts:integer/int32; maxAttempts:integer/int32; errorMessage:string; heartbeatTime:string/date-time; startedTime:string/date-time; finishedTime:string/date-time; createTime:string/date-time |
| `KnowledgeAuditResponse` | object | - | id:integer/int64; tenantId:integer/int64; spaceId:integer/int64; resourceType:string; resourceId:integer/int64; action:string; detailJson:string; status:integer/int32; delFlag:integer/int32; createTime:string/date-time; updateTime:string/date-time |
| `KnowledgeDocumentDTO` | object | - | id:integer/int64; title:string; content:string; docType:string; source:string; knowledgeIds:array<integer/int64> |
| `KnowledgeGraphResponse` | object | - | node:KnowledgeResponse; parentNodes:array<KnowledgeResponse>; childNodes:array<KnowledgeResponse>; relatedNodes:array<KnowledgeResponse> |
| `KnowledgeRecommendResponse` | object | - | knowledgeId:integer/int64; knowledgeName:string; mastery:number/double; recommendType:string; reason:string; score:integer/int32 |
| `KnowledgeResponse` | object | - | id:integer/int64; code:string; name:string; description:string; difficulty:integer/int32; category:string; tags:string; parentIds:array<integer/int64>; childIds:array<integer/int64>; documents:array<KnowledgeDocumentDTO> |
| `LoginRequest` | object | password, username | username*:string; password*:string; captcha:string; captchaKey:string; roleId:integer/int64; email:string; phone:string |
| `LoginResponseVO` | object | - | id:integer/int64; realName:string; roles:array<string>; username:string; homePath:string; accessToken:string; tokenType:string; expiresIn:integer/int64; currentTenantId:integer/int64; homeTenantId:integer/int64; switchMode:string; tenantName:string; tenants:array<TenantInfoVO>; subTenants:array<TenantContextVO> |
| `McpToolDescriptor` | object | - | name:string; description:string; serverId:string; parameters:object; tags:array<string>; category:string; builtin:boolean; registeredAt:integer/int64 |
| `MediaRequest` | object | recordId, url | recordId*:integer/int64; url*:string; mediaType:string |
| `MediaVO` | object | - | id:integer/int64; recordId:integer/int64; url:string; type:string; size:integer/int64; duration:integer/int32; width:integer/int32; height:integer/int32; sort:integer/int32; createTime:string/date-time |
| `MemberRequest` | object | principalId, principalType, spaceRole | principalType*:string; principalId*:integer/int64; spaceRole*:string |
| `MemberView` | object | - | id:integer/int64; spaceId:integer/int64; principalType:string; principalId:integer/int64; spaceRole:string |
| `MenuPageRequest` | object | - | pageSize:integer/int32; pageNum:integer/int32; orderByColumn:string; isAsc:string; orderFields:array<OrderField>; filterConditions:array<FilterCondition>; name:string; code:string; type:string; status:integer/int32 |
| `MenuRequest` | object | name, type | name*:string; code:string; type*:string; path:string; redirect:string; icon:string; component:string; layout:string; keepAlive:boolean; method:string; description:string; show:boolean; status:string; order:integer/int32; pid:integer/int64 |
| `MenuVO` | object | - | id:integer/int64; name:string; code:string; type:string; path:string; redirect:string; icon:string; component:string; layout:string; keepAlive:boolean; method:string; description:string; show:boolean; status:integer/int32; order:integer/int32; delFlag:integer/int32; createTime:string/date-time; updateTime:string/date-time; children:array<MenuVO>; pid:integer/int64 |
| `MetaVO` | object | - | title:string; activeIcon:string; activePath:string; affixTab:boolean; affixTabOrder:integer/int32; authority:array<string>; badge:string; badgeType:string; badgeVariants:string; fullPathKey:boolean; hideChildrenInMenu:boolean; hideInBreadcrumb:boolean; hideInMenu:boolean; hideInTab:boolean; icon:string; iframeSrc:string; ignoreAccess:boolean; keepAlive:boolean; link:string; loaded:boolean; maxNumOfOpenTab:integer/int32; menuVisibleWithForbidden:boolean; noBasicLayout:boolean; openInNewWindow:boolean; order:integer/int32; query:- |
| `NodeConfig` | object | - | nodeId:string; nodeName:string; description:string; nodeType:string(DEFAULT,INTENT,RAG_RETRIEVAL,RAG_ENHANCEMENT,MEMORY_SHORT_TERM,MEMORY_LONG_TERM,MEMORY_RETRIEVAL,LLM_CALL,TOOL_CALL,CONDITION,TRANSFORM,OUTPUT_FORMAT,AGENT_CALL,ABILITY_QUERY,EDUCATION_TEACH,EDUCATION_PRACTICE,SCORE_ANALYSIS,REVIEW_SCHEDULE,PREREQ_CHECK); enabled:boolean; timeout:integer/int64; retryCount:integer/int32; retryInterval:integer/int64; properties:object; errorStrategy:string; logLevel:string |
| `NodeConfigDTO` | object | - | nodeName:string; description:string; nodeType:string; enabled:boolean; timeout:integer/int64; retryCount:integer/int32; retryInterval:integer/int64; errorStrategy:string; logLevel:string; properties:object; config:object |
| `NodeConfigRequest` | object | nodeId, nodeName, nodeType | nodeId*:string; nodeName*:string; nodeType*:string; description:string; enabled:boolean; timeout:integer/int64; retryCount:integer/int32; retryInterval:integer/int64; errorStrategy:string; logLevel:string; properties:object; config:object |
| `NodeInputParam` | object | - | name:string; type:string; source:string(API_REQUEST,CONFIG_VALUE,PREVIOUS_NODE,DEFAULT_VALUE); required:boolean; description:string; defaultValue:- |
| `NodeTypeMetaVO` | object | - | code:string; name:string; description:string; icon:string; color:string; fields:array<FieldMeta> |
| `OrderField` | object | - | column:string; direction:string(ASC,DESC) |
| `OverviewResponse` | object | - | totalStudyDays:integer/int32; totalKnowledge:integer/int32; masteredKnowledge:integer/int32; totalQuestions:integer/int32; accuracy:number/double; weeklyHours:number/double; streakDays:integer/int32 |
| `PageDataAgentVO` | object | - | items:array<AgentVO>; total:integer/int64 |
| `PageDataAiModelVO` | object | - | items:array<AiModelVO>; total:integer/int64 |
| `PageDataAiPlatformVO` | object | - | items:array<AiPlatformVO>; total:integer/int64 |
| `PageDataAuthCodeOptionVO` | object | - | items:array<AuthCodeOptionVO>; total:integer/int64 |
| `PageDataCaseView` | object | - | items:array<CaseView>; total:integer/int64 |
| `PageDataCourseResponse` | object | - | items:array<CourseResponse>; total:integer/int64 |
| `PageDataDictVO` | object | - | items:array<DictVO>; total:integer/int64 |
| `PageDataDocumentView` | object | - | items:array<DocumentView>; total:integer/int64 |
| `PageDataExamResponse` | object | - | items:array<ExamResponse>; total:integer/int64 |
| `PageDataIntentDefVO` | object | - | items:array<IntentDefVO>; total:integer/int64 |
| `PageDataJobView` | object | - | items:array<JobView>; total:integer/int64 |
| `PageDataKnowledgeAuditResponse` | object | - | items:array<KnowledgeAuditResponse>; total:integer/int64 |
| `PageDataMediaVO` | object | - | items:array<MediaVO>; total:integer/int64 |
| `PageDataMenuVO` | object | - | items:array<MenuVO>; total:integer/int64 |
| `PageDataPointView` | object | - | items:array<PointView>; total:integer/int64 |
| `PageDataProfileVO` | object | - | items:array<ProfileVO>; total:integer/int64 |
| `PageDataQuestionResponse` | object | - | items:array<QuestionResponse>; total:integer/int64 |
| `PageDataRecordVO` | object | - | items:array<RecordVO>; total:integer/int64 |
| `PageDataResourceResponse` | object | - | items:array<ResourceResponse>; total:integer/int64 |
| `PageDataRoleVO` | object | - | items:array<RoleVO>; total:integer/int64 |
| `PageDataSpaceView` | object | - | items:array<SpaceView>; total:integer/int64 |
| `PageDataStudentResponse` | object | - | items:array<StudentResponse>; total:integer/int64 |
| `PageDataSubjectResponse` | object | - | items:array<SubjectResponse>; total:integer/int64 |
| `PageDataTagVO` | object | - | items:array<TagVO>; total:integer/int64 |
| `PageDataTenantVO` | object | - | items:array<TenantVO>; total:integer/int64 |
| `PageDataTextbookResponse` | object | - | items:array<TextbookResponse>; total:integer/int64 |
| `PageDataTimelineEventVO` | object | - | items:array<TimelineEventVO>; total:integer/int64 |
| `PageDataUserVO` | object | - | items:array<UserVO>; total:integer/int64 |
| `PageQuery` | object | - | pageSize:integer/int32; pageNum:integer/int32; orderByColumn:string; isAsc:string; orderFields:array<OrderField>; filterConditions:array<FilterCondition> |
| `ParameterInfo` | object | - | type:string; description:string; required:boolean; defaultValue:- |
| `PluginInfoVO` | object | - | id:string; name:string; version:string; description:string; state:string; loadedAt:string |
| `PointView` | object | - | id:integer/int64; spaceId:integer/int64; code:string; name:string; description:string; difficultyLevel:integer/int32; category:string; tags:string |
| `PredicateCondition` | object | - | predicate:-; target:string |
| `ProfileRequest` | object | name | name*:string; avatar:string |
| `ProfileVO` | object | - | id:integer/int64; name:string; gender:string; birthDate:string/date-time; avatar:string; status:integer/int32; createTime:string/date-time; updateTime:string/date-time |
| `QuestionRecommendResponse` | object | - | questionId:integer/int64; title:string; type:string; difficulty:integer/int32; knowledgeId:integer/int64; knowledgeName:string; recommendType:string; reason:string; score:integer/int32 |
| `QuestionRequest` | object | title, type | id:integer/int64; title*:string; type*:string; code:string; subjectCode:string; grade:integer/int32; difficulty:integer/int32; abilityDimension:string; options:string; answer:string; analysis:string; tags:string; status:integer/int32 |
| `QuestionResponse` | object | - | id:integer/int64; code:string; type:string; subjectCode:string; grade:integer/int32; difficulty:integer/int32; abilityDimension:string; title:string; options:string; answer:string; analysis:string; tags:string; usedCount:integer/int64 |
| `RebuildRequest` | object | spaceId | spaceId*:integer/int64 |
| `RecordRequest` | object | eventId | eventId*:integer/int64; content:string |
| `RecordVO` | object | - | id:integer/int64; eventId:integer/int64; content:string; mood:string; location:string; weather:string; createTime:string/date-time; mediaList:array<MediaVO>; tags:array<TagVO> |
| `ReducerObject` | - | - | - |
| `RefreshTokenRequest` | object | accessToken | accessToken*:string |
| `RegisterAgentRequest` | object | - | agentId:string; name:string; description:string; versionNumber:string; versionDescription:string; graph:Graph |
| `RelationRequest` | object | sourceId, targetId, type | sourceId*:integer/int64; targetId*:integer/int64; type*:string(PRE,NEXT,INCLUDE,RELATED,SIMILAR,BELONG); weight:number/double |
| `RelationView` | object | - | sourceId:integer/int64; targetId:integer/int64; relationType:string; weight:number/double; source:KnowledgeResponse; target:KnowledgeResponse |
| `ReplaceDocumentRelationsRequest` | object | relations | relations*:array<DocumentRelationRequest> |
| `ReplacePointsRequest` | object | pointIds | pointIds*:array<integer/int64>; relationType:string |
| `ReplaceRequest` | object | documentIds | documentIds*:array<integer/int64>; relationType:string |
| `ResetPasswordRequest` | object | - | password:string |
| `ResourceRecommendResponse` | object | - | resourceId:integer/int64; title:string; type:string; knowledgeId:integer/int64; knowledgeName:string; recommendType:string; reason:string; score:integer/int32 |
| `ResourceRequest` | object | name, type | id:integer/int64; name*:string; type*:string; subjectCode:string; grade:integer/int32; difficulty:integer/int32; coverUrl:string; url:string; description:string; status:integer/int32 |
| `ResourceResponse` | object | - | id:integer/int64; name:string; type:string; url:string; subjectCode:string; grade:integer/int32; difficulty:integer/int32; coverUrl:string; description:string; viewCount:integer/int64 |
| `RestoreCheckResult` | object | - | valid:boolean; entries:integer/int64; errors:array<string> |
| `ResultAbilityRadarResponse` | object | - | code:integer/int32; data:AbilityRadarResponse; message:string; error:string; success:boolean |
| `ResultAgentDefinition` | object | - | code:integer/int32; data:AgentDefinition; message:string; error:string; success:boolean |
| `ResultAgentDetailVO` | object | - | code:integer/int32; data:AgentDetailVO; message:string; error:string; success:boolean |
| `ResultAgentVO` | object | - | code:integer/int32; data:AgentVO; message:string; error:string; success:boolean |
| `ResultAgentVersionDetailVO` | object | - | code:integer/int32; data:AgentVersionDetailVO; message:string; error:string; success:boolean |
| `ResultAgentVersionVO` | object | - | code:integer/int32; data:AgentVersionVO; message:string; error:string; success:boolean |
| `ResultAiModelVO` | object | - | code:integer/int32; data:AiModelVO; message:string; error:string; success:boolean |
| `ResultAiPlatformResponse` | object | - | code:integer/int32; data:AiPlatformResponse; message:string; error:string; success:boolean |
| `ResultAiPlatformVO` | object | - | code:integer/int32; data:AiPlatformVO; message:string; error:string; success:boolean |
| `ResultAuthCodeResponse` | object | - | code:integer/int32; data:AuthCodeResponse; message:string; error:string; success:boolean |
| `ResultBackupResult` | object | - | code:integer/int32; data:BackupResult; message:string; error:string; success:boolean |
| `ResultBoolean` | object | - | code:integer/int32; data:boolean; message:string; error:string; success:boolean |
| `ResultCaptchaVO` | object | - | code:integer/int32; data:CaptchaVO; message:string; error:string; success:boolean |
| `ResultCaseView` | object | - | code:integer/int32; data:CaseView; message:string; error:string; success:boolean |
| `ResultChapterResponse` | object | - | code:integer/int32; data:ChapterResponse; message:string; error:string; success:boolean |
| `ResultCourseResponse` | object | - | code:integer/int32; data:CourseResponse; message:string; error:string; success:boolean |
| `ResultDemoChatResponse` | object | - | code:integer/int32; data:DemoChatResponse; message:string; error:string; success:boolean |
| `ResultDictVO` | object | - | code:integer/int32; data:DictVO; message:string; error:string; success:boolean |
| `ResultDifficultyScaleView` | object | - | code:integer/int32; data:DifficultyScaleView; message:string; error:string; success:boolean |
| `ResultDocumentView` | object | - | code:integer/int32; data:DocumentView; message:string; error:string; success:boolean |
| `ResultExamResponse` | object | - | code:integer/int32; data:ExamResponse; message:string; error:string; success:boolean |
| `ResultFileView` | object | - | code:integer/int32; data:FileView; message:string; error:string; success:boolean |
| `ResultGraphValidationVO` | object | - | code:integer/int32; data:GraphValidationVO; message:string; error:string; success:boolean |
| `ResultHybridRecommendResponse` | object | - | code:integer/int32; data:HybridRecommendResponse; message:string; error:string; success:boolean |
| `ResultIntentDefVO` | object | - | code:integer/int32; data:IntentDefVO; message:string; error:string; success:boolean |
| `ResultJobView` | object | - | code:integer/int32; data:JobView; message:string; error:string; success:boolean |
| `ResultKnowledgeGraphResponse` | object | - | code:integer/int32; data:KnowledgeGraphResponse; message:string; error:string; success:boolean |
| `ResultListAgentDefinition` | object | - | code:integer/int32; data:array<AgentDefinition>; message:string; error:string; success:boolean |
| `ResultListAgentVersionVO` | object | - | code:integer/int32; data:array<AgentVersionVO>; message:string; error:string; success:boolean |
| `ResultListAiModelResponse` | object | - | code:integer/int32; data:array<AiModelResponse>; message:string; error:string; success:boolean |
| `ResultListAiModelVO` | object | - | code:integer/int32; data:array<AiModelVO>; message:string; error:string; success:boolean |
| `ResultListAiPlatformVO` | object | - | code:integer/int32; data:array<AiPlatformVO>; message:string; error:string; success:boolean |
| `ResultListAuthCodeOptionVO` | object | - | code:integer/int32; data:array<AuthCodeOptionVO>; message:string; error:string; success:boolean |
| `ResultListChapterResponse` | object | - | code:integer/int32; data:array<ChapterResponse>; message:string; error:string; success:boolean |
| `ResultListCourseResponse` | object | - | code:integer/int32; data:array<CourseResponse>; message:string; error:string; success:boolean |
| `ResultListDailyTaskResponse` | object | - | code:integer/int32; data:array<DailyTaskResponse>; message:string; error:string; success:boolean |
| `ResultListDictVO` | object | - | code:integer/int32; data:array<DictVO>; message:string; error:string; success:boolean |
| `ResultListDocumentRelationView` | object | - | code:integer/int32; data:array<DocumentRelationView>; message:string; error:string; success:boolean |
| `ResultListDocumentSummary` | object | - | code:integer/int32; data:array<DocumentSummary>; message:string; error:string; success:boolean |
| `ResultListExamResponse` | object | - | code:integer/int32; data:array<ExamResponse>; message:string; error:string; success:boolean |
| `ResultListFileView` | object | - | code:integer/int32; data:array<FileView>; message:string; error:string; success:boolean |
| `ResultListIdNameOptionVO` | object | - | code:integer/int32; data:array<IdNameOptionVO>; message:string; error:string; success:boolean |
| `ResultListKnowledgeRecommendResponse` | object | - | code:integer/int32; data:array<KnowledgeRecommendResponse>; message:string; error:string; success:boolean |
| `ResultListLong` | object | - | code:integer/int32; data:array<integer/int64>; message:string; error:string; success:boolean |
| `ResultListMapStringObject` | object | - | code:integer/int32; data:array<object>; message:string; error:string; success:boolean |
| `ResultListMcpToolDescriptor` | object | - | code:integer/int32; data:array<McpToolDescriptor>; message:string; error:string; success:boolean |
| `ResultListMemberView` | object | - | code:integer/int32; data:array<MemberView>; message:string; error:string; success:boolean |
| `ResultListMenuVO` | object | - | code:integer/int32; data:array<MenuVO>; message:string; error:string; success:boolean |
| `ResultListNodeTypeMetaVO` | object | - | code:integer/int32; data:array<NodeTypeMetaVO>; message:string; error:string; success:boolean |
| `ResultListPluginInfoVO` | object | - | code:integer/int32; data:array<PluginInfoVO>; message:string; error:string; success:boolean |
| `ResultListQuestionRecommendResponse` | object | - | code:integer/int32; data:array<QuestionRecommendResponse>; message:string; error:string; success:boolean |
| `ResultListQuestionResponse` | object | - | code:integer/int32; data:array<QuestionResponse>; message:string; error:string; success:boolean |
| `ResultListRelationView` | object | - | code:integer/int32; data:array<RelationView>; message:string; error:string; success:boolean |
| `ResultListResourceRecommendResponse` | object | - | code:integer/int32; data:array<ResourceRecommendResponse>; message:string; error:string; success:boolean |
| `ResultListResourceResponse` | object | - | code:integer/int32; data:array<ResourceResponse>; message:string; error:string; success:boolean |
| `ResultListReviewTaskResponse` | object | - | code:integer/int32; data:array<ReviewTaskResponse>; message:string; error:string; success:boolean |
| `ResultListRoleVO` | object | - | code:integer/int32; data:array<RoleVO>; message:string; error:string; success:boolean |
| `ResultListRouteMenuVO` | object | - | code:integer/int32; data:array<RouteMenuVO>; message:string; error:string; success:boolean |
| `ResultListSpaceView` | object | - | code:integer/int32; data:array<SpaceView>; message:string; error:string; success:boolean |
| `ResultListString` | object | - | code:integer/int32; data:array<string>; message:string; error:string; success:boolean |
| `ResultListStudyPlanResponse` | object | - | code:integer/int32; data:array<StudyPlanResponse>; message:string; error:string; success:boolean |
| `ResultListStudyRecordResponse` | object | - | code:integer/int32; data:array<StudyRecordResponse>; message:string; error:string; success:boolean |
| `ResultListSubjectResponse` | object | - | code:integer/int32; data:array<SubjectResponse>; message:string; error:string; success:boolean |
| `ResultListTagVO` | object | - | code:integer/int32; data:array<TagVO>; message:string; error:string; success:boolean |
| `ResultListTenantInfoVO` | object | - | code:integer/int32; data:array<TenantInfoVO>; message:string; error:string; success:boolean |
| `ResultListTenantVO` | object | - | code:integer/int32; data:array<TenantVO>; message:string; error:string; success:boolean |
| `ResultListTextbookResponse` | object | - | code:integer/int32; data:array<TextbookResponse>; message:string; error:string; success:boolean |
| `ResultListTimelineEventVO` | object | - | code:integer/int32; data:array<TimelineEventVO>; message:string; error:string; success:boolean |
| `ResultListTimezoneOptionVO` | object | - | code:integer/int32; data:array<TimezoneOptionVO>; message:string; error:string; success:boolean |
| `ResultListUserTenantAssignmentVO` | object | - | code:integer/int32; data:array<UserTenantAssignmentVO>; message:string; error:string; success:boolean |
| `ResultListVersionView` | object | - | code:integer/int32; data:array<VersionView>; message:string; error:string; success:boolean |
| `ResultListWeakPointResponse` | object | - | code:integer/int32; data:array<WeakPointResponse>; message:string; error:string; success:boolean |
| `ResultListWrongQuestionResponse` | object | - | code:integer/int32; data:array<WrongQuestionResponse>; message:string; error:string; success:boolean |
| `ResultLoginResponseVO` | object | - | code:integer/int32; data:LoginResponseVO; message:string; error:string; success:boolean |
| `ResultLong` | object | - | code:integer/int32; data:integer/int64; message:string; error:string; success:boolean |
| `ResultMapStringObject` | object | - | code:integer/int32; data:object; message:string; error:string; success:boolean |
| `ResultMapStringString` | object | - | code:integer/int32; data:object; message:string; error:string; success:boolean |
| `ResultMcpToolDescriptor` | object | - | code:integer/int32; data:McpToolDescriptor; message:string; error:string; success:boolean |
| `ResultMediaVO` | object | - | code:integer/int32; data:MediaVO; message:string; error:string; success:boolean |
| `ResultNodeTypeMetaVO` | object | - | code:integer/int32; data:NodeTypeMetaVO; message:string; error:string; success:boolean |
| `ResultObject` | object | - | code:integer/int32; data:-; message:string; error:string; success:boolean |
| `ResultOverviewResponse` | object | - | code:integer/int32; data:OverviewResponse; message:string; error:string; success:boolean |
| `ResultPageDataAgentVO` | object | - | code:integer/int32; data:PageDataAgentVO; message:string; error:string; success:boolean |
| `ResultPageDataAiModelVO` | object | - | code:integer/int32; data:PageDataAiModelVO; message:string; error:string; success:boolean |
| `ResultPageDataAiPlatformVO` | object | - | code:integer/int32; data:PageDataAiPlatformVO; message:string; error:string; success:boolean |
| `ResultPageDataAuthCodeOptionVO` | object | - | code:integer/int32; data:PageDataAuthCodeOptionVO; message:string; error:string; success:boolean |
| `ResultPageDataCaseView` | object | - | code:integer/int32; data:PageDataCaseView; message:string; error:string; success:boolean |
| `ResultPageDataCourseResponse` | object | - | code:integer/int32; data:PageDataCourseResponse; message:string; error:string; success:boolean |
| `ResultPageDataDictVO` | object | - | code:integer/int32; data:PageDataDictVO; message:string; error:string; success:boolean |
| `ResultPageDataDocumentView` | object | - | code:integer/int32; data:PageDataDocumentView; message:string; error:string; success:boolean |
| `ResultPageDataExamResponse` | object | - | code:integer/int32; data:PageDataExamResponse; message:string; error:string; success:boolean |
| `ResultPageDataIntentDefVO` | object | - | code:integer/int32; data:PageDataIntentDefVO; message:string; error:string; success:boolean |
| `ResultPageDataJobView` | object | - | code:integer/int32; data:PageDataJobView; message:string; error:string; success:boolean |
| `ResultPageDataKnowledgeAuditResponse` | object | - | code:integer/int32; data:PageDataKnowledgeAuditResponse; message:string; error:string; success:boolean |
| `ResultPageDataMediaVO` | object | - | code:integer/int32; data:PageDataMediaVO; message:string; error:string; success:boolean |
| `ResultPageDataMenuVO` | object | - | code:integer/int32; data:PageDataMenuVO; message:string; error:string; success:boolean |
| `ResultPageDataPointView` | object | - | code:integer/int32; data:PageDataPointView; message:string; error:string; success:boolean |
| `ResultPageDataProfileVO` | object | - | code:integer/int32; data:PageDataProfileVO; message:string; error:string; success:boolean |
| `ResultPageDataQuestionResponse` | object | - | code:integer/int32; data:PageDataQuestionResponse; message:string; error:string; success:boolean |
| `ResultPageDataRecordVO` | object | - | code:integer/int32; data:PageDataRecordVO; message:string; error:string; success:boolean |
| `ResultPageDataResourceResponse` | object | - | code:integer/int32; data:PageDataResourceResponse; message:string; error:string; success:boolean |
| `ResultPageDataRoleVO` | object | - | code:integer/int32; data:PageDataRoleVO; message:string; error:string; success:boolean |
| `ResultPageDataSpaceView` | object | - | code:integer/int32; data:PageDataSpaceView; message:string; error:string; success:boolean |
| `ResultPageDataStudentResponse` | object | - | code:integer/int32; data:PageDataStudentResponse; message:string; error:string; success:boolean |
| `ResultPageDataSubjectResponse` | object | - | code:integer/int32; data:PageDataSubjectResponse; message:string; error:string; success:boolean |
| `ResultPageDataTagVO` | object | - | code:integer/int32; data:PageDataTagVO; message:string; error:string; success:boolean |
| `ResultPageDataTenantVO` | object | - | code:integer/int32; data:PageDataTenantVO; message:string; error:string; success:boolean |
| `ResultPageDataTextbookResponse` | object | - | code:integer/int32; data:PageDataTextbookResponse; message:string; error:string; success:boolean |
| `ResultPageDataTimelineEventVO` | object | - | code:integer/int32; data:PageDataTimelineEventVO; message:string; error:string; success:boolean |
| `ResultPageDataUserVO` | object | - | code:integer/int32; data:PageDataUserVO; message:string; error:string; success:boolean |
| `ResultPointView` | object | - | code:integer/int32; data:PointView; message:string; error:string; success:boolean |
| `ResultProfileVO` | object | - | code:integer/int32; data:ProfileVO; message:string; error:string; success:boolean |
| `ResultQuestionResponse` | object | - | code:integer/int32; data:QuestionResponse; message:string; error:string; success:boolean |
| `ResultRecordVO` | object | - | code:integer/int32; data:RecordVO; message:string; error:string; success:boolean |
| `ResultResourceResponse` | object | - | code:integer/int32; data:ResourceResponse; message:string; error:string; success:boolean |
| `ResultRestoreCheckResult` | object | - | code:integer/int32; data:RestoreCheckResult; message:string; error:string; success:boolean |
| `ResultReviewTaskResponse` | object | - | code:integer/int32; data:ReviewTaskResponse; message:string; error:string; success:boolean |
| `ResultRoleVO` | object | - | code:integer/int32; data:RoleVO; message:string; error:string; success:boolean |
| `ResultRunResult` | object | - | code:integer/int32; data:RunResult; message:string; error:string; success:boolean |
| `ResultSearchResponse` | object | - | code:integer/int32; data:SearchResponse; message:string; error:string; success:boolean |
| `ResultSetString` | object | - | code:integer/int32; data:array<string>; message:string; error:string; success:boolean |
| `ResultSpaceView` | object | - | code:integer/int32; data:SpaceView; message:string; error:string; success:boolean |
| `ResultString` | object | - | code:integer/int32; data:string; message:string; error:string; success:boolean |
| `ResultStudentResponse` | object | - | code:integer/int32; data:StudentResponse; message:string; error:string; success:boolean |
| `ResultStudyPlanResponse` | object | - | code:integer/int32; data:StudyPlanResponse; message:string; error:string; success:boolean |
| `ResultStudyRecordResponse` | object | - | code:integer/int32; data:StudyRecordResponse; message:string; error:string; success:boolean |
| `ResultSubjectResponse` | object | - | code:integer/int32; data:SubjectResponse; message:string; error:string; success:boolean |
| `ResultSwitchContextResponse` | object | - | code:integer/int32; data:SwitchContextResponse; message:string; error:string; success:boolean |
| `ResultTagVO` | object | - | code:integer/int32; data:TagVO; message:string; error:string; success:boolean |
| `ResultTenantVO` | object | - | code:integer/int32; data:TenantVO; message:string; error:string; success:boolean |
| `ResultTextbookResponse` | object | - | code:integer/int32; data:TextbookResponse; message:string; error:string; success:boolean |
| `ResultTimelineEventVO` | object | - | code:integer/int32; data:TimelineEventVO; message:string; error:string; success:boolean |
| `ResultTrendResponse` | object | - | code:integer/int32; data:TrendResponse; message:string; error:string; success:boolean |
| `ResultUploadResult` | object | - | code:integer/int32; data:UploadResult; message:string; error:string; success:boolean |
| `ResultUploadSession` | object | - | code:integer/int32; data:UploadSession; message:string; error:string; success:boolean |
| `ResultUserContextVO` | object | - | code:integer/int32; data:UserContextVO; message:string; error:string; success:boolean |
| `ResultUserVO` | object | - | code:integer/int32; data:UserVO; message:string; error:string; success:boolean |
| `ResultValidateCaptchaResponse` | object | - | code:integer/int32; data:ValidateCaptchaResponse; message:string; error:string; success:boolean |
| `ResultVoid` | object | - | code:integer/int32; data:-; message:string; error:string; success:boolean |
| `ResultWrongQuestionResponse` | object | - | code:integer/int32; data:WrongQuestionResponse; message:string; error:string; success:boolean |
| `ReviewRequest` | object | knowledgeId, studentId | id:integer/int64; studentId*:integer/int64; knowledgeId*:integer/int64; status:integer/int32; reviewDate:string/date; reviewRound:integer/int32; resultScore:number/double; completedAt:string/date-time |
| `ReviewTaskResponse` | object | - | id:integer/int64; studentId:integer/int64; knowledgeId:integer/int64; knowledgeName:string; reviewRound:integer/int32; reviewDate:string; status:integer/int32; statusDesc:string; resultScore:number/double; completedAt:string/date-time |
| `RolePageRequest` | object | - | pageSize:integer/int32; pageNum:integer/int32; orderByColumn:string; isAsc:string; orderFields:array<OrderField>; filterConditions:array<FilterCondition>; name:string |
| `RoleRequest` | object | code, name | code*:string; tenantId:integer/int64; name*:string; status:string; remark:string; permissions:array<integer/int64> |
| `RoleVO` | object | - | id:integer/int64; tenantId:integer/int64; code:string; name:string; status:integer/int32; remark:string; delFlag:integer/int32; createTime:string/date-time; updateTime:string/date-time; permissions:array<integer/int64> |
| `RouteMenuVO` | object | - | id:integer/int64; pid:integer/int64; name:string; path:string; component:string; type:string; status:integer/int32; icon:string; redirect:string; meta:MetaVO; children:array<RouteMenuVO> |
| `RunRequest` | object | - | spaceId:integer/int64; topK:integer/int32 |
| `RunResult` | object | - | spaceId:integer/int64; caseCount:integer/int32; topK:integer/int32; recallAtK:number/double; mrr:number/double; citationAccuracy:number/double; cases:array<CaseResult> |
| `SearchRequest` | object | query, spaceId | spaceId*:integer/int64; query*:string; mode:string; topK:integer/int32; threshold:number/double; rerank:boolean |
| `SearchResponse` | object | - | spaceId:integer/int64; mode:string; hits:array<HybridHit> |
| `SetTimezoneRequest` | object | timezone | timezone*:string |
| `SpaceView` | object | - | id:integer/int64; code:string; domainCode:string; name:string; description:string; accessMode:string; reviewMode:string; bindingMode:string; difficultyScaleId:integer/int64; embeddingProfile:string; rerankProfile:string; chunkStrategy:string; chunkSize:integer/int32; chunkOverlap:integer/int32; activeIndexVersion:integer/int64; status:integer/int32; createTime:string/date-time; updateTime:string/date-time |
| `StateGraphAgentState` | object | - | channels:object; stateSerializer:StateSerializerAgentState; stateFactory:AgentStateFactoryAgentState |
| `StateSerializerAgentState` | - | - | - |
| `StudentRequest` | object | grade, name, userId | id:integer/int64; name*:string; userId*:integer/int64; studentNo:string; grade*:integer/int32; gradeLevel:string; school:string; className:string; status:integer/int32 |
| `StudentResponse` | object | - | id:integer/int64; userId:integer/int64; studentNo:string; name:string; gender:integer/int32; grade:integer/int32; gradeLevel:string; school:string; className:string |
| `StudyPlanRequest` | object | studentId | id:integer/int64; studentId*:integer/int64; name:string; startDate:string/date; endDate:string/date; status:string |
| `StudyPlanResponse` | object | - | id:integer/int64; studentId:integer/int64; name:string; startDate:string; endDate:string; status:integer/int32; statusDesc:string; totalItems:integer/int32; completedItems:integer/int32; items:array<DailyTaskResponse> |
| `StudyRecordRequest` | object | knowledgeId, recordType, studentId | id:integer/int64; studentId*:integer/int64; knowledgeId*:integer/int64; recordType*:string; questionId:integer/int64; score:number/double; accuracy:number/double; durationSec:integer/int32 |
| `StudyRecordResponse` | object | - | id:integer/int64; studentId:integer/int64; knowledgeId:integer/int64; recordType:string; questionId:integer/int64; score:number/double; accuracy:number/double; durationSec:integer/int32; createTime:string/date-time |
| `SubjectRequest` | object | code, name | id:integer/int64; code*:string; name*:string; gradeLevel:string; description:string; icon:string; sortOrder:integer/int32; status:integer/int32 |
| `SubjectResponse` | object | - | id:integer/int64; code:string; name:string; gradeLevel:string; icon:string; sortOrder:integer/int32 |
| `SwitchContextResponse` | object | - | userInfo:UserVO; tenants:array<TenantInfoVO> |
| `SwitchRoleRequest` | object | roleId | roleId*:integer/int64 |
| `SwitchTenantRequest` | object | tenantId | tenantId*:integer/int64 |
| `TagRequest` | object | name | name*:string |
| `TagVO` | object | - | id:integer/int64; name:string; createTime:string/date-time |
| `TenantContextVO` | object | - | tenantId:integer/int64; tenantName:string; roleCode:string |
| `TenantInfoVO` | object | - | id:integer/int64; code:string; name:string; pathName:string |
| `TenantPageRequest` | object | - | pageSize:integer/int32; pageNum:integer/int32; orderByColumn:string; isAsc:string; orderFields:array<OrderField>; filterConditions:array<FilterCondition>; name:string; code:string; status:integer/int32 |
| `TenantRequest` | object | code, name | parentId:integer/int64; code*:string; name*:string; contactName:string; contactPhone:string; address:string; domain:string; intro:string; order:integer/int32; leader:string; email:string; remark:string; status:string; menuIds:array<integer/int64>; authCodeIds:array<integer/int64>; adminRoleName:string; adminUsername:string; adminPassword:string |
| `TenantVO` | object | - | id:integer/int64; parentId:integer/int64; code:string; name:string; contactName:string; contactPhone:string; address:string; domain:string; intro:string; order:integer/int32; leader:string; phone:string; email:string; remark:string; status:integer/int32; createTime:string/date-time; updateTime:string/date-time; children:array<TenantVO> |
| `TextbookRequest` | object | grade, name, subjectCode | id:integer/int64; name*:string; subjectCode*:string; grade*:integer/int32; publisher:string; author:string; edition:string; isbn:string; status:integer/int32 |
| `TextbookResponse` | object | - | id:integer/int64; name:string; subjectCode:string; grade:integer/int32; publisher:string; isbn:string |
| `TimelineEventRequest` | object | profileId, title | profileId*:integer/int64; title*:string; eventTime:string/date-time; eventDate:string/date-time; eventType:string |
| `TimelineEventVO` | object | - | id:integer/int64; profileId:integer/int64; title:string; eventTime:string/date-time; type:string; visibility:string; createdBy:integer/int64; createTime:string/date-time; record:RecordVO; mediaList:array<MediaVO> |
| `TimezoneOptionVO` | object | - | label:string; value:string |
| `TrendResponse` | object | - | dates:array<string>; values:array<number/double> |
| `UpdatePointRequest` | object | - | name:string; description:string; difficultyLevel:integer/int32; category:string; tags:string |
| `UpdateSpaceRequest` | object | - | name:string; description:string; domainCode:string; accessMode:string; reviewMode:string; bindingMode:string; difficultyScaleId:integer/int64; embeddingProfile:string; rerankProfile:string; chunkStrategy:string; chunkSize:integer/int32; chunkOverlap:integer/int32; status:integer/int32 |
| `UploadResult` | object | - | document:DocumentView; versionId:integer/int64; jobId:integer/int64; duplicate:boolean |
| `UploadSession` | object | - | sessionId:string; spaceId:integer/int64; fileName:string; size:integer/int64; totalChunks:integer/int32; uploadedChunks:array<integer/int32>; chunkSize:integer/int32 |
| `UserContextVO` | object | - | userId:integer/int64; username:string; userType:string; token:string; loginTime:integer/int64; expireTime:integer/int64; ipaddr:string; loginLocation:string; browser:string; os:string; nickName:string; avatar:string; extInfo:string; isLogin:boolean; message:string; deviceInfo:string |
| `UserPageRequest` | object | - | pageSize:integer/int32; pageNum:integer/int32; orderByColumn:string; isAsc:string; orderFields:array<OrderField>; filterConditions:array<FilterCondition>; username:string |
| `UserRequest` | object | username | username*:string; password:string; nickName:string; email:string; phone:string; gender:string; avatar:string; address:string; status:string; remark:string; roleIds:array<integer/int64>; tenantId:integer/int64; postIds:array<integer/int64> |
| `UserTenantAssignmentVO` | object | - | tenantId:integer/int64; tenantName:string; roleId:integer/int64; roleName:string; roleCode:string |
| `UserTenantRoleRequest` | object | roleId, tenantId | tenantId*:integer/int64; roleId*:integer/int64 |
| `UserVO` | object | - | id:integer/int64; username:string; status:string; delFlag:integer/int32; createTime:string/date-time; updateTime:string/date-time; nickName:string; gender:string; avatar:string; address:string; email:string; phone:string; remark:string; roles:array<RoleVO>; roleIds:array<integer/int64>; currentRole:RoleVO; extInfo:string; tenants:array<TenantInfoVO>; subTenants:array<TenantContextVO>; currentTenantId:integer/int64; homeTenantId:integer/int64; switchMode:string |
| `ValidateCaptchaRequest` | object | - | key:string; code:string |
| `ValidateCaptchaResponse` | object | - | success:boolean; message:string |
| `VersionRequest` | object | versionNumber | versionNumber*:string; description:string; copyFromVersionId:integer/int64 |
| `VersionView` | object | - | id:integer/int64; documentId:integer/int64; spaceId:integer/int64; versionNo:integer/int32; title:string; lifecycleStatus:string; parseStatus:string; objectKey:string; mimeType:string; fileSize:integer/int64; checksum:string; modelProfile:string; publishedAt:string/date-time; createTime:string/date-time |
| `WeakPointResponse` | object | - | knowledgeId:integer/int64; knowledgeName:string; mastery:number/double |
| `WrongQuestionRequest` | object | questionId, studentId | id:integer/int64; studentId*:integer/int64; questionId*:integer/int64; knowledgeId:integer/int64; studentAnswer:string; correctTimes:integer/int32; status:integer/int32 |
| `WrongQuestionResponse` | object | - | id:integer/int64; studentId:integer/int64; questionId:integer/int64; knowledgeId:integer/int64; questionTitle:string; studentAnswer:string; correctAnswer:string; correctTimes:integer/int32 |

## 维护与验证

```powershell
python scripts/docs/generate_reference_docs.py --openapi-url http://127.0.0.1:9000/v3/api-docs
```

生成后应运行前端 `pnpm run test:contract`，确保前端方法与路径仍被该 OpenAPI 契约覆盖。

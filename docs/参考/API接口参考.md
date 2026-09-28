# API 接口参考

> 本文档由 `scripts/docs/generate_reference_docs.py` 从 SpringDoc OpenAPI 自动生成。
> 生成源：`../shiyu-ui/tests/contracts/shiyu-ai-openapi.json`；OpenAPI：`3.1.0`；服务版本：`0.1`。

## 契约约定

- 后端 Controller 原生保留 `/api/{domain}` 前缀；浏览器开发环境和生产网关原样转发该前缀，不移除或重复添加。
- 除登录、注册、验证码等公开入口外，请求使用 `Authorization: Bearer <accessToken>`。
- 普通 JSON 接口通常返回 `Result<T>`；流式接口按 OpenAPI 标注返回 SSE 或二进制内容。
- `requestBody` 与响应栏保留 OpenAPI schema 名称，具体字段见“组件模型”。

## 租户统计边界

- `/api/governance/usage/**` 只返回当前租户数据。
- `/api/governance/platform/usage/**` 要求 `platform:usage:read`，并由 IAM 校验归属租户和当前租户均为默认租户 ID 1、有效 super 角色、非委派状态。
- 平台统计无客户端 allTenants 开关；拒绝访问返回权限错误。

## 接口清单

### Agent Definition

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/agent/agents` | Get Page | 登录态；细粒度权限见权限矩阵 | pageNo[query,可选]:integer/int32; pageSize[query,可选]:integer/int32; name[query,可选]:string; status[query,可选]:integer/int32 | 200=*/*:ResultPageDataAgentVO |
| POST | `/api/agent/agents` | Create | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:AgentRequest | 200=*/*:ResultAgentVO |
| GET | `/api/agent/agents/definitions` | List Agents | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListAgentDefinition |
| DELETE | `/api/agent/agents/definitions/{agentId}` | Delete Agent | 登录态；细粒度权限见权限矩阵 | agentId[path,必填]:string | 200=*/*:ResultVoid |
| GET | `/api/agent/agents/definitions/{agentId}` | Get Agent | 登录态；细粒度权限见权限矩阵 | agentId[path,必填]:string | 200=*/*:ResultAgentDefinition |
| GET | `/api/agent/agents/node-types` | Get Node Types | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListNodeTypeMetaVO |
| GET | `/api/agent/agents/node-types/detail` | Get Node Type | 登录态；细粒度权限见权限矩阵 | nodeType[query,必填]:string | 200=*/*:ResultNodeTypeMetaVO |
| GET | `/api/agent/agents/options` | List All Options | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListIdNameOptionVO |
| POST | `/api/agent/agents/register` | Register Agent | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:RegisterAgentRequest | 200=*/*:ResultMapStringObject |
| POST | `/api/agent/agents/status` | Update Status | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; status[query,必填]:integer/int32 | 200=*/*:ResultVoid |
| POST | `/api/agent/agents/version/switch` | Switch Version | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; version[query,必填]:string | 200=*/*:ResultVoid |
| DELETE | `/api/agent/agents/{id}` | Delete | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/agent/agents/{id}` | Get by Id | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultAgentDetailVO |
| PUT | `/api/agent/agents/{id}` | Update | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:AgentRequest | 200=*/*:ResultAgentVO |

### Agent Version

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/agent/versions/activate` | Activate | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| POST | `/api/agent/versions/archive` | Archive | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| POST | `/api/agent/versions/copy` | Copy | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; body[必填]=application/json:VersionRequest | 200=*/*:ResultAgentVersionVO |
| GET | `/api/agent/versions/graph/canvas` | Get Canvas | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64 | 200=*/*:ResultString |
| POST | `/api/agent/versions/graph/canvas-update` | Update Canvas | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64; body[必填]=application/json:string | 200=*/*:ResultVoid |
| GET | `/api/agent/versions/graph/detail` | Get Graph | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64 | 200=*/*:ResultAgentVersionDetailVO |
| POST | `/api/agent/versions/graph/edge/create` | Add Edge | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64; body[必填]=application/json:EdgeRequest | 200=*/*:ResultVoid |
| POST | `/api/agent/versions/graph/edge/delete` | Delete Edge | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64; sourceNodeId[query,必填]:string; targetNodeId[query,必填]:string | 200=*/*:ResultVoid |
| POST | `/api/agent/versions/graph/node/create` | Add Node | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64; body[必填]=application/json:NodeConfigRequest | 200=*/*:ResultVoid |
| POST | `/api/agent/versions/graph/node/delete` | Delete Node | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64; nodeId[query,必填]:string | 200=*/*:ResultVoid |
| POST | `/api/agent/versions/graph/node/update` | Update Node | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64; nodeId[query,必填]:string; body[必填]=application/json:NodeConfigRequest | 200=*/*:ResultVoid |
| POST | `/api/agent/versions/graph/update` | Update Graph | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64; body[必填]=application/json:GraphConfigRequest | 200=*/*:ResultAgentVersionDetailVO |
| POST | `/api/agent/versions/graph/validate` | Validate Graph | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64; body[必填]=application/json:GraphConfigRequest | 200=*/*:ResultGraphValidationVO |
| POST | `/api/agent/versions/publish` | Publish | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64 | 200=*/*:ResultVoid |

### Agent Version CRUD

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/agent/agents/{agentId}/versions` | List Agent Versions | 登录态；细粒度权限见权限矩阵 | agentId[path,必填]:string | 200=*/*:ResultListAgentVersionVO |
| POST | `/api/agent/agents/{agentId}/versions` | Create Agent Version | 登录态；细粒度权限见权限矩阵 | agentId[path,必填]:string; body[必填]=application/json:VersionRequest | 200=*/*:ResultAgentVersionVO |
| DELETE | `/api/agent/agents/{agentId}/versions/{versionId}` | Delete Agent Version | 登录态；细粒度权限见权限矩阵 | agentId[path,必填]:string; versionId[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/agent/agents/{agentId}/versions/{versionId}` | Get Agent Version | 登录态；细粒度权限见权限矩阵 | agentId[path,必填]:string; versionId[path,必填]:integer/int64 | 200=*/*:ResultAgentVersionDetailVO |
| PUT | `/api/agent/agents/{agentId}/versions/{versionId}` | Update Agent Version | 登录态；细粒度权限见权限矩阵 | agentId[path,必填]:string; versionId[path,必填]:integer/int64; body[必填]=application/json:VersionRequest | 200=*/*:ResultAgentVersionVO |

### Ai Model

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/model/model-configurations` | Get Page | 登录态；细粒度权限见权限矩阵 | platformId[query,可选]:integer/int64; pageNo[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataAiModelVO |
| POST | `/api/model/model-configurations` | Create | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:AiModelRequest | 200=*/*:ResultAiModelVO |
| POST | `/api/model/model-configurations/batch-delete` | Delete Batch | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:array<integer/int64> | 200=*/*:ResultVoid |
| GET | `/api/model/model-configurations/options` | Get Options | 登录态；细粒度权限见权限矩阵 | platformId[query,可选]:integer/int64 | 200=*/*:ResultListIdNameOptionVO |
| GET | `/api/model/model-configurations/platform` | Get by Platform Id | 登录态；细粒度权限见权限矩阵 | platformId[query,必填]:integer/int64 | 200=*/*:ResultListAiModelVO |
| GET | `/api/model/model-configurations/platform/by-code` | Get by Platform Code | 登录态；细粒度权限见权限矩阵 | platformCode[query,必填]:string | 200=*/*:ResultListAiModelResponse |
| GET | `/api/model/model-configurations/platform/default` | Get Default By Platform Id | 登录态；细粒度权限见权限矩阵 | platformId[query,必填]:integer/int64 | 200=*/*:ResultAiModelVO |
| POST | `/api/model/model-configurations/set-default` | Set Default | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultAiModelVO |
| DELETE | `/api/model/model-configurations/{id}` | Delete | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/model/model-configurations/{id}` | Get by Id | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultAiModelVO |
| PUT | `/api/model/model-configurations/{id}` | Update | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:AiModelRequest | 200=*/*:ResultAiModelVO |

### Ai Platform

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/model/platforms` | Get Page | 登录态；细粒度权限见权限矩阵 | name[query,可选]:string; code[query,可选]:string; pageNo[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataAiPlatformVO |
| POST | `/api/model/platforms` | Create | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:AiPlatformRequest | 200=*/*:ResultAiPlatformVO |
| GET | `/api/model/platforms/code` | Get by Code | 登录态；细粒度权限见权限矩阵 | code[query,必填]:string | 200=*/*:ResultAiPlatformResponse |
| GET | `/api/model/platforms/default` | Get Default | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultAiPlatformResponse |
| GET | `/api/model/platforms/enabled` | Get All Enabled | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListAiPlatformVO |
| GET | `/api/model/platforms/options` | Get Options | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListIdNameOptionVO |
| POST | `/api/model/platforms/reload` | Reload | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultVoid |
| POST | `/api/model/platforms/set-default` | Set Default | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultAiPlatformVO |
| DELETE | `/api/model/platforms/{id}` | Delete | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/model/platforms/{id}` | Get by Id | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultAiPlatformResponse |
| PUT | `/api/model/platforms/{id}` | Update | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:AiPlatformRequest | 200=*/*:ResultAiPlatformVO |

### Auth

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/iam/auth/code-login` | Code Login | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:CodeLoginRequest | 200=*/*:ResultLoginResponseVO |
| GET | `/api/iam/auth/codes` | Get Auth Codes | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListString |
| POST | `/api/iam/auth/current-role` | Switch Current Role | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:SwitchRoleRequest | 200=*/*:ResultSwitchContextResponse |
| POST | `/api/iam/auth/forget-password` | Forget Password | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ForgetPasswordRequest | 200=*/*:ResultBoolean |
| POST | `/api/iam/auth/login` | Login | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:LoginRequest | 200=*/*:ResultLoginResponseVO |
| POST | `/api/iam/auth/logout` | Logout | 登录态；细粒度权限见权限矩阵 | Authorization[header,可选]:string | 200=*/*:ResultString |
| POST | `/api/iam/auth/refresh` | Refresh Token | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:RefreshTokenRequest | 200=*/*:ResultString |
| POST | `/api/iam/auth/register` | Register | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:LoginRequest | 200=*/*:ResultLoginResponseVO |
| POST | `/api/iam/auth/switch-tenant` | Switch Tenant | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:SwitchTenantRequest | 200=*/*:ResultSwitchContextResponse |
| GET | `/api/iam/auth/tenants` | Get User Tenants | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListTenantInfoVO |

### Auth Code

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/iam/auth-codes` | Page Auth Codes | 登录态；细粒度权限见权限矩阵 | request[query,必填]:AuthCodePageRequest | 200=*/*:ResultPageDataAuthCodeOptionVO |
| POST | `/api/iam/auth-codes` | Create Auth Code | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:AuthCodeRequest | 200=*/*:ResultAuthCodeResponse |
| GET | `/api/iam/auth-codes/options` | Auth code options | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListAuthCodeOptionVO |
| POST | `/api/iam/auth-codes/roles/grant` | Grant role auth codes | 登录态；细粒度权限见权限矩阵 | roleId[query,必填]:integer/int64; tenantId[query,必填]:integer/int64; body[必填]=application/json:array<integer/int64> | 200=*/*:ResultVoid |
| GET | `/api/iam/auth-codes/roles/list` | List role auth codes | 登录态；细粒度权限见权限矩阵 | roleId[query,必填]:integer/int64; tenantId[query,必填]:integer/int64 | 200=*/*:ResultListString |
| POST | `/api/iam/auth-codes/roles/replace` | Replace role auth codes | 登录态；细粒度权限见权限矩阵 | roleId[query,必填]:integer/int64; tenantId[query,必填]:integer/int64; body[必填]=application/json:array<string> | 200=*/*:ResultVoid |
| POST | `/api/iam/auth-codes/roles/revoke` | Revoke role auth code | 登录态；细粒度权限见权限矩阵 | roleId[query,必填]:integer/int64; tenantId[query,必填]:integer/int64; authCodeId[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| DELETE | `/api/iam/auth-codes/{id}` | Delete Auth Code | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| PUT | `/api/iam/auth-codes/{id}` | Update Auth Code | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:AuthCodeRequest | 200=*/*:ResultVoid |

### Captcha

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/iam/auth/captcha` | Get Captcha | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultCaptchaVO |
| POST | `/api/iam/auth/captcha/validate` | Validate Captcha | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ValidateCaptchaRequest | 200=*/*:ResultValidateCaptchaResponse |

### Chat Product Assets

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/conversation/chat-products/characters` | characters | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListCharacterAsset |
| POST | `/api/conversation/chat-products/characters` | createCharacter | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:CharacterRequest | 200=*/*:ResultCharacterAsset |
| POST | `/api/conversation/chat-products/characters/import` | importCharacter | 登录态；细粒度权限见权限矩阵 | previewToken[query,必填]:string; body[可选]=multipart/form-data:object | 200=*/*:ResultCharacterAsset |
| POST | `/api/conversation/chat-products/characters/import/preview` | previewCharacterImport | 登录态；细粒度权限见权限矩阵 | body[可选]=multipart/form-data:object | 200=*/*:ResultPreview |
| DELETE | `/api/conversation/chat-products/characters/{id}` | deleteCharacter | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultVoid |
| GET | `/api/conversation/chat-products/characters/{id}` | character | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultCharacterAsset |
| GET | `/api/conversation/chat-products/characters/{id}/png` | exportCharacter | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=image/png:string/byte |
| GET | `/api/conversation/chat-products/groups` | groups | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListGroupChatAsset |
| POST | `/api/conversation/chat-products/groups` | createGroup | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:GroupRequest | 200=*/*:ResultGroupChatAsset |
| DELETE | `/api/conversation/chat-products/groups/{id}` | deleteGroup | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultVoid |
| GET | `/api/conversation/chat-products/groups/{id}` | group | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultGroupChatAsset |
| POST | `/api/conversation/chat-products/groups/{id}/next-speaker` | nextSpeaker | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; body[可选]=application/json:TurnRequest | 200=*/*:ResultTurnDecision |
| POST | `/api/conversation/chat-products/groups/{id}/turn` | runTurn | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; body[必填]=application/json:TurnRunRequest | 200=*/*:ResultGroupTurnRun |
| GET | `/api/conversation/chat-products/lorebooks` | lorebooks | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListLorebookAsset |
| POST | `/api/conversation/chat-products/lorebooks` | createLorebook | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:LorebookEntry | 200=*/*:ResultLorebookAsset |
| DELETE | `/api/conversation/chat-products/lorebooks/{id}` | deleteLorebook | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultVoid |
| GET | `/api/conversation/chat-products/lorebooks/{id}` | lorebook | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultLorebookAsset |
| GET | `/api/conversation/chat-products/personas` | personas | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListPersonaAsset |
| POST | `/api/conversation/chat-products/personas` | createPersona | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:Persona | 200=*/*:ResultPersonaAsset |
| DELETE | `/api/conversation/chat-products/personas/{id}` | deletePersona | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultVoid |
| GET | `/api/conversation/chat-products/personas/{id}` | persona | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultPersonaAsset |
| POST | `/api/conversation/chat-products/prompt-studio/preview` | preview_1 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:PromptPreviewRequest | 200=*/*:ResultPromptPreview |
| GET | `/api/conversation/chat-products/prompt-studio/templates` | prompts | 登录态；细粒度权限见权限矩阵 | templateId[query,可选]:string | 200=*/*:ResultListPromptTemplateVersion |
| POST | `/api/conversation/chat-products/prompt-studio/templates` | createPrompt | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:PromptRequest | 200=*/*:ResultPromptTemplateVersion |
| POST | `/api/conversation/chat-products/prompt-studio/templates/{templateId}/diff` | diffPrompt | 登录态；细粒度权限见权限矩阵 | templateId[path,必填]:string; body[必填]=application/json:DiffRequest | 200=*/*:ResultPromptDiff |
| POST | `/api/conversation/chat-products/prompt-studio/templates/{templateId}/publish` | publishPrompt | 登录态；细粒度权限见权限矩阵 | templateId[path,必填]:string; body[必填]=application/json:PublishRequest | 200=*/*:ResultPromptTemplateVersion |
| POST | `/api/conversation/chat-products/prompt-studio/templates/{templateId}/test` | testPrompt | 登录态；细粒度权限见权限矩阵 | templateId[path,必填]:string; body[必填]=application/json:PromptTestRequest | 200=*/*:ResultPromptTestRun |

### Conversation Platform

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/conversation/conversations` | list_11 | 登录态；细粒度权限见权限矩阵 | limit[query,可选]:integer/int32; offset[query,可选]:integer/int32 | 200=*/*:ResultListConversation |
| POST | `/api/conversation/conversations` | create_19 | 登录态；细粒度权限见权限矩阵 | Idempotency-Key[header,可选]:string; body[必填]=application/json:CreateConversationRequest | 200=*/*:ResultConversation |
| POST | `/api/conversation/conversations/import` | importConversation | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ImportRequest/text/plain:ImportRequest | 200=*/*:ResultConversation |
| POST | `/api/conversation/conversations/import/preview` | importPreview | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ImportRequest | 200=*/*:ResultPreview |
| DELETE | `/api/conversation/conversations/{id}` | delete_18 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultVoid |
| GET | `/api/conversation/conversations/{id}` | detail_1 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultConversation |
| PATCH | `/api/conversation/conversations/{id}` | update_18 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; body[必填]=application/json:UpdateConversationRequest | 200=*/*:ResultConversation |
| POST | `/api/conversation/conversations/{id}/active-leaf` | activeLeaf | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; messageId[query,可选]:string; body[可选]=application/json:ActiveLeafRequest | 200=*/*:ResultVoid |
| GET | `/api/conversation/conversations/{id}/branches` | branches | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultListConversation |
| POST | `/api/conversation/conversations/{id}/branches` | branch | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; messageId[query,必填]:string | 200=*/*:ResultConversation |
| GET | `/api/conversation/conversations/{id}/export` | export | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; format[query,可选]:string | 200=application/json:object |
| POST | `/api/conversation/conversations/{id}/generations` | generation | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; Idempotency-Key[header,可选]:string; body[必填]=application/json:MessageRequest | 200=*/*:ResultGenerationRun |
| GET | `/api/conversation/conversations/{id}/messages` | messages | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; limit[query,可选]:integer/int32 | 200=*/*:ResultListConversationMessage |
| POST | `/api/conversation/conversations/{id}/messages` | message | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; Idempotency-Key[header,可选]:string; body[必填]=application/json:MessageRequest | 200=*/*:ResultGenerationRun |
| GET | `/api/conversation/conversations/{id}/prompt-preview` | promptPreview | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultPromptPreview |

### Dict

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/iam/dicts` | Get Dict List | 登录态；细粒度权限见权限矩阵 | request[query,必填]:DictPageRequest | 200=*/*:ResultPageDataDictVO |
| POST | `/api/iam/dicts` | Create Dict | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:DictRequest | 200=*/*:ResultDictVO |
| POST | `/api/iam/dicts/batch-delete` | Delete Dicts | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:array<integer/int64> | 200=*/*:ResultVoid |
| GET | `/api/iam/dicts/type` | Get Dict By Type | 登录态；细粒度权限见权限矩阵 | dictType[query,必填]:string | 200=*/*:ResultListDictVO |
| DELETE | `/api/iam/dicts/{id}` | Delete Dict | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| PUT | `/api/iam/dicts/{id}` | Update Dict | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:DictRequest | 200=*/*:ResultDictVO |

### Execution

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/agent/executions/cancel` | Cancel Execution | 登录态；细粒度权限见权限矩阵 | executionId[query,必填]:string | 200=*/*:ResultVoid |
| GET | `/api/agent/executions/detail` | Get Execution Details | 登录态；细粒度权限见权限矩阵 | executionId[query,必填]:string | 200=*/*:ResultMapStringObject |
| POST | `/api/agent/executions/execute` | Execute Agent | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; body[可选]=application/json:object | 200=*/*:ResultMapStringObject |
| POST | `/api/agent/executions/execute-stream` | Execute Agent Stream | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; body[可选]=application/json:object | 200=text/event-stream:array<ResultMapStringObject> |
| GET | `/api/agent/executions/history` | Get Execution History | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; limit[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |
| POST | `/api/agent/executions/pause` | Pause Execution | 登录态；细粒度权限见权限矩阵 | executionId[query,必填]:string | 200=*/*:ResultVoid |
| POST | `/api/agent/executions/resume` | Resume Execution | 登录态；细粒度权限见权限矩阵 | executionId[query,必填]:string | 200=*/*:ResultMapStringObject |
| GET | `/api/agent/executions/status` | Get Execution Status | 登录态；细粒度权限见权限矩阵 | executionId[query,必填]:string | 200=*/*:ResultMapStringObject |

### File

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| DELETE | `/api/iam/files` | 删除文件 | 登录态；细粒度权限见权限矩阵 | key[query,必填]:string | 200=*/*:ResultBoolean |
| GET | `/api/iam/files` | 获取文件列表 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListFileView |
| GET | `/api/iam/files/config` | 获取文件存储配置 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultMapStringObject |
| GET | `/api/iam/files/download` | 下载文件 | 登录态；细粒度权限见权限矩阵 | key[query,必填]:string | 200=*/*:string/binary |
| POST | `/api/iam/files/upload` | 上传文件 | 登录态；细粒度权限见权限矩阵 | body[可选]=application/json:object | 200=*/*:ResultFileView |

### MAGMA Memory Platform

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/memory/admin/indexes/rebuild` | rebuild | 登录态；细粒度权限见权限矩阵 | namespace[query,必填]:string | 200=*/*:ResultVoid |
| POST | `/api/memory/events` | ingest | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:EventRequest | 200=*/*:ResultMemoryEvent |
| POST | `/api/memory/events/{id}/confirm` | confirm | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultVoid |
| GET | `/api/memory/events/{id}/relations` | relations | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; graphType[query,必填]:string(TEMPORAL,SEMANTIC,CAUSAL,ENTITY); limit[query,可选]:integer/int32 | 200=*/*:ResultListMemoryEdge |
| POST | `/api/memory/events/{id}/revoke` | revoke | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultVoid |
| POST | `/api/memory/events/{id}/supersede` | supersede | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; body[必填]=application/json:EventRequest | 200=*/*:ResultMemoryEvent |
| POST | `/api/memory/query` | query | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:QueryRequest | 200=*/*:ResultMemoryRetrievalResult |
| GET | `/api/memory/retrieval-traces/{traceId}` | trace | 登录态；细粒度权限见权限矩阵 | traceId[path,必填]:string | 200=*/*:ResultMemoryRetrievalTrace |

### MCP 工具市场

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/tooling/tools/mcp/categories` | 获取工具分类 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultSetString |
| GET | `/api/tooling/tools/mcp/stats` | 获取工具统计 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultMapStringObject |
| GET | `/api/tooling/tools/mcp/tools` | 列出所有工具 | 登录态；细粒度权限见权限矩阵 | category[query,可选]:string; tag[query,可选]:string; keyword[query,可选]:string | 200=*/*:ResultListMcpToolDescriptor |
| GET | `/api/tooling/tools/mcp/tools/detail` | 获取工具详情 | 登录态；细粒度权限见权限矩阵 | name[query,必填]:string | 200=*/*:ResultMcpToolDescriptor |
| POST | `/api/tooling/tools/mcp/tools/execute` | 执行工具 | 登录态；细粒度权限见权限矩阵 | name[query,必填]:string; body[可选]=application/json:object | 200=*/*:ResultObject |

### Menu

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/iam/menus` | Get System Menu Page | 登录态；细粒度权限见权限矩阵 | request[query,必填]:MenuPageRequest | 200=*/*:ResultPageDataMenuVO |
| POST | `/api/iam/menus` | Create Menu | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:MenuRequest | 200=*/*:ResultVoid |
| GET | `/api/iam/menus/all` | Get All Menus | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListRouteMenuVO |
| GET | `/api/iam/menus/children` | Get Menu Children | 登录态；细粒度权限见权限矩阵 | parentId[query,必填]:integer/int64 | 200=*/*:ResultListRouteMenuVO |
| GET | `/api/iam/menus/list` | Get System Menu List | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListMenuVO |
| GET | `/api/iam/menus/name-exists` | Is Menu Name Exists | 登录态；细粒度权限见权限矩阵 | name[query,必填]:string; id[query,可选]:integer/int64 | 200=*/*:ResultBoolean |
| GET | `/api/iam/menus/path-exists` | Is Menu Path Exists | 登录态；细粒度权限见权限矩阵 | path[query,必填]:string; id[query,可选]:integer/int64 | 200=*/*:ResultBoolean |
| GET | `/api/iam/menus/permissions` | Get Menu Permissions Tree | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListRouteMenuVO |
| GET | `/api/iam/menus/roots` | Get Menu Roots | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListRouteMenuVO |
| GET | `/api/iam/menus/tree` | Get All Tree | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListRouteMenuVO |
| DELETE | `/api/iam/menus/{id}` | Delete Menu | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| PUT | `/api/iam/menus/{id}` | Update Menu | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:MenuRequest | 200=*/*:ResultVoid |

### Platform Usage

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/governance/platform/usage/by-model` | 平台按模型用量 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListMapStringObject |
| GET | `/api/governance/platform/usage/daily` | 平台按日用量 | 登录态；细粒度权限见权限矩阵 | days[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |
| GET | `/api/governance/platform/usage/embedding/overview` | 平台 Embedding 用量概览 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultMapStringObject |
| GET | `/api/governance/platform/usage/llm/daily` | 平台 LLM 按日用量 | 登录态；细粒度权限见权限矩阵 | days[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |
| GET | `/api/governance/platform/usage/llm/monthly` | 平台 LLM 按月用量 | 登录态；细粒度权限见权限矩阵 | months[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |
| GET | `/api/governance/platform/usage/llm/weekly` | 平台 LLM 按周用量 | 登录态；细粒度权限见权限矩阵 | weeks[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |
| GET | `/api/governance/platform/usage/monthly` | 平台按月用量 | 登录态；细粒度权限见权限矩阵 | months[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |
| GET | `/api/governance/platform/usage/overview` | 平台用量概览 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultMapStringObject |
| GET | `/api/governance/platform/usage/weekly` | 平台按周用量 | 登录态；细粒度权限见权限矩阵 | weeks[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |

### Usage

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/governance/usage/by-model` | LLM 按模型聚合 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListMapStringObject |
| GET | `/api/governance/usage/daily` | 按日聚合（所有类型，按 usage_type 分组） | 登录态；细粒度权限见权限矩阵 | days[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |
| GET | `/api/governance/usage/embedding/overview` | Embedding 用量概览 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultMapStringObject |
| GET | `/api/governance/usage/llm/daily` | LLM 按日聚合（含 token/cost） | 登录态；细粒度权限见权限矩阵 | days[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |
| GET | `/api/governance/usage/llm/monthly` | LLM 按月聚合（含 token/cost） | 登录态；细粒度权限见权限矩阵 | months[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |
| GET | `/api/governance/usage/llm/weekly` | LLM 按周聚合（含 token/cost） | 登录态；细粒度权限见权限矩阵 | weeks[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |
| GET | `/api/governance/usage/monthly` | 按月聚合（所有类型，按 usage_type 分组） | 登录态；细粒度权限见权限矩阵 | months[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |
| GET | `/api/governance/usage/overview` | 用量概览（所有类型） | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultMapStringObject |
| GET | `/api/governance/usage/weekly` | 按周聚合（所有类型，按 usage_type 分组） | 登录态；细粒度权限见权限矩阵 | weeks[query,可选]:integer/int32 | 200=*/*:ResultListMapStringObject |

### ai-runtime-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/agent/apps` | apps | 登录态；细粒度权限见权限矩阵 | limit[query,可选]:integer/int32 | 200=*/*:ResultListAiApp |
| POST | `/api/agent/apps` | createApp | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:AppRequest | 200=*/*:ResultAiApp |
| POST | `/api/agent/apps/{id}/execute` | executeApp | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; body[必填]=application/json:AppExecutionRequest | 200=*/*:ResultMapStringObject |
| POST | `/api/agent/apps/{id}/preview` | preview_2 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; body[必填]=application/json:PreviewRequest | 200=*/*:ResultAiAppPreview |
| GET | `/api/agent/apps/{id}/versions` | versions | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultListAiAppVersion |
| POST | `/api/agent/apps/{id}/versions` | version | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; body[必填]=application/json:VersionRequest | 200=*/*:ResultAiAppVersion |
| POST | `/api/agent/apps/{id}/versions/{versionId}/archive` | archive_2 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; versionId[path,必填]:string | 200=*/*:ResultAiAppVersion |
| POST | `/api/agent/apps/{id}/versions/{versionId}/publish` | publish_4 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; versionId[path,必填]:string | 200=*/*:ResultAiAppVersion |
| GET | `/api/agent/generations/{generationId}/runtime-events` | generationEvents | 登录态；细粒度权限见权限矩阵 | generationId[path,必填]:string; afterSeq[query,可选]:integer/int64; follow[query,可选]:boolean; waitMs[query,可选]:integer/int32; Last-Event-ID[header,可选]:string | 200=text/event-stream:array<ServerSentEventAiRunEvent> |
| GET | `/api/agent/runs` | runs | 登录态；细粒度权限见权限矩阵 | limit[query,可选]:integer/int32 | 200=*/*:ResultListAiRun |
| POST | `/api/agent/runs` | startRun | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:RunRequest | 200=*/*:ResultAiRun |
| GET | `/api/agent/runs/{id}` | run_2 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultAiRun |
| POST | `/api/agent/runs/{id}/cancel` | cancel_2 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultAiRun |
| GET | `/api/agent/runs/{id}/event-history` | eventHistory | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; afterSeq[query,可选]:integer/int64; limit[query,可选]:integer/int32 | 200=application/json:ResultListAiRunEvent |
| GET | `/api/agent/runs/{id}/events` | events | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; afterSeq[query,可选]:integer/int64; limit[query,可选]:integer/int32; follow[query,可选]:boolean; waitMs[query,可选]:integer/int32; Last-Event-ID[header,可选]:string | 200=text/event-stream:array<ServerSentEventAiRunEvent> |
| GET | `/api/agent/runs/{id}/prompt-snapshot` | promptSnapshot | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultMapStringObject |
| GET | `/api/agent/runs/{id}/trajectory` | trajectory | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultListAiRunEvent |

### analytics-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/education/analytics/ability-radar` | getAbilityRadar | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64; knowledgeId[query,必填]:integer/int64 | 200=*/*:ResultAbilityRadarResponse |
| GET | `/api/education/analytics/overview` | getOverview_1 | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultOverviewResponse |
| POST | `/api/education/analytics/record-create` | createRecord | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:StudyRecordRequest | 200=*/*:ResultStudyRecordResponse |
| GET | `/api/education/analytics/records` | listRecordsByStudent | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListStudyRecordResponse |
| GET | `/api/education/analytics/records/knowledge` | listRecordsByStudentAndKnowledge | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64; knowledgeId[query,必填]:integer/int64 | 200=*/*:ResultListStudyRecordResponse |
| GET | `/api/education/analytics/trend` | getTrend | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultTrendResponse |
| GET | `/api/education/analytics/weak-points` | getWeakPoints | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListWeakPointResponse |

### chapter-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/education/chapter` | create_17 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ChapterRequest | 200=*/*:ResultChapterResponse |
| GET | `/api/education/chapter/children` | listByParentId | 登录态；细粒度权限见权限矩阵 | parentId[query,必填]:integer/int64 | 200=*/*:ResultListChapterResponse |
| POST | `/api/education/chapter/knowledge/bind` | replaceKnowledgeIds | 登录态；细粒度权限见权限矩阵 | chapterId[query,必填]:integer/int64; body[必填]=application/json:array<integer/int64> | 200=*/*:ResultVoid |
| GET | `/api/education/chapter/knowledge/list` | listKnowledgeIds | 登录态；细粒度权限见权限矩阵 | chapterId[query,必填]:integer/int64 | 200=*/*:ResultListLong |
| GET | `/api/education/chapter/textbook` | listByTextbookId | 登录态；细粒度权限见权限矩阵 | textbookId[query,必填]:integer/int64 | 200=*/*:ResultListChapterResponse |
| GET | `/api/education/chapter/tree` | getChapterTree | 登录态；细粒度权限见权限矩阵 | textbookId[query,必填]:integer/int64 | 200=*/*:ResultListChapterResponse |
| DELETE | `/api/education/chapter/{id}` | delete_15 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/chapter/{id}` | getById_12 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultChapterResponse |
| PUT | `/api/education/chapter/{id}` | update_15 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:ChapterRequest | 200=*/*:ResultVoid |

### config-console-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/console/api/config` | describe | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ConfigResponse |
| POST | `/console/api/config` | save | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ConfigChangeSet | 200=*/*:ConfigApplyResult |
| POST | `/console/api/config/restore-last-applied` | restore | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:RestoreRequest | 200=*/*:ConfigApplyResult |
| POST | `/console/api/config/validate` | validate | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ConfigChangeSet | 200=*/*:ValidationResponse |

### console-session-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/console/api/session` | status | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:SessionResponse |
| POST | `/console/api/session/exchange` | exchange | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ExchangeRequest | 200=*/*:SessionResponse |
| POST | `/console/api/session/logout` | logout | 登录态；细粒度权限见权限矩阵 | - | 200=OK |

### course-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/education/course` | list_9 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataCourseResponse |
| POST | `/api/education/course` | create_16 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:CourseRequest | 200=*/*:ResultCourseResponse |
| GET | `/api/education/course/grade` | listByGrade | 登录态；细粒度权限见权限矩阵 | grade[query,必填]:integer/int32 | 200=*/*:ResultListCourseResponse |
| POST | `/api/education/course/learn` | startLearning | 登录态；细粒度权限见权限矩阵 | courseId[query,必填]:integer/int64; studentId[query,必填]:integer/int64 | 200=*/*:ResultCourseResponse |
| GET | `/api/education/course/subject` | listBySubjectCode_2 | 登录态；细粒度权限见权限矩阵 | subjectCode[query,必填]:string | 200=*/*:ResultListCourseResponse |
| DELETE | `/api/education/course/{id}` | delete_14 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/course/{id}` | getById_11 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultCourseResponse |
| PUT | `/api/education/course/{id}` | update_14 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:CourseRequest | 200=*/*:ResultVoid |

### education-resource-content-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/education/education-resources/{fileName}` | open | 登录态；细粒度权限见权限矩阵 | fileName[path,必填]:string | 200=*/*:string/binary |

### evaluation-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/agent/evaluations/datasets` | create_21 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:DatasetRequest | 200=*/*:ResultEvalDataset |
| GET | `/api/agent/evaluations/datasets/{id}/cases` | cases | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultListEvalCase |
| POST | `/api/agent/evaluations/datasets/{id}/cases` | addCase | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; body[必填]=application/json:CaseRequest | 200=*/*:ResultEvalCase |
| POST | `/api/agent/evaluations/runs` | run_1 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:RunRequest | 200=*/*:ResultEvalRun |
| GET | `/api/agent/evaluations/runs/{id}` | detail_2 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultEvalRun |
| GET | `/api/agent/evaluations/runs/{id}/results` | results | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultListEvalResult |

### exam-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/education/exam` | list_8 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataExamResponse |
| POST | `/api/education/exam` | create_15 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ExamRequest | 200=*/*:ResultExamResponse |
| GET | `/api/education/exam/subject` | listBySubjectCode_1 | 登录态；细粒度权限见权限矩阵 | subjectCode[query,必填]:string | 200=*/*:ResultListExamResponse |
| GET | `/api/education/exam/teacher` | listByTeacherId | 登录态；细粒度权限见权限矩阵 | teacherId[query,必填]:integer/int64 | 200=*/*:ResultListExamResponse |
| DELETE | `/api/education/exam/{id}` | delete_13 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/exam/{id}` | getById_10 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultExamResponse |
| PUT | `/api/education/exam/{id}` | update_13 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:ExamRequest | 200=*/*:ResultVoid |

### generation-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/conversation/generations/{id}/cancel` | cancel_1 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultVoid |
| GET | `/api/conversation/generations/{id}/events` | stream | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; afterSeq[query,可选]:integer/int32; follow[query,可选]:boolean; waitMs[query,可选]:integer/int32; Last-Event-ID[header,可选]:string | 200=text/event-stream:array<ServerSentEventGenerationEvent> |

### intent-def-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| DELETE | `/api/agent/intents` | deleteBatch_1 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:array<integer/int64> | 200=*/*:ResultVoid |
| GET | `/api/agent/intents` | page_5 | 登录态；细粒度权限见权限矩阵 | agentId[query,可选]:string; name[query,可选]:string; code[query,可选]:string; category[query,可选]:string; pageNo[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataIntentDefVO |
| POST | `/api/agent/intents` | create_20 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:IntentDefRequest | 200=*/*:ResultIntentDefVO |
| GET | `/api/agent/intents/options` | options_2 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListIdNameOptionVO |
| DELETE | `/api/agent/intents/{id}` | delete_16 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/agent/intents/{id}` | detail | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultIntentDefVO |
| PUT | `/api/agent/intents/{id}` | update_16 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:IntentDefRequest | 200=*/*:ResultIntentDefVO |

### media-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/model/media/image/generate` | generate | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:GenerateRequest | 200=*/*:ResultImageResult |
| POST | `/api/model/media/image/understand` | understand | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ImageRequest | 200=*/*:ResultVisionResult |
| POST | `/api/model/media/translate` | translate | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:TranslateRequest | 200=*/*:ResultString |
| POST | `/api/model/media/tts` | tts | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:TtsRequest | 200=*/*:ResultMapStringString |

### message-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/conversation/messages/{messageId}/edits` | edit | 登录态；细粒度权限见权限矩阵 | messageId[path,必填]:string; Idempotency-Key[header,可选]:string; body[必填]=application/json:EditRequest | 200=*/*:ResultConversationMessage |
| POST | `/api/conversation/messages/{messageId}/generations` | retry_1 | 登录态；细粒度权限见权限矩阵 | messageId[path,必填]:string; Idempotency-Key[header,可选]:string; body[可选]=application/json:RetryRequest | 200=*/*:ResultGenerationRun |

### model-gateway-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/model/providers` | models | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListModelProviderCapabilities |
| GET | `/api/model/providers/{id}/health` | healthById | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultProviderHealth |
| GET | `/api/model/providers/{provider}/{model}/health` | health | 登录态；细粒度权限见权限矩阵 | provider[path,必填]:string; model[path,必填]:string | 200=*/*:ResultProviderHealth |
| GET | `/api/model/routes` | routes | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListModelRoutePolicy |
| POST | `/api/model/routes` | save_1 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:RouteRequest | 200=*/*:ResultModelRoutePolicy |
| POST | `/api/model/routes/{id}/test` | test | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; body[可选]=application/json:TestRequest | 200=*/*:ResultModelProviderCapabilities |

### open-ai-compatible-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/conversation/chat/completions` | chatCompletions | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ChatCompletionRequest | 200=application/json:object/text/event-stream:object |
| POST | `/api/conversation/responses` | responses | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ResponsesRequest | 200=application/json:object/text/event-stream:object |
| GET | `/api/model/models` | models_1 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:object |

### prompt-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/conversation/prompts` | list_10 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListPromptTemplate |
| POST | `/api/conversation/prompts` | create_18 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:CreateRequest | 200=*/*:ResultPromptTemplate |
| POST | `/api/conversation/prompts/preview` | preview | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:PreviewRequest | 200=*/*:ResultPromptPreview |
| POST | `/api/conversation/prompts/{id}/publish` | publish_2 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultPromptTemplate |

### question-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/education/question` | list_7 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataQuestionResponse |
| POST | `/api/education/question` | create_14 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:QuestionRequest | 200=*/*:ResultQuestionResponse |
| GET | `/api/education/question/difficulty` | listByDifficulty | 登录态；细粒度权限见权限矩阵 | difficulty[query,必填]:integer/int32 | 200=*/*:ResultListQuestionResponse |
| GET | `/api/education/question/subject-grade` | listBySubjectAndGrade_1 | 登录态；细粒度权限见权限矩阵 | subjectCode[query,必填]:string; grade[query,必填]:integer/int32 | 200=*/*:ResultListQuestionResponse |
| GET | `/api/education/question/type` | listByType_1 | 登录态；细粒度权限见权限矩阵 | type[query,必填]:string | 200=*/*:ResultListQuestionResponse |
| DELETE | `/api/education/question/{id}` | delete_12 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/question/{id}` | getById_9 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultQuestionResponse |
| PUT | `/api/education/question/{id}` | update_12 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:QuestionRequest | 200=*/*:ResultVoid |

### resource-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/education/resource` | list_6 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataResourceResponse |
| POST | `/api/education/resource` | create_13 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ResourceRequest | 200=*/*:ResultResourceResponse |
| GET | `/api/education/resource/subject` | listBySubjectCode | 登录态；细粒度权限见权限矩阵 | subjectCode[query,必填]:string | 200=*/*:ResultListResourceResponse |
| GET | `/api/education/resource/type` | listByType | 登录态；细粒度权限见权限矩阵 | type[query,必填]:string | 200=*/*:ResultListResourceResponse |
| DELETE | `/api/education/resource/{id}` | delete_11 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/resource/{id}` | getById_8 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultResourceResponse |
| PUT | `/api/education/resource/{id}` | update_11 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:ResourceRequest | 200=*/*:ResultVoid |

### review-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/education/review` | list_5 | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64; status[query,必填]:integer/int32 | 200=*/*:ResultListReviewTaskResponse |
| POST | `/api/education/review` | create_12 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ReviewRequest | 200=*/*:ResultReviewTaskResponse |
| POST | `/api/education/review/complete` | complete | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:CompleteReviewRequest | 200=*/*:ResultVoid |
| GET | `/api/education/review/today` | listTodayTasks | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListReviewTaskResponse |
| DELETE | `/api/education/review/{id}` | delete_10 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/review/{id}` | getById_7 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultReviewTaskResponse |
| PUT | `/api/education/review/{id}` | update_10 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:ReviewRequest | 200=*/*:ResultVoid |

### role-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/iam/roles` | getRoleList | 登录态；细粒度权限见权限矩阵 | r[query,必填]:RolePageRequest | 200=*/*:ResultPageDataRoleVO |
| POST | `/api/iam/roles` | createRole | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:RoleRequest | 200=*/*:ResultVoid |
| GET | `/api/iam/roles/all` | getAllRoles | 登录态；细粒度权限见权限矩阵 | status[query,可选]:string; tenantId[query,必填]:integer/int64 | 200=*/*:ResultListRoleVO |
| POST | `/api/iam/roles/menus/replace` | replaceRoleMenus | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; tenantId[query,必填]:integer/int64; body[必填]=application/json:array<integer/int64> | 200=*/*:ResultVoid |
| POST | `/api/iam/roles/users/add` | assignUserRoles | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:AssignUserRolesRequest | 200=*/*:ResultVoid |
| POST | `/api/iam/roles/users/remove` | removeUserRoles | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:AssignUserRolesRequest | 200=*/*:ResultVoid |
| DELETE | `/api/iam/roles/{id}` | deleteRole | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/iam/roles/{id}` | getRoleDetail | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; tenantId[query,必填]:integer/int64 | 200=*/*:ResultRoleVO |
| PUT | `/api/iam/roles/{id}` | updateRole | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:RoleRequest | 200=*/*:ResultVoid |

### runtime-console-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/console/api/logs/files` | logFiles | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:LogFilesResponse |
| GET | `/console/api/logs/tail` | tail | 登录态；细粒度权限见权限矩阵 | file[query,必填]:string; cursor[query,可选]:string; maxBytes[query,可选]:integer/int32; level[query,可选]:string; query[query,可选]:string | 200=*/*:LogChunk |
| GET | `/console/api/metrics` | metrics | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:MetricsResponse |
| GET | `/console/api/runtime` | runtime | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:RuntimeStatus |

### runtime-lifecycle-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/console/api/lifecycle/internal-link` | internalLink | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:object |
| POST | `/console/api/lifecycle/internal-shutdown` | internalShutdown | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:object |
| POST | `/console/api/lifecycle/restart` | restart | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:object |
| POST | `/console/api/lifecycle/stop` | stop | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:object |

### student-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/education/students` | list_4 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataStudentResponse |
| POST | `/api/education/students` | create_11 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:StudentRequest | 200=*/*:ResultStudentResponse |
| GET | `/api/education/students/user` | getByUserId | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64 | 200=*/*:ResultStudentResponse |
| DELETE | `/api/education/students/{id}` | delete_9 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/students/{id}` | getById_6 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultStudentResponse |
| PUT | `/api/education/students/{id}` | update_9 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:StudentRequest | 200=*/*:ResultVoid |

### study-plan-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/education/study-plan` | create_10 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:StudyPlanRequest | 200=*/*:ResultStudyPlanResponse |
| GET | `/api/education/study-plan/active` | listActiveByStudent | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListStudyPlanResponse |
| GET | `/api/education/study-plan/student` | listByStudentId_1 | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListStudyPlanResponse |
| GET | `/api/education/study-plan/today-tasks` | getTodayTasks | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListDailyTaskResponse |
| DELETE | `/api/education/study-plan/{id}` | delete_8 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/study-plan/{id}` | getById_5 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultStudyPlanResponse |
| PUT | `/api/education/study-plan/{id}` | update_8 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:StudyPlanRequest | 200=*/*:ResultVoid |

### subject-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/education/subject` | list_3 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataSubjectResponse |
| POST | `/api/education/subject` | create_9 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:SubjectRequest | 200=*/*:ResultSubjectResponse |
| GET | `/api/education/subject/code` | getByCode_1 | 登录态；细粒度权限见权限矩阵 | code[query,必填]:string | 200=*/*:ResultSubjectResponse |
| GET | `/api/education/subject/grade-level` | listByGradeLevel | 登录态；细粒度权限见权限矩阵 | gradeLevel[query,必填]:string | 200=*/*:ResultListSubjectResponse |
| DELETE | `/api/education/subject/{id}` | delete_7 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/subject/{id}` | getById_4 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultSubjectResponse |
| PUT | `/api/education/subject/{id}` | update_7 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:SubjectRequest | 200=*/*:ResultVoid |

### tenant-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/iam/tenants` | getTenantPage | 登录态；细粒度权限见权限矩阵 | r[query,必填]:TenantPageRequest | 200=*/*:ResultPageDataTenantVO |
| POST | `/api/iam/tenants` | createTenant | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:TenantRequest | 200=*/*:ResultVoid |
| GET | `/api/iam/tenants/tree` | getAllTenants | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListTenantVO |
| DELETE | `/api/iam/tenants/{id}` | deleteTenant | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/iam/tenants/{id}` | getTenantById | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultTenantVO |
| PUT | `/api/iam/tenants/{id}` | updateTenant | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:TenantRequest | 200=*/*:ResultVoid |

### textbook-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/education/textbook` | list_2 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataTextbookResponse |
| POST | `/api/education/textbook` | create_8 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:TextbookRequest | 200=*/*:ResultTextbookResponse |
| GET | `/api/education/textbook/subject-grade` | listBySubjectAndGrade | 登录态；细粒度权限见权限矩阵 | subjectCode[query,必填]:string; grade[query,必填]:integer/int32 | 200=*/*:ResultListTextbookResponse |
| DELETE | `/api/education/textbook/{id}` | delete_6 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/textbook/{id}` | getById_3 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultTextbookResponse |
| PUT | `/api/education/textbook/{id}` | update_6 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:TextbookRequest | 200=*/*:ResultVoid |

### timezone-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/iam/timezone/current` | getTimezone | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultString |
| GET | `/api/iam/timezone/options` | getTimezoneOptions | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListTimezoneOptionVO |
| POST | `/api/iam/timezone/set` | setTimezone | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:SetTimezoneRequest | 200=*/*:ResultVoid |

### tool-approval-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/agent/approvals` | listAll | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListToolApproval |
| POST | `/api/agent/approvals/{id}/approve` | approve_1 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultToolApproval |
| POST | `/api/agent/approvals/{id}/reject` | reject_1 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultToolApproval |
| GET | `/api/agent/runs/{runId}/approvals` | list_12 | 登录态；细粒度权限见权限矩阵 | runId[path,必填]:string | 200=*/*:ResultListToolApproval |
| POST | `/api/agent/runs/{runId}/approvals` | request | 登录态；细粒度权限见权限矩阵 | runId[path,必填]:string; body[必填]=application/json:Request | 200=*/*:ResultToolApproval |

### user-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/iam/users` | getUserList | 登录态；细粒度权限见权限矩阵 | r[query,必填]:UserPageRequest | 200=*/*:ResultPageDataUserVO |
| POST | `/api/iam/users` | createUser | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:UserRequest | 200=*/*:ResultMapStringObject |
| GET | `/api/iam/users/detail` | getUserInfo | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultUserVO |
| POST | `/api/iam/users/password/change` | changePassword | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64; body[必填]=application/json:ChangePasswordRequest | 200=*/*:ResultVoid |
| POST | `/api/iam/users/password/reset` | resetPassword | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64; body[必填]=application/json:ResetPasswordRequest | 200=*/*:ResultVoid |
| GET | `/api/iam/users/tenant-assignments` | getTenantAssignments | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64 | 200=*/*:ResultListUserTenantAssignmentVO |
| POST | `/api/iam/users/tenant-assignments/replace` | replaceTenantAssignments | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64; body[必填]=application/json:array<UserTenantRoleRequest> | 200=*/*:ResultVoid |
| DELETE | `/api/iam/users/{id}` | deleteUser | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| PUT | `/api/iam/users/{id}` | updateUser | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:UserRequest | 200=*/*:ResultVoid |

### wrong-question-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/education/wrong-question` | create_7 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:WrongQuestionRequest | 200=*/*:ResultWrongQuestionResponse |
| GET | `/api/education/wrong-question/student` | listByStudentId | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListWrongQuestionResponse |
| DELETE | `/api/education/wrong-question/{id}` | delete_5 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/wrong-question/{id}` | getById_2 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64 | 200=*/*:ResultWrongQuestionResponse |
| PUT | `/api/education/wrong-question/{id}` | update_5 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; body[必填]=application/json:WrongQuestionRequest | 200=*/*:ResultVoid |

### 插件系统

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/tooling/plugins` | 列出所有插件 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListPluginInfoVO |
| GET | `/api/tooling/plugins/market` | market | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListPluginMarketEntry |
| POST | `/api/tooling/plugins/market/publish` | publish | 登录态；细粒度权限见权限矩阵 | developmentMode[query,可选]:boolean; body[必填]=application/json:PluginMarketEntry | 200=*/*:ResultPluginMarketEntry |
| POST | `/api/tooling/plugins/market/{pluginId}/disable` | disable | 登录态；细粒度权限见权限矩阵 | pluginId[path,必填]:string | 200=*/*:ResultVoid |
| POST | `/api/tooling/plugins/scan` | 重新扫描插件目录 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultVoid |
| POST | `/api/tooling/plugins/start` | 启动插件 | 登录态；细粒度权限见权限矩阵 | pluginId[query,必填]:string | 200=*/*:ResultVoid |
| POST | `/api/tooling/plugins/stop` | 停止插件 | 登录态；细粒度权限见权限矩阵 | pluginId[query,必填]:string | 200=*/*:ResultVoid |
| POST | `/api/tooling/plugins/uninstall` | 卸载插件 | 登录态；细粒度权限见权限矩阵 | pluginId[query,必填]:string | 200=*/*:ResultVoid |

### 智能推荐

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/education/recommend/hybrid` | 混合推荐 — 聚合知识点/题目/资源/复习 + AI 综合学习建议 | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultHybridRecommendResponse |
| GET | `/api/education/recommend/knowledge` | 推荐薄弱知识点 — 基于能力差距 + 遗忘紧迫度 | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64; topK[query,可选]:integer/int32 | 200=*/*:ResultListKnowledgeRecommendResponse |
| GET | `/api/education/recommend/questions` | 推荐题目 — 基于薄弱知识点 + 难度匹配 + 能力维度 | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64; count[query,可选]:integer/int32 | 200=*/*:ResultListQuestionRecommendResponse |
| GET | `/api/education/recommend/resources` | 推荐学习资源 — 基于薄弱点 + 最近学习知识点 | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64; topK[query,可选]:integer/int32 | 200=*/*:ResultListResourceRecommendResponse |
| GET | `/api/education/recommend/review` | 推荐复习任务 — 基于遗忘曲线的到期/即将到期复习 | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64; count[query,可选]:integer/int32 | 200=*/*:ResultListQuestionRecommendResponse |

### 知识任务

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/knowledge/ingestion-jobs` | page_6 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32; spaceId[query,可选]:integer/int64; status[query,可选]:string; version[header,可选]:string | 200=*/*:ResultPageDataJobView |
| GET | `/api/knowledge/ingestion-jobs/{id}` | get_2 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultJobView |
| POST | `/api/knowledge/ingestion-jobs/{id}/cancel` | cancel | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultVoid |
| POST | `/api/knowledge/ingestion-jobs/{id}/retry` | retry | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultVoid |

### 知识关系

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/knowledge/points/{pointId}/relations` | list_1 | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListRelationView |
| POST | `/api/knowledge/points/{pointId}/relations` | create_4 | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:RelationRequest | 200=*/*:ResultVoid |
| DELETE | `/api/knowledge/points/{pointId}/relations/{targetId}` | delete_21 | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; targetId[path,必填]:integer/int64; type[query,必填]:string(PRE,NEXT,INCLUDE,RELATED,SIMILAR,BELONG); version[header,可选]:string | 200=*/*:ResultVoid |

### 知识平台审计

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/knowledge/audits` | page_7 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32; spaceId[query,可选]:integer/int64; version[header,可选]:string | 200=*/*:ResultPageDataKnowledgeAuditResponse |

### 知识引擎运维

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/knowledge/system/backup` | backup | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string | 200=*/*:ResultBackupResult |
| POST | `/api/knowledge/system/restore-check` | restoreCheck | 登录态；细粒度权限见权限矩阵 | fileName[query,必填]:string; version[header,可选]:string | 200=*/*:ResultRestoreCheckResult |
| GET | `/api/knowledge/system/status` | status_1 | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string | 200=*/*:ResultMapStringObject |

### 知识文档

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| DELETE | `/api/knowledge/documents/upload-sessions/{sessionId}` | cancelUpload | 登录态；细粒度权限见权限矩阵 | sessionId[path,必填]:string; version[header,可选]:string | 200=*/*:ResultVoid |
| GET | `/api/knowledge/documents/upload-sessions/{sessionId}` | uploadStatus | 登录态；细粒度权限见权限矩阵 | sessionId[path,必填]:string; version[header,可选]:string | 200=*/*:ResultUploadSession |
| POST | `/api/knowledge/documents/upload-sessions/{sessionId}/chunks/{index}` | uploadChunk | 登录态；细粒度权限见权限矩阵 | sessionId[path,必填]:string; index[path,必填]:integer/int32; totalChunks[query,必填]:integer/int32; version[header,可选]:string; body[可选]=multipart/form-data:object | 200=*/*:ResultUploadSession |
| POST | `/api/knowledge/documents/upload-sessions/{sessionId}/complete` | completeUpload | 登录态；细粒度权限见权限矩阵 | sessionId[path,必填]:string; version[header,可选]:string | 200=*/*:ResultUploadResult |
| DELETE | `/api/knowledge/documents/{id}` | delete_19 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultVoid |
| GET | `/api/knowledge/documents/{id}` | get_3 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultDocumentView |
| POST | `/api/knowledge/documents/{id}/approve` | approve | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; comment[query,可选]:string; version[header,可选]:string | 200=*/*:ResultDocumentView |
| POST | `/api/knowledge/documents/{id}/archive` | archive | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; comment[query,可选]:string; version[header,可选]:string | 200=*/*:ResultDocumentView |
| GET | `/api/knowledge/documents/{id}/preview` | preview_3 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:string/byte |
| POST | `/api/knowledge/documents/{id}/publish` | publish_1 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; comment[query,可选]:string; version[header,可选]:string | 200=*/*:ResultDocumentView |
| POST | `/api/knowledge/documents/{id}/reject` | reject | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; comment[query,可选]:string; version[header,可选]:string | 200=*/*:ResultDocumentView |
| POST | `/api/knowledge/documents/{id}/submit` | submit | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; comment[query,可选]:string; version[header,可选]:string | 200=*/*:ResultDocumentView |
| GET | `/api/knowledge/documents/{id}/versions` | versions_1 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListVersionView |
| POST | `/api/knowledge/documents/{id}/versions/{versionId}/rollback` | rollback | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; versionId[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultDocumentView |
| GET | `/api/knowledge/spaces/{spaceId}/documents` | page_2 | 登录态；细粒度权限见权限矩阵 | spaceId[path,必填]:integer/int64; pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32; keyword[query,可选]:string; lifecycleStatus[query,可选]:string; parseStatus[query,可选]:string; version[header,可选]:string | 200=*/*:ResultPageDataDocumentView |
| POST | `/api/knowledge/spaces/{spaceId}/documents` | upload | 登录态；细粒度权限见权限矩阵 | spaceId[path,必填]:integer/int64; title[query,可选]:string; version[header,可选]:string; body[可选]=multipart/form-data:object | 200=*/*:ResultUploadResult |
| POST | `/api/knowledge/spaces/{spaceId}/documents/import-url` | importUrl | 登录态；细粒度权限见权限矩阵 | spaceId[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:ImportUrlRequest | 200=*/*:ResultUploadResult |
| POST | `/api/knowledge/spaces/{spaceId}/documents/upload-sessions` | beginUpload | 登录态；细粒度权限见权限矩阵 | spaceId[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:BeginRequest | 200=*/*:ResultUploadSession |

### 知识检索

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/knowledge/index-jobs/rebuild` | rebuild_1 | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string; body[必填]=application/json:RebuildRequest | 200=*/*:ResultLong |
| POST | `/api/knowledge/search` | search | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string; body[必填]=application/json:SearchRequest | 200=*/*:ResultSearchResponse |

### 知识点

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| DELETE | `/api/knowledge/points/{id}` | delete_3 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultVoid |
| GET | `/api/knowledge/points/{id}` | get_1 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultPointView |
| PUT | `/api/knowledge/points/{id}` | update_3 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:UpdatePointRequest | 200=*/*:ResultPointView |
| GET | `/api/knowledge/points/{id}/graph` | graph | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultKnowledgeGraphResponse |
| GET | `/api/knowledge/spaces/{spaceId}/points` | page_1 | 登录态；细粒度权限见权限矩阵 | spaceId[path,必填]:integer/int64; pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32; keyword[query,可选]:string; category[query,可选]:string; version[header,可选]:string | 200=*/*:ResultPageDataPointView |
| POST | `/api/knowledge/spaces/{spaceId}/points` | create_3 | 登录态；细粒度权限见权限矩阵 | spaceId[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:CreatePointRequest | 200=*/*:ResultPointView |

### 知识点文档关系

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/knowledge/documents/{documentId}/points` | listPoints | 登录态；细粒度权限见权限矩阵 | documentId[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListLong |
| PUT | `/api/knowledge/documents/{documentId}/points` | replacePoints | 登录态；细粒度权限见权限矩阵 | documentId[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:ReplacePointsRequest | 200=*/*:ResultVoid |
| GET | `/api/knowledge/documents/{documentId}/relations` | listDocumentRelations | 登录态；细粒度权限见权限矩阵 | documentId[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListDocumentRelationView |
| PUT | `/api/knowledge/documents/{documentId}/relations` | replaceDocumentRelations | 登录态；细粒度权限见权限矩阵 | documentId[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:ReplaceDocumentRelationsRequest | 200=*/*:ResultVoid |
| GET | `/api/knowledge/points/{pointId}/documents` | list | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListDocumentSummary |
| PUT | `/api/knowledge/points/{pointId}/documents` | replace | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:ReplaceRequest | 200=*/*:ResultVoid |

### 知识空间

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/knowledge/spaces` | page | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32; keyword[query,可选]:string; domainCode[query,可选]:string; version[header,可选]:string | 200=*/*:ResultPageDataSpaceView |
| POST | `/api/knowledge/spaces` | create_2 | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string; body[必填]=application/json:CreateSpaceRequest | 200=*/*:ResultSpaceView |
| POST | `/api/knowledge/spaces/default` | ensureDefault | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string | 200=*/*:ResultSpaceView |
| GET | `/api/knowledge/spaces/options` | options | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListSpaceView |
| DELETE | `/api/knowledge/spaces/{id}` | delete_2 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultVoid |
| GET | `/api/knowledge/spaces/{id}` | get | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultSpaceView |
| PUT | `/api/knowledge/spaces/{id}` | update_2 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:UpdateSpaceRequest | 200=*/*:ResultSpaceView |
| GET | `/api/knowledge/spaces/{id}/difficulty-scale` | difficultyScale | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultDifficultyScaleView |
| GET | `/api/knowledge/spaces/{id}/members` | members | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListMemberView |
| PUT | `/api/knowledge/spaces/{id}/members` | replaceMembers | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:array<MemberRequest> | 200=*/*:ResultVoid |

### 知识评测

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/knowledge/evaluations` | page_3 | 登录态；细粒度权限见权限矩阵 | spaceId[query,必填]:integer/int64; pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32; version[header,可选]:string | 200=*/*:ResultPageDataCaseView |
| POST | `/api/knowledge/evaluations` | create_5 | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string; body[必填]=application/json:CreateCaseRequest | 200=*/*:ResultCaseView |
| POST | `/api/knowledge/evaluations/run` | run | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string; body[必填]=application/json:RunRequest | 200=*/*:ResultRunResult |
| DELETE | `/api/knowledge/evaluations/{id}` | delete_22 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultVoid |

### 知识路径

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/knowledge/points/path` | findPath | 登录态；细粒度权限见权限矩阵 | fromId[query,必填]:integer/int64; toId[query,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListLong |
| GET | `/api/knowledge/points/{pointId}/path` | path | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListLong |
| GET | `/api/knowledge/points/{pointId}/prerequisites` | prerequisites | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; masteredIds[query,可选]:array<integer/int64>; version[header,可选]:string | 200=*/*:ResultListLong |

## 组件模型

| Schema | 类型 | 必填字段 | 字段定义 |
|---|---|---|---|
| `AbilityRadarResponse` | object | - | abilities:object; knowledgeId:integer/int64; overallMastery:number/double; studentId:integer/int64 |
| `ActiveLeafRequest` | object | - | messageId:string |
| `AgentDefinition` | object | - | agentId:string; createdAt:integer/int64; currentVersion:string; description:string; extInfo:object; name:string; startNodeId:string; updatedAt:integer/int64; versions:object |
| `AgentDetailVO` | object | - | agentId:string; createTime:string/date-time; currentVersion:string; description:string; extInfo:object; id:integer/int64; name:string; status:integer/int32; updateTime:string/date-time; versions:array<AgentVersionVO> |
| `AgentRequest` | object | agentId, name | agentId*:string; description:string; name*:string; status:integer/int32 |
| `AgentStateFactoryAgentState` | - | - | - |
| `AgentVO` | object | - | agentId:string; createTime:string/date-time; currentVersion:string; description:string; extInfo:object; id:integer/int64; name:string; status:integer/int32; updateTime:string/date-time |
| `AgentVersion` | object | - | createdAt:integer/int64; description:string; versionNumber:string |
| `AgentVersionDetailVO` | object | - | agentId:string; canvasConfig:string; createTime:string/date-time; description:string; graphConfig:GraphConfigVO; id:integer/int64; status:integer/int32; statusDesc:string; updateTime:string/date-time; versionNumber:string |
| `AgentVersionVO` | object | - | agentId:string; createTime:string/date-time; description:string; id:integer/int64; status:integer/int32; statusDesc:string; updateTime:string/date-time; versionNumber:string |
| `AiApp` | object | - | createdAt:string/date-time; description:string; id:string; name:string; ownerUserId:UserId; publishedVersionId:string; status:string; tenantId:TenantId; updatedAt:string/date-time |
| `AiAppPreview` | object | - | appId:string; appVersionId:string; configuration:object; executable:boolean; model:string; promptHash:string; status:string |
| `AiAppVersion` | object | - | appId:string; configJson:string; createdAt:string/date-time; id:string; publishedAt:string/date-time; status:string; tenantId:TenantId; version:string |
| `AiModelRequest` | object | modelName | description:string; displayName:string; isDefault:string; modelConfig:string; modelName*:string; platformId:integer/int64; sort:integer/int32; status:string |
| `AiModelResponse` | object | - | createTime:string/date-time; description:string; displayName:string; id:integer/int64; isDefault:string; modelConfig:string; modelName:string; platformId:integer/int64; platformName:string; sort:integer/int32; status:string; updateTime:string/date-time |
| `AiModelVO` | object | - | createTime:string/date-time; description:string; displayName:string; id:integer/int64; isDefault:string; modelConfig:string; modelName:string; platformId:integer/int64; platformName:string; sort:integer/int32; status:string; updateTime:string/date-time |
| `AiPlatformRequest` | object | code, name | adapterType:string(OPENAI_COMPATIBLE,OLLAMA); apiKey:string; availableModels:string; baseUrl:string; code*:string; extraConfig:string; isDefault:string; maxRetries:integer/int32; maxTokens:integer/int32; name*:string; remark:string; sort:integer/int32; status:string; temperature:number/double |
| `AiPlatformResponse` | object | - | adapterType:string(OPENAI_COMPATIBLE,OLLAMA); availableModels:string; baseUrl:string; code:string; createTime:string/date-time; extraConfig:string; id:integer/int64; isDefault:string; maxRetries:integer/int32; maxTokens:integer/int32; name:string; remark:string; sort:integer/int32; status:string; temperature:number/double; updateTime:string/date-time |
| `AiPlatformVO` | object | - | adapterType:string(OPENAI_COMPATIBLE,OLLAMA); availableModels:string; baseUrl:string; code:string; createTime:string/date-time; extraConfig:string; id:integer/int64; isDefault:string; maxRetries:integer/int32; maxTokens:integer/int32; name:string; remark:string; sort:integer/int32; status:string; temperature:number/double; updateTime:string/date-time |
| `AiRun` | object | - | appId:string; appVersionId:string; completedAt:string/date-time; completionTokens:integer/int64; conversationId:string; costSnapshot:string; createdAt:string/date-time; errorCode:string; estimatedUsage:boolean; executionId:string; generationId:string; id:string; lastEventSeq:integer/int64; model:string; ownerUserId:UserId; parentRunId:string; promptHash:string; promptTokens:integer/int64; sourceId:string; sourceType:string(CONVERSATION,GENERATION,AGENT,KNOWLEDGE,MEMORY,TOOL,API); status:string(CREATED,RUNNING,COMPLETED,FAILED,CANCELLED); tenantId:TenantId; traceId:string; version:integer/int64 |
| `AiRunEvent` | object | - | appId:string; appVersionId:string; conversationId:string; createdAt:string/date-time; executionId:string; generationId:string; parentEventSeq:integer/int64; payload:string; providerRequestId:string; redacted:boolean; runId:string; schemaVersion:integer/int32; seq:integer/int64; stepId:string; tenantId:TenantId; traceId:string; turnId:string; type:string(RUN_STARTED,TURN_STARTED,TURN_COMPLETED,STEP_STARTED,STEP_COMPLETED,PROMPT_ASSEMBLED,RETRIEVAL_STARTED,RETRIEVAL_COMPLETED,MODEL_STARTED,MODEL_BLOCK_STARTED,MODEL_DELTA,MODEL_REASONING_DELTA,MODEL_TOOL_CALL_DELTA,MODEL_BLOCK_COMPLETED,TOOL_REQUESTED,TOOL_APPROVAL_REQUIRED,TOOL_APPROVAL_DECIDED,TOOL_COMPLETED,MEMORY_READ,MEMORY_WRITE,MODEL_USAGE,MODEL_COMPLETED,RUN_COMPLETED,RUN_FAILED,RUN_CANCELLED) |
| `AppExecutionRequest` | object | - | appVersionId:string; input:object; prompt:string |
| `AppRequest` | object | - | description:string; name:string |
| `AssignUserRolesRequest` | object | tenantId | tenantId*:integer/int64; userIds:array<integer/int64> |
| `AuthCodeOptionVO` | object | - | action:string; code:string; createTime:string/date-time; id:integer/int64; module:string; name:string; resource:string; status:integer/int32 |
| `AuthCodePageRequest` | object | - | code:string; filterConditions:array<FilterCondition>; isAsc:string; name:string; orderByColumn:string; orderFields:array<OrderField>; pageNum:integer/int32; pageSize:integer/int32 |
| `AuthCodeRequest` | object | code | code*:string; name:string |
| `AuthCodeResponse` | object | - | code:string; createTime:string/date-time; id:integer/int64; name:string; status:integer/int32; updateTime:string/date-time |
| `BackupResult` | object | - | createdAt:string; fileName:string; size:integer/int64 |
| `BaseNode` | object | - | config:NodeConfig; executionHistoryService:ExecutionHistoryService; requiredInputs:array<NodeInputParam> |
| `BeginRequest` | object | fileName | checksum:string; contentType:string; fileName*:string; size:integer/int64; title:string |
| `CaptchaVO` | object | - | expireTime:integer/int64; image:string; key:string |
| `CaseRequest` | object | - | expected:string; input:string; metadata:object |
| `CaseResult` | object | - | caseId:integer/int64; citationAccuracy:number/double; expectedDocumentIds:array<integer/int64>; question:string; recallAtK:number/double; reciprocalRank:number/double; returnedDocumentIds:array<integer/int64> |
| `CaseView` | object | - | expectedAnswer:string; expectedDocIds:string; id:integer/int64; question:string; spaceId:integer/int64 |
| `ChangePasswordRequest` | object | newPassword, oldPassword | newPassword*:string; oldPassword*:string |
| `ChannelObject` | object | - | default:-; reducer:ReducerObject |
| `ChapterRequest` | object | name, textbookId | chapterOrder:integer/int32; id:integer/int64; name*:string; parentId:integer/int64; status:integer/int32; textbookId*:integer/int64 |
| `ChapterResponse` | object | - | chapterOrder:integer/int32; children:array<ChapterResponse>; id:integer/int64; name:string; parentId:integer/int64; textbookId:integer/int64 |
| `CharacterAsset` | object | - | card:CharacterCardV2; createdAt:string/date-time; id:string; ownerUserId:integer/int64; pngData:string/byte; tenantId:integer/int64; updatedAt:string/date-time; visibility:string |
| `CharacterCardV2` | object | - | description:string; exampleDialogues:array<string>; extensions:object; firstMessage:string; name:string; scenario:string; spec:string; systemPrompt:string; version:integer/int32 |
| `CharacterRequest` | object | - | card:CharacterCardV2; visibility:string |
| `ChatCompletionRequest` | object | - | conversationId:string; maxOutputTokens:integer/int32; messages:array<object>; model:string; platform:string; reasoningEffort:string; store:boolean; stream:boolean; temperature:number/double; tools:array<object> |
| `CodeLoginRequest` | object | captchaKey, code, phone | captchaKey*:string; code*:string; phone*:string |
| `CompileConfig` | - | - | - |
| `CompiledGraphAgentState` | object | - | compileConfig:CompileConfig; maxIterations:integer/int32; stateGraph:StateGraphAgentState |
| `CompleteReviewRequest` | object | - | resultScore:number/double; studentId:integer/int64 |
| `ConditionEdge` | object | - | defaultTarget:string; from:string; functionCondition:-; nodeMappings:object; predicateConditions:array<PredicateCondition> |
| `ConditionalEdgeDTO` | object | - | conditionType:string; defaultTarget:string; nodeMappings:object |
| `ConfigApplyResult` | object | - | issues:array<string>; message:string; restartRequired:boolean; revision:integer/int64; status:string |
| `ConfigChangeSet` | object | - | expectedVersion:integer/int64; secrets:object; values:object |
| `ConfigFieldDescriptor` | object | - | applyMode:string; configured:boolean; editable:boolean; effectiveValue:string; key:string; label:string; savedValue:string; sensitive:boolean; source:string; supported:boolean; type:string; unsupportedReason:string |
| `ConfigResponse` | object | - | fields:array<ConfigFieldDescriptor>; version:integer/int64 |
| `ContentPart` | object | - | mediaUri:string; metadata:object; mimeType:string; text:string; type:string |
| `Conversation` | object | - | activeLeafMessageId:string; branchFromMessageId:string; createdAt:string/date-time; id:string; model:string; ownerUserId:integer/int64; parentConversationId:string; platform:string; rollingSummary:string; sceneType:string; status:string(ACTIVE,ARCHIVED,DELETED); tenantId:integer/int64; title:string; updatedAt:string/date-time; version:integer/int64 |
| `ConversationMessage` | object | - | contentParts:array<ContentPart>; conversationId:string; createdAt:string/date-time; generationId:string; id:string; parentMessageId:string; role:string(SYSTEM,USER,ASSISTANT,TOOL); sequence:integer/int32; sourceMessageId:string; status:string(PENDING,STREAMING,COMPLETED,CANCELLED,FAILED,DELETED); toolCall:object; updatedAt:string/date-time |
| `CourseRequest` | object | name | coverUrl:string; description:string; grade:integer/int32; id:integer/int64; name*:string; status:integer/int32; subjectCode:string; teacherId:integer/int64; textbookId:integer/int64; totalHours:integer/int32 |
| `CourseResponse` | object | - | coverUrl:string; description:string; grade:integer/int32; id:integer/int64; name:string; status:integer/int32; subjectCode:string; teacherId:integer/int64; textbookId:integer/int64; totalHours:integer/int32 |
| `CreateCaseRequest` | object | question | expectedAnswer:string; expectedDocIds:string; question*:string; spaceId:integer/int64 |
| `CreateConversationRequest` | object | - | model:string; platform:string; sceneType:string; systemPrompt:string; title:string |
| `CreatePointRequest` | object | code, name | category:string; code*:string; description:string; difficultyLevel:integer/int32; name*:string; tags:string |
| `CreateRequest` | object | - | name:string; template:string; variables:array<string> |
| `CreateSpaceRequest` | object | code, name | accessMode:string; bindingMode:string; chunkOverlap:integer/int32; chunkSize:integer/int32; chunkStrategy:string; code*:string; description:string; difficultyScaleId:integer/int64; domainCode:string; embeddingProfile:string; name*:string; rerankProfile:string; reviewMode:string |
| `DailyTaskResponse` | object | - | id:integer/int64; knowledgeId:integer/int64; knowledgeName:string; orderNo:integer/int32; planDate:string; status:integer/int32; statusDesc:string |
| `DataSourceConfig` | object | - | dependsOn:string; dictType:string; labelKey:string; type:string; url:string; valueKey:string |
| `DatasetRequest` | object | - | description:string; name:string |
| `DictPageRequest` | object | - | filterConditions:array<FilterCondition>; isAsc:string; orderByColumn:string; orderFields:array<OrderField>; pageNum:integer/int32; pageSize:integer/int32 |
| `DictRequest` | object | - | cssClass:string; dictLabel:string; dictSort:integer/int32; dictType:string; dictValue:string; isDefault:string; listClass:string; remark:string; status:integer/int32 |
| `DictVO` | object | - | createTime:string/date-time; cssClass:string; dictLabel:string; dictSort:integer/int32; dictType:string; dictValue:string; id:integer/int64; isDefault:string; listClass:string; remark:string; status:string; tenantId:integer/int64; updateTime:string/date-time |
| `DiffRequest` | object | - | fromVersion:integer/int32; toVersion:integer/int32 |
| `DifficultyLevelView` | object | - | description:string; label:string; level:integer/int32 |
| `DifficultyScaleView` | object | - | code:string; description:string; id:integer/int64; levelCount:integer/int32; levels:array<DifficultyLevelView>; name:string |
| `DocumentRelationRequest` | object | - | documentId:integer/int64; relationType:string |
| `DocumentRelationView` | object | - | id:integer/int64; relationType:string; sourceDocumentId:integer/int64; targetDocumentId:integer/int64; targetTitle:string |
| `DocumentSummary` | object | - | docType:string; id:integer/int64; lifecycleStatus:string; parseStatus:string; spaceId:integer/int64; title:string |
| `DocumentView` | object | - | checksum:string; createTime:string/date-time; currentVersionId:integer/int64; docType:string; fileSize:integer/int64; id:integer/int64; lifecycleStatus:string; mimeType:string; objectKey:string; parseStatus:string; source:string; spaceId:integer/int64; title:string; updateTime:string/date-time |
| `EdgeRequest` | object | sourceNodeId, targetNodeId | conditionMappings:object; conditionType:string; defaultTarget:string; edgeType:string; sourceNodeId*:string; targetNodeId*:string |
| `EditRequest` | object | - | content:string |
| `EvalCase` | object | - | createdAt:string/date-time; datasetId:string; expected:string; id:string; input:string; metadata:object; tenantId:integer/int64 |
| `EvalDataset` | object | - | createdAt:string/date-time; description:string; id:string; name:string; ownerUserId:integer/int64; tenantId:integer/int64 |
| `EvalResult` | object | - | caseId:string; detail:string; metric:string(EXACT_MATCH,CONTAINS,JSON_SCHEMA,TOOL_CALL_SCHEMA,CITATION_COVERAGE,RETRIEVAL_HIT,TOKEN_BUDGET,COST_BUDGET); passed:boolean; score:number/double |
| `EvalRun` | object | - | appVersionId:string; completedAt:string/date-time; createdAt:string/date-time; datasetId:string; id:string; metric:string(EXACT_MATCH,CONTAINS,JSON_SCHEMA,TOOL_CALL_SCHEMA,CITATION_COVERAGE,RETRIEVAL_HIT,TOKEN_BUDGET,COST_BUDGET); ownerUserId:integer/int64; passRate:number/double; results:array<EvalResult>; status:string; tenantId:integer/int64 |
| `EventRequest` | object | - | attributes:object; confidence:number/double; confirmationPolicy:string(AUTO,REQUIRED,DISABLED); content:string; eventType:string; importance:number/double; namespace:string; occurredAt:string/date-time; sourceId:string; sourceType:string; subjectId:string; subjectType:string |
| `ExamRequest` | object | durationMin, grade, name, subjectCode, totalScore, type | durationMin*:integer/int32; grade*:integer/int32; id:integer/int64; name*:string; status:integer/int32; subjectCode*:string; teacherId:integer/int64; totalScore*:integer/int32; type*:string |
| `ExamResponse` | object | - | durationMin:integer/int32; grade:integer/int32; id:integer/int64; name:string; status:integer/int32; subjectCode:string; teacherId:integer/int64; totalScore:integer/int32; type:string |
| `ExchangeRequest` | object | - | grant:string |
| `ExecutionHistoryService` | - | - | - |
| `FieldMeta` | object | - | defaultValue:-; description:string; key:string; label:string; options:object; required:boolean; source:DataSourceConfig; type:string |
| `FileView` | object | - | contentType:string; key:string; lastModified:string/date-time; name:string; size:integer/int64; storageType:string; url:string |
| `FilterCondition` | object | - | field:string; operator:string(EQ,NE,GT,GE,LT,LE,LIKE,IN,NOT_IN,IS_NULL,IS_NOT_NULL); value:- |
| `ForgetPasswordRequest` | object | captchaKey, code, email, newPassword | captchaKey*:string; code*:string; email*:string/email; newPassword*:string |
| `GenerateRequest` | object | - | format:string; prompt:string; provider:string |
| `GenerationRun` | object | - | assistantMessageId:string; cancelRequested:boolean; completionTokens:integer/int64; conversationId:string; createdAt:string/date-time; errorCode:string; id:string; inputMessageId:string; lastEventSequence:integer/int32; latencyMs:integer/int64; model:string; platform:string; promptTokens:integer/int64; runtimeRunId:string; speakerId:string; status:string(CREATED,RUNNING,COMPLETED,CANCELLED,FAILED); updatedAt:string/date-time; version:integer/int64 |
| `Graph` | object | - | channels:object; compiled:boolean; compiledGraph:CompiledGraphAgentState; conditionalEdges:object; description:string; edges:object; endNode:string; name:string; nodes:object; startNode:string |
| `GraphConfigRequest` | object | - | conditionalEdges:object; description:string; edges:object; endNode:string; name:string; nodes:object; startNode:string |
| `GraphConfigVO` | object | - | conditionalEdges:object; description:string; edges:object; endNode:string; name:string; nodes:object; startNode:string |
| `GraphValidationVO` | object | - | errors:array<string>; valid:boolean; warnings:array<string> |
| `GroupChat` | object | - | id:string; maxTurns:integer/int32; name:string; participants:array<Participant>; speakerPolicy:string(MANUAL,ROUND_ROBIN,MODEL_ROUTED); tokenBudget:integer/int32 |
| `GroupChatAsset` | object | - | createdAt:string/date-time; group:GroupChat; id:string; ownerUserId:integer/int64; tenantId:integer/int64; updatedAt:string/date-time |
| `GroupRequest` | object | - | maxTurns:integer/int32; name:string; participants:array<Participant>; speakerPolicy:string(MANUAL,ROUND_ROBIN,MODEL_ROUTED); tokenBudget:integer/int32 |
| `GroupTurnRun` | object | - | decision:TurnDecision; generationId:string; speakerId:string |
| `HybridHit` | object | - | bm25Score:number/double; chunkId:integer/int64; content:string; documentId:integer/int64; highlight:string; rerankScore:number/double; rrfScore:number/double; vectorScore:number/double |
| `HybridRecommendResponse` | object | - | generateTime:integer/int64; knowledgeTop:array<KnowledgeRecommendResponse>; overallAdvice:string; questionTop:array<QuestionRecommendResponse>; resourceTop:array<ResourceRecommendResponse>; reviewTop:array<QuestionRecommendResponse>; studentId:integer/int64 |
| `IdNameOptionVO` | object | - | code:string; id:integer/int64; name:string; value:string |
| `ImageRequest` | object | - | imageBase64:string; instruction:string; mimeType:string; provider:string |
| `ImageResult` | object | - | height:integer/int32; mimeType:string; objectKey:string; width:integer/int32 |
| `ImportRequest` | object | - | content:string; format:string; model:string; platform:string; previewToken:string; sceneType:string; systemPrompt:string; title:string |
| `ImportUrlRequest` | object | url | title:string; url*:string |
| `ImportedMessage` | object | - | content:string; role:string |
| `IntentDefRequest` | object | agentId, code, name | agentId*:string; category:string; code*:string; confidenceThreshold:number/double; description:string; name*:string; priority:integer/int32; status:integer/int32; targetNode:string |
| `IntentDefVO` | object | - | agentId:string; category:string; code:string; createTime:string/date-time; description:string; id:integer/int64; name:string; status:string; updateTime:string/date-time |
| `JobView` | object | - | attempts:integer/int32; createTime:string/date-time; documentId:integer/int64; errorMessage:string; finishedTime:string/date-time; heartbeatTime:string/date-time; id:integer/int64; jobKey:string; jobType:string; maxAttempts:integer/int32; progress:integer/int32; spaceId:integer/int64; stage:string; startedTime:string/date-time; status:string; versionId:integer/int64 |
| `KnowledgeAuditResponse` | object | - | action:string; createTime:string/date-time; delFlag:integer/int32; detailJson:string; id:integer/int64; resourceId:integer/int64; resourceType:string; spaceId:integer/int64; status:integer/int32; tenantId:integer/int64; updateTime:string/date-time |
| `KnowledgeDocumentDTO` | object | - | content:string; docType:string; id:integer/int64; knowledgeIds:array<integer/int64>; source:string; title:string |
| `KnowledgeGraphResponse` | object | - | childNodes:array<KnowledgeResponse>; node:KnowledgeResponse; parentNodes:array<KnowledgeResponse>; relatedNodes:array<KnowledgeResponse> |
| `KnowledgeRecommendResponse` | object | - | knowledgeId:integer/int64; knowledgeName:string; mastery:number/double; reason:string; recommendType:string; score:integer/int32 |
| `KnowledgeResponse` | object | - | category:string; childIds:array<integer/int64>; code:string; description:string; difficulty:integer/int32; documents:array<KnowledgeDocumentDTO>; id:integer/int64; name:string; parentIds:array<integer/int64>; tags:string |
| `LogChunk` | object | - | fileName:string; lines:array<string>; nextCursor:string; nextOffset:integer/int64; rotated:boolean; truncated:boolean |
| `LogFile` | object | - | label:string; modifiedAt:string/date-time; name:string; sizeBytes:integer/int64 |
| `LogFilesResponse` | object | - | files:array<LogFile> |
| `LoginRequest` | object | password, username | captcha:string; captchaKey:string; email:string; password*:string; phone:string; roleId:integer/int64; username*:string |
| `LoginResponseVO` | object | - | accessToken:string; currentTenantId:integer/int64; expiresIn:integer/int64; homePath:string; homeTenantId:integer/int64; id:integer/int64; realName:string; roles:array<string>; subTenants:array<TenantContextVO>; switchMode:string; tenantName:string; tenants:array<TenantInfoVO>; tokenType:string; username:string |
| `LorebookAsset` | object | - | createdAt:string/date-time; entry:LorebookEntry; id:string; ownerUserId:integer/int64; tenantId:integer/int64; updatedAt:string/date-time |
| `LorebookEntry` | object | - | content:string; enabled:boolean; id:string; insertionPosition:string; keys:array<string>; priority:integer/int32; tokenBudget:integer/int32 |
| `McpToolDescriptor` | object | - | builtin:boolean; category:string; description:string; name:string; parameters:object; registeredAt:integer/int64; serverId:string; tags:array<string> |
| `MemberRequest` | object | principalId, principalType, spaceRole | principalId*:integer/int64; principalType*:string; spaceRole*:string |
| `MemberView` | object | - | id:integer/int64; principalId:integer/int64; principalType:string; spaceId:integer/int64; spaceRole:string |
| `MemoryEdge` | object | - | active:boolean; confidence:number/double; createdAt:string/date-time; directed:boolean; evidenceSource:string; graphType:string(TEMPORAL,SEMANTIC,CAUSAL,ENTITY); id:string; origin:string(RULE,DOMAIN,MODEL); relationType:string; sourceNodeId:string; targetNodeId:string; tenantId:TenantId; weight:number/double |
| `MemoryEvent` | object | - | attributes:object; confidence:number/double; confirmationPolicy:string(AUTO,REQUIRED,DISABLED); content:string; createdAt:string/date-time; eventType:string; id:string; importance:number/double; namespace:string; occurredAt:string/date-time; sourceId:string; sourceType:string; status:string(CANDIDATE,ACTIVE,SUPERSEDED,REVOKED); subjectId:string; subjectType:string; tenantId:TenantId; updatedAt:string/date-time |
| `MemoryPath` | object | - | edges:array<MemoryEdge>; event:MemoryEvent; score:number/double |
| `MemoryRetrievalResult` | object | - | paths:array<MemoryPath>; traceId:string |
| `MemoryRetrievalTrace` | object | - | anchorEventIds:array<string>; createdAt:string/date-time; filteredEventIds:array<string>; graphWeights:object; id:string; namespace:string; queryText:string; relationPaths:array<array<string>>; resultEventIds:array<string>; tenantId:TenantId |
| `MenuPageRequest` | object | - | code:string; filterConditions:array<FilterCondition>; isAsc:string; name:string; orderByColumn:string; orderFields:array<OrderField>; pageNum:integer/int32; pageSize:integer/int32; status:integer/int32; type:string |
| `MenuRequest` | object | name, type | code:string; component:string; description:string; icon:string; keepAlive:boolean; layout:string; method:string; name*:string; order:integer/int32; path:string; pid:integer/int64; redirect:string; show:boolean; status:string; type*:string |
| `MenuVO` | object | - | children:array<MenuVO>; code:string; component:string; createTime:string/date-time; delFlag:integer/int32; description:string; icon:string; id:integer/int64; keepAlive:boolean; layout:string; method:string; name:string; order:integer/int32; path:string; pid:integer/int64; redirect:string; show:boolean; status:integer/int32; type:string; updateTime:string/date-time |
| `MessageRequest` | object | - | content:string; model:string; platform:string |
| `MetaVO` | object | - | activeIcon:string; activePath:string; affixTab:boolean; affixTabOrder:integer/int32; authority:array<string>; badge:string; badgeType:string; badgeVariants:string; fullPathKey:boolean; hideChildrenInMenu:boolean; hideInBreadcrumb:boolean; hideInMenu:boolean; hideInTab:boolean; icon:string; iframeSrc:string; ignoreAccess:boolean; keepAlive:boolean; link:string; loaded:boolean; maxNumOfOpenTab:integer/int32; menuVisibleWithForbidden:boolean; noBasicLayout:boolean; openInNewWindow:boolean; order:integer/int32; query:-; title:string |
| `MetricSnapshot` | object | - | capturedAt:string/date-time; values:object |
| `MetricsResponse` | object | - | samples:array<MetricSnapshot> |
| `ModelProviderCapabilities` | object | - | cacheUsage:boolean; cancellation:boolean; contextWindow:integer/int32; features:array<string>; jsonSchema:boolean; maxOutputTokens:integer/int32; model:string; multimodal:boolean; parallelTools:boolean; provider:string; reasoningLevels:array<string>; streamUsage:boolean; streaming:boolean; tools:boolean |
| `ModelRoutePolicy` | object | - | fallbackOnError:boolean; id:string; maxTokens:integer/int64; name:string; orderedModels:array<string>; tenantId:integer/int64; timeoutMs:integer/int32 |
| `NodeConfig` | object | - | description:string; enabled:boolean; errorStrategy:string; logLevel:string; nodeId:string; nodeName:string; nodeType:NodeType; properties:object; retryCount:integer/int32; retryInterval:integer/int64; timeout:integer/int64 |
| `NodeConfigDTO` | object | - | config:object; description:string; enabled:boolean; errorStrategy:string; logLevel:string; nodeName:string; nodeType:string; properties:object; retryCount:integer/int32; retryInterval:integer/int64; timeout:integer/int64 |
| `NodeConfigRequest` | object | nodeId, nodeName, nodeType | config:object; description:string; enabled:boolean; errorStrategy:string; logLevel:string; nodeId*:string; nodeName*:string; nodeType*:string; properties:object; retryCount:integer/int32; retryInterval:integer/int64; timeout:integer/int64 |
| `NodeInputParam` | object | - | defaultValue:-; description:string; name:string; required:boolean; source:string(API_REQUEST,CONFIG_VALUE,PREVIOUS_NODE,DEFAULT_VALUE); type:string |
| `NodeType` | object | - | builtIn:boolean; code:string; description:string; name:string |
| `NodeTypeMetaVO` | object | - | code:string; color:string; description:string; fields:array<FieldMeta>; icon:string; name:string |
| `OrderField` | object | - | column:string; direction:string(ASC,DESC) |
| `OverviewResponse` | object | - | accuracy:number/double; masteredKnowledge:integer/int32; streakDays:integer/int32; totalKnowledge:integer/int32; totalQuestions:integer/int32; totalStudyDays:integer/int32; weeklyHours:number/double |
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
| `PageDataMenuVO` | object | - | items:array<MenuVO>; total:integer/int64 |
| `PageDataPointView` | object | - | items:array<PointView>; total:integer/int64 |
| `PageDataQuestionResponse` | object | - | items:array<QuestionResponse>; total:integer/int64 |
| `PageDataResourceResponse` | object | - | items:array<ResourceResponse>; total:integer/int64 |
| `PageDataRoleVO` | object | - | items:array<RoleVO>; total:integer/int64 |
| `PageDataSpaceView` | object | - | items:array<SpaceView>; total:integer/int64 |
| `PageDataStudentResponse` | object | - | items:array<StudentResponse>; total:integer/int64 |
| `PageDataSubjectResponse` | object | - | items:array<SubjectResponse>; total:integer/int64 |
| `PageDataTenantVO` | object | - | items:array<TenantVO>; total:integer/int64 |
| `PageDataTextbookResponse` | object | - | items:array<TextbookResponse>; total:integer/int64 |
| `PageDataUserVO` | object | - | items:array<UserVO>; total:integer/int64 |
| `ParameterInfo` | object | - | defaultValue:-; description:string; required:boolean; type:string |
| `Participant` | object | - | characterId:string; displayName:string; id:string |
| `Persona` | object | - | attributes:object; id:string; identity:string; name:string; ownerUserId:integer/int64; tone:string; visibility:string |
| `PersonaAsset` | object | - | createdAt:string/date-time; id:string; ownerUserId:integer/int64; persona:Persona; tenantId:integer/int64; updatedAt:string/date-time |
| `PluginInfoVO` | object | - | description:string; id:string; loadedAt:string; name:string; state:string; version:string |
| `PluginMarketEntry` | object | - | checksum:string; enabled:boolean; id:string; manifest:string; permissions:array<string>; publishedAt:string/date-time; publisherKey:string; signature:string; source:string; updatePolicy:string; version:string |
| `PointView` | object | - | category:string; code:string; description:string; difficultyLevel:integer/int32; id:integer/int64; name:string; spaceId:integer/int64; tags:string |
| `PredicateCondition` | object | - | predicate:-; target:string |
| `Preview` | object | - | expiresAt:string/date-time; messages:array<ImportedMessage>; token:string |
| `PreviewRequest` | object | - | template:string; variables:object |
| `PromptDiff` | object | - | changes:array<string>; fromVersion:integer/int32; templateId:string; toVersion:integer/int32 |
| `PromptPreview` | object | - | content:string; estimatedTokens:integer/int64; variables:array<string> |
| `PromptPreviewRequest` | object | - | body:string; variables:object |
| `PromptRequest` | object | - | body:string; status:string; templateId:string; testCases:array<string>; variableSchema:object; version:integer/int32 |
| `PromptTemplate` | object | - | createdAt:string/date-time; id:string; name:string; ownerUserId:integer/int64; status:string; template:string; tenantId:integer/int64; updatedAt:string/date-time; variables:array<string> |
| `PromptTemplateVersion` | object | - | body:string; createdAt:string/date-time; id:string; publishedAt:string/date-time; status:string; templateId:string; testCases:array<string>; variableSchema:object; version:integer/int32 |
| `PromptTestRequest` | object | - | variables:object; version:integer/int32 |
| `PromptTestRun` | object | - | estimatedTokens:integer/int32; renderedCases:array<string>; templateId:string; version:integer/int32 |
| `ProviderHealth` | object | - | checkedAt:string/date-time; consecutiveFailures:integer/int32; healthy:boolean; message:string; model:string; provider:string |
| `PublishRequest` | object | - | version:integer/int32 |
| `QueryRequest` | object | - | from:string/date-time; graphTypes:array<string(TEMPORAL,SEMANTIC,CAUSAL,ENTITY)>; intent:string(SEMANTIC,TEMPORAL,CAUSAL,ENTITY,HYBRID); maxDepth:integer/int32; maxNodes:integer/int32; maxTokens:integer/int32; namespace:string; subjectId:string; subjectType:string; text:string; to:string/date-time |
| `QuestionRecommendResponse` | object | - | difficulty:integer/int32; knowledgeId:integer/int64; knowledgeName:string; questionId:integer/int64; reason:string; recommendType:string; score:integer/int32; title:string; type:string |
| `QuestionRequest` | object | answer, difficulty, grade, subjectCode, title, type | abilityDimension:string; analysis:string; answer*:string; code:string; difficulty*:integer/int32; grade*:integer/int32; id:integer/int64; options:string; status:integer/int32; subjectCode*:string; tags:string; title*:string; type*:string |
| `QuestionResponse` | object | - | abilityDimension:string; analysis:string; answer:string; code:string; difficulty:integer/int32; grade:integer/int32; id:integer/int64; options:string; subjectCode:string; tags:string; title:string; type:string; usedCount:integer/int64 |
| `RebuildRequest` | object | spaceId | spaceId*:integer/int64 |
| `ReducerObject` | - | - | - |
| `RefreshTokenRequest` | object | accessToken | accessToken*:string |
| `RegisterAgentRequest` | object | - | agentId:string; description:string; graph:Graph; name:string; versionDescription:string; versionNumber:string |
| `RelationRequest` | object | sourceId, targetId, type | sourceId*:integer/int64; targetId*:integer/int64; type*:string(PRE,NEXT,INCLUDE,RELATED,SIMILAR,BELONG); weight:number/double |
| `RelationView` | object | - | relationType:string; source:KnowledgeResponse; sourceId:integer/int64; target:KnowledgeResponse; targetId:integer/int64; weight:number/double |
| `ReplaceDocumentRelationsRequest` | object | relations | relations*:array<DocumentRelationRequest> |
| `ReplacePointsRequest` | object | pointIds | pointIds*:array<integer/int64>; relationType:string |
| `ReplaceRequest` | object | documentIds | documentIds*:array<integer/int64>; relationType:string |
| `Request` | object | - | argumentsRedacted:string; toolName:string |
| `ResetPasswordRequest` | object | - | password:string |
| `ResourceRecommendResponse` | object | - | knowledgeId:integer/int64; knowledgeName:string; reason:string; recommendType:string; resourceId:integer/int64; score:integer/int32; title:string; type:string |
| `ResourceRequest` | object | name, type, url | coverUrl:string; description:string; difficulty:integer/int32; grade:integer/int32; id:integer/int64; name*:string; status:integer/int32; subjectCode:string; type*:string; url*:string |
| `ResourceResponse` | object | - | coverUrl:string; description:string; difficulty:integer/int32; grade:integer/int32; id:integer/int64; name:string; subjectCode:string; type:string; url:string; viewCount:integer/int64 |
| `ResponsesRequest` | object | - | conversationId:string; input:-; maxOutputTokens:integer/int32; model:string; platform:string; reasoningEffort:string; store:boolean; stream:boolean; temperature:number/double; tools:array<object> |
| `RestoreCheckResult` | object | - | entries:integer/int64; errors:array<string>; valid:boolean |
| `RestoreRequest` | object | - | expectedVersion:integer/int64 |
| `ResultAbilityRadarResponse` | object | - | code:integer/int32; data:AbilityRadarResponse; error:string; message:string; success:boolean |
| `ResultAgentDefinition` | object | - | code:integer/int32; data:AgentDefinition; error:string; message:string; success:boolean |
| `ResultAgentDetailVO` | object | - | code:integer/int32; data:AgentDetailVO; error:string; message:string; success:boolean |
| `ResultAgentVO` | object | - | code:integer/int32; data:AgentVO; error:string; message:string; success:boolean |
| `ResultAgentVersionDetailVO` | object | - | code:integer/int32; data:AgentVersionDetailVO; error:string; message:string; success:boolean |
| `ResultAgentVersionVO` | object | - | code:integer/int32; data:AgentVersionVO; error:string; message:string; success:boolean |
| `ResultAiApp` | object | - | code:integer/int32; data:AiApp; error:string; message:string; success:boolean |
| `ResultAiAppPreview` | object | - | code:integer/int32; data:AiAppPreview; error:string; message:string; success:boolean |
| `ResultAiAppVersion` | object | - | code:integer/int32; data:AiAppVersion; error:string; message:string; success:boolean |
| `ResultAiModelVO` | object | - | code:integer/int32; data:AiModelVO; error:string; message:string; success:boolean |
| `ResultAiPlatformResponse` | object | - | code:integer/int32; data:AiPlatformResponse; error:string; message:string; success:boolean |
| `ResultAiPlatformVO` | object | - | code:integer/int32; data:AiPlatformVO; error:string; message:string; success:boolean |
| `ResultAiRun` | object | - | code:integer/int32; data:AiRun; error:string; message:string; success:boolean |
| `ResultAuthCodeResponse` | object | - | code:integer/int32; data:AuthCodeResponse; error:string; message:string; success:boolean |
| `ResultBackupResult` | object | - | code:integer/int32; data:BackupResult; error:string; message:string; success:boolean |
| `ResultBoolean` | object | - | code:integer/int32; data:boolean; error:string; message:string; success:boolean |
| `ResultCaptchaVO` | object | - | code:integer/int32; data:CaptchaVO; error:string; message:string; success:boolean |
| `ResultCaseView` | object | - | code:integer/int32; data:CaseView; error:string; message:string; success:boolean |
| `ResultChapterResponse` | object | - | code:integer/int32; data:ChapterResponse; error:string; message:string; success:boolean |
| `ResultCharacterAsset` | object | - | code:integer/int32; data:CharacterAsset; error:string; message:string; success:boolean |
| `ResultConversation` | object | - | code:integer/int32; data:Conversation; error:string; message:string; success:boolean |
| `ResultConversationMessage` | object | - | code:integer/int32; data:ConversationMessage; error:string; message:string; success:boolean |
| `ResultCourseResponse` | object | - | code:integer/int32; data:CourseResponse; error:string; message:string; success:boolean |
| `ResultDictVO` | object | - | code:integer/int32; data:DictVO; error:string; message:string; success:boolean |
| `ResultDifficultyScaleView` | object | - | code:integer/int32; data:DifficultyScaleView; error:string; message:string; success:boolean |
| `ResultDocumentView` | object | - | code:integer/int32; data:DocumentView; error:string; message:string; success:boolean |
| `ResultEvalCase` | object | - | code:integer/int32; data:EvalCase; error:string; message:string; success:boolean |
| `ResultEvalDataset` | object | - | code:integer/int32; data:EvalDataset; error:string; message:string; success:boolean |
| `ResultEvalRun` | object | - | code:integer/int32; data:EvalRun; error:string; message:string; success:boolean |
| `ResultExamResponse` | object | - | code:integer/int32; data:ExamResponse; error:string; message:string; success:boolean |
| `ResultFileView` | object | - | code:integer/int32; data:FileView; error:string; message:string; success:boolean |
| `ResultGenerationRun` | object | - | code:integer/int32; data:GenerationRun; error:string; message:string; success:boolean |
| `ResultGraphValidationVO` | object | - | code:integer/int32; data:GraphValidationVO; error:string; message:string; success:boolean |
| `ResultGroupChatAsset` | object | - | code:integer/int32; data:GroupChatAsset; error:string; message:string; success:boolean |
| `ResultGroupTurnRun` | object | - | code:integer/int32; data:GroupTurnRun; error:string; message:string; success:boolean |
| `ResultHybridRecommendResponse` | object | - | code:integer/int32; data:HybridRecommendResponse; error:string; message:string; success:boolean |
| `ResultImageResult` | object | - | code:integer/int32; data:ImageResult; error:string; message:string; success:boolean |
| `ResultIntentDefVO` | object | - | code:integer/int32; data:IntentDefVO; error:string; message:string; success:boolean |
| `ResultJobView` | object | - | code:integer/int32; data:JobView; error:string; message:string; success:boolean |
| `ResultKnowledgeGraphResponse` | object | - | code:integer/int32; data:KnowledgeGraphResponse; error:string; message:string; success:boolean |
| `ResultListAgentDefinition` | object | - | code:integer/int32; data:array<AgentDefinition>; error:string; message:string; success:boolean |
| `ResultListAgentVersionVO` | object | - | code:integer/int32; data:array<AgentVersionVO>; error:string; message:string; success:boolean |
| `ResultListAiApp` | object | - | code:integer/int32; data:array<AiApp>; error:string; message:string; success:boolean |
| `ResultListAiAppVersion` | object | - | code:integer/int32; data:array<AiAppVersion>; error:string; message:string; success:boolean |
| `ResultListAiModelResponse` | object | - | code:integer/int32; data:array<AiModelResponse>; error:string; message:string; success:boolean |
| `ResultListAiModelVO` | object | - | code:integer/int32; data:array<AiModelVO>; error:string; message:string; success:boolean |
| `ResultListAiPlatformVO` | object | - | code:integer/int32; data:array<AiPlatformVO>; error:string; message:string; success:boolean |
| `ResultListAiRun` | object | - | code:integer/int32; data:array<AiRun>; error:string; message:string; success:boolean |
| `ResultListAiRunEvent` | object | - | code:integer/int32; data:array<AiRunEvent>; error:string; message:string; success:boolean |
| `ResultListAuthCodeOptionVO` | object | - | code:integer/int32; data:array<AuthCodeOptionVO>; error:string; message:string; success:boolean |
| `ResultListChapterResponse` | object | - | code:integer/int32; data:array<ChapterResponse>; error:string; message:string; success:boolean |
| `ResultListCharacterAsset` | object | - | code:integer/int32; data:array<CharacterAsset>; error:string; message:string; success:boolean |
| `ResultListConversation` | object | - | code:integer/int32; data:array<Conversation>; error:string; message:string; success:boolean |
| `ResultListConversationMessage` | object | - | code:integer/int32; data:array<ConversationMessage>; error:string; message:string; success:boolean |
| `ResultListCourseResponse` | object | - | code:integer/int32; data:array<CourseResponse>; error:string; message:string; success:boolean |
| `ResultListDailyTaskResponse` | object | - | code:integer/int32; data:array<DailyTaskResponse>; error:string; message:string; success:boolean |
| `ResultListDictVO` | object | - | code:integer/int32; data:array<DictVO>; error:string; message:string; success:boolean |
| `ResultListDocumentRelationView` | object | - | code:integer/int32; data:array<DocumentRelationView>; error:string; message:string; success:boolean |
| `ResultListDocumentSummary` | object | - | code:integer/int32; data:array<DocumentSummary>; error:string; message:string; success:boolean |
| `ResultListEvalCase` | object | - | code:integer/int32; data:array<EvalCase>; error:string; message:string; success:boolean |
| `ResultListEvalResult` | object | - | code:integer/int32; data:array<EvalResult>; error:string; message:string; success:boolean |
| `ResultListExamResponse` | object | - | code:integer/int32; data:array<ExamResponse>; error:string; message:string; success:boolean |
| `ResultListFileView` | object | - | code:integer/int32; data:array<FileView>; error:string; message:string; success:boolean |
| `ResultListGroupChatAsset` | object | - | code:integer/int32; data:array<GroupChatAsset>; error:string; message:string; success:boolean |
| `ResultListIdNameOptionVO` | object | - | code:integer/int32; data:array<IdNameOptionVO>; error:string; message:string; success:boolean |
| `ResultListKnowledgeRecommendResponse` | object | - | code:integer/int32; data:array<KnowledgeRecommendResponse>; error:string; message:string; success:boolean |
| `ResultListLong` | object | - | code:integer/int32; data:array<integer/int64>; error:string; message:string; success:boolean |
| `ResultListLorebookAsset` | object | - | code:integer/int32; data:array<LorebookAsset>; error:string; message:string; success:boolean |
| `ResultListMapStringObject` | object | - | code:integer/int32; data:array<object>; error:string; message:string; success:boolean |
| `ResultListMcpToolDescriptor` | object | - | code:integer/int32; data:array<McpToolDescriptor>; error:string; message:string; success:boolean |
| `ResultListMemberView` | object | - | code:integer/int32; data:array<MemberView>; error:string; message:string; success:boolean |
| `ResultListMemoryEdge` | object | - | code:integer/int32; data:array<MemoryEdge>; error:string; message:string; success:boolean |
| `ResultListMenuVO` | object | - | code:integer/int32; data:array<MenuVO>; error:string; message:string; success:boolean |
| `ResultListModelProviderCapabilities` | object | - | code:integer/int32; data:array<ModelProviderCapabilities>; error:string; message:string; success:boolean |
| `ResultListModelRoutePolicy` | object | - | code:integer/int32; data:array<ModelRoutePolicy>; error:string; message:string; success:boolean |
| `ResultListNodeTypeMetaVO` | object | - | code:integer/int32; data:array<NodeTypeMetaVO>; error:string; message:string; success:boolean |
| `ResultListPersonaAsset` | object | - | code:integer/int32; data:array<PersonaAsset>; error:string; message:string; success:boolean |
| `ResultListPluginInfoVO` | object | - | code:integer/int32; data:array<PluginInfoVO>; error:string; message:string; success:boolean |
| `ResultListPluginMarketEntry` | object | - | code:integer/int32; data:array<PluginMarketEntry>; error:string; message:string; success:boolean |
| `ResultListPromptTemplate` | object | - | code:integer/int32; data:array<PromptTemplate>; error:string; message:string; success:boolean |
| `ResultListPromptTemplateVersion` | object | - | code:integer/int32; data:array<PromptTemplateVersion>; error:string; message:string; success:boolean |
| `ResultListQuestionRecommendResponse` | object | - | code:integer/int32; data:array<QuestionRecommendResponse>; error:string; message:string; success:boolean |
| `ResultListQuestionResponse` | object | - | code:integer/int32; data:array<QuestionResponse>; error:string; message:string; success:boolean |
| `ResultListRelationView` | object | - | code:integer/int32; data:array<RelationView>; error:string; message:string; success:boolean |
| `ResultListResourceRecommendResponse` | object | - | code:integer/int32; data:array<ResourceRecommendResponse>; error:string; message:string; success:boolean |
| `ResultListResourceResponse` | object | - | code:integer/int32; data:array<ResourceResponse>; error:string; message:string; success:boolean |
| `ResultListReviewTaskResponse` | object | - | code:integer/int32; data:array<ReviewTaskResponse>; error:string; message:string; success:boolean |
| `ResultListRoleVO` | object | - | code:integer/int32; data:array<RoleVO>; error:string; message:string; success:boolean |
| `ResultListRouteMenuVO` | object | - | code:integer/int32; data:array<RouteMenuVO>; error:string; message:string; success:boolean |
| `ResultListSpaceView` | object | - | code:integer/int32; data:array<SpaceView>; error:string; message:string; success:boolean |
| `ResultListString` | object | - | code:integer/int32; data:array<string>; error:string; message:string; success:boolean |
| `ResultListStudyPlanResponse` | object | - | code:integer/int32; data:array<StudyPlanResponse>; error:string; message:string; success:boolean |
| `ResultListStudyRecordResponse` | object | - | code:integer/int32; data:array<StudyRecordResponse>; error:string; message:string; success:boolean |
| `ResultListSubjectResponse` | object | - | code:integer/int32; data:array<SubjectResponse>; error:string; message:string; success:boolean |
| `ResultListTenantInfoVO` | object | - | code:integer/int32; data:array<TenantInfoVO>; error:string; message:string; success:boolean |
| `ResultListTenantVO` | object | - | code:integer/int32; data:array<TenantVO>; error:string; message:string; success:boolean |
| `ResultListTextbookResponse` | object | - | code:integer/int32; data:array<TextbookResponse>; error:string; message:string; success:boolean |
| `ResultListTimezoneOptionVO` | object | - | code:integer/int32; data:array<TimezoneOptionVO>; error:string; message:string; success:boolean |
| `ResultListToolApproval` | object | - | code:integer/int32; data:array<ToolApproval>; error:string; message:string; success:boolean |
| `ResultListUserTenantAssignmentVO` | object | - | code:integer/int32; data:array<UserTenantAssignmentVO>; error:string; message:string; success:boolean |
| `ResultListVersionView` | object | - | code:integer/int32; data:array<VersionView>; error:string; message:string; success:boolean |
| `ResultListWeakPointResponse` | object | - | code:integer/int32; data:array<WeakPointResponse>; error:string; message:string; success:boolean |
| `ResultListWrongQuestionResponse` | object | - | code:integer/int32; data:array<WrongQuestionResponse>; error:string; message:string; success:boolean |
| `ResultLoginResponseVO` | object | - | code:integer/int32; data:LoginResponseVO; error:string; message:string; success:boolean |
| `ResultLong` | object | - | code:integer/int32; data:integer/int64; error:string; message:string; success:boolean |
| `ResultLorebookAsset` | object | - | code:integer/int32; data:LorebookAsset; error:string; message:string; success:boolean |
| `ResultMapStringObject` | object | - | code:integer/int32; data:object; error:string; message:string; success:boolean |
| `ResultMapStringString` | object | - | code:integer/int32; data:object; error:string; message:string; success:boolean |
| `ResultMcpToolDescriptor` | object | - | code:integer/int32; data:McpToolDescriptor; error:string; message:string; success:boolean |
| `ResultMemoryEvent` | object | - | code:integer/int32; data:MemoryEvent; error:string; message:string; success:boolean |
| `ResultMemoryRetrievalResult` | object | - | code:integer/int32; data:MemoryRetrievalResult; error:string; message:string; success:boolean |
| `ResultMemoryRetrievalTrace` | object | - | code:integer/int32; data:MemoryRetrievalTrace; error:string; message:string; success:boolean |
| `ResultModelProviderCapabilities` | object | - | code:integer/int32; data:ModelProviderCapabilities; error:string; message:string; success:boolean |
| `ResultModelRoutePolicy` | object | - | code:integer/int32; data:ModelRoutePolicy; error:string; message:string; success:boolean |
| `ResultNodeTypeMetaVO` | object | - | code:integer/int32; data:NodeTypeMetaVO; error:string; message:string; success:boolean |
| `ResultObject` | object | - | code:integer/int32; data:-; error:string; message:string; success:boolean |
| `ResultOverviewResponse` | object | - | code:integer/int32; data:OverviewResponse; error:string; message:string; success:boolean |
| `ResultPageDataAgentVO` | object | - | code:integer/int32; data:PageDataAgentVO; error:string; message:string; success:boolean |
| `ResultPageDataAiModelVO` | object | - | code:integer/int32; data:PageDataAiModelVO; error:string; message:string; success:boolean |
| `ResultPageDataAiPlatformVO` | object | - | code:integer/int32; data:PageDataAiPlatformVO; error:string; message:string; success:boolean |
| `ResultPageDataAuthCodeOptionVO` | object | - | code:integer/int32; data:PageDataAuthCodeOptionVO; error:string; message:string; success:boolean |
| `ResultPageDataCaseView` | object | - | code:integer/int32; data:PageDataCaseView; error:string; message:string; success:boolean |
| `ResultPageDataCourseResponse` | object | - | code:integer/int32; data:PageDataCourseResponse; error:string; message:string; success:boolean |
| `ResultPageDataDictVO` | object | - | code:integer/int32; data:PageDataDictVO; error:string; message:string; success:boolean |
| `ResultPageDataDocumentView` | object | - | code:integer/int32; data:PageDataDocumentView; error:string; message:string; success:boolean |
| `ResultPageDataExamResponse` | object | - | code:integer/int32; data:PageDataExamResponse; error:string; message:string; success:boolean |
| `ResultPageDataIntentDefVO` | object | - | code:integer/int32; data:PageDataIntentDefVO; error:string; message:string; success:boolean |
| `ResultPageDataJobView` | object | - | code:integer/int32; data:PageDataJobView; error:string; message:string; success:boolean |
| `ResultPageDataKnowledgeAuditResponse` | object | - | code:integer/int32; data:PageDataKnowledgeAuditResponse; error:string; message:string; success:boolean |
| `ResultPageDataMenuVO` | object | - | code:integer/int32; data:PageDataMenuVO; error:string; message:string; success:boolean |
| `ResultPageDataPointView` | object | - | code:integer/int32; data:PageDataPointView; error:string; message:string; success:boolean |
| `ResultPageDataQuestionResponse` | object | - | code:integer/int32; data:PageDataQuestionResponse; error:string; message:string; success:boolean |
| `ResultPageDataResourceResponse` | object | - | code:integer/int32; data:PageDataResourceResponse; error:string; message:string; success:boolean |
| `ResultPageDataRoleVO` | object | - | code:integer/int32; data:PageDataRoleVO; error:string; message:string; success:boolean |
| `ResultPageDataSpaceView` | object | - | code:integer/int32; data:PageDataSpaceView; error:string; message:string; success:boolean |
| `ResultPageDataStudentResponse` | object | - | code:integer/int32; data:PageDataStudentResponse; error:string; message:string; success:boolean |
| `ResultPageDataSubjectResponse` | object | - | code:integer/int32; data:PageDataSubjectResponse; error:string; message:string; success:boolean |
| `ResultPageDataTenantVO` | object | - | code:integer/int32; data:PageDataTenantVO; error:string; message:string; success:boolean |
| `ResultPageDataTextbookResponse` | object | - | code:integer/int32; data:PageDataTextbookResponse; error:string; message:string; success:boolean |
| `ResultPageDataUserVO` | object | - | code:integer/int32; data:PageDataUserVO; error:string; message:string; success:boolean |
| `ResultPersonaAsset` | object | - | code:integer/int32; data:PersonaAsset; error:string; message:string; success:boolean |
| `ResultPluginMarketEntry` | object | - | code:integer/int32; data:PluginMarketEntry; error:string; message:string; success:boolean |
| `ResultPointView` | object | - | code:integer/int32; data:PointView; error:string; message:string; success:boolean |
| `ResultPreview` | object | - | code:integer/int32; data:Preview; error:string; message:string; success:boolean |
| `ResultPromptDiff` | object | - | code:integer/int32; data:PromptDiff; error:string; message:string; success:boolean |
| `ResultPromptPreview` | object | - | code:integer/int32; data:PromptPreview; error:string; message:string; success:boolean |
| `ResultPromptTemplate` | object | - | code:integer/int32; data:PromptTemplate; error:string; message:string; success:boolean |
| `ResultPromptTemplateVersion` | object | - | code:integer/int32; data:PromptTemplateVersion; error:string; message:string; success:boolean |
| `ResultPromptTestRun` | object | - | code:integer/int32; data:PromptTestRun; error:string; message:string; success:boolean |
| `ResultProviderHealth` | object | - | code:integer/int32; data:ProviderHealth; error:string; message:string; success:boolean |
| `ResultQuestionResponse` | object | - | code:integer/int32; data:QuestionResponse; error:string; message:string; success:boolean |
| `ResultResourceResponse` | object | - | code:integer/int32; data:ResourceResponse; error:string; message:string; success:boolean |
| `ResultRestoreCheckResult` | object | - | code:integer/int32; data:RestoreCheckResult; error:string; message:string; success:boolean |
| `ResultReviewTaskResponse` | object | - | code:integer/int32; data:ReviewTaskResponse; error:string; message:string; success:boolean |
| `ResultRoleVO` | object | - | code:integer/int32; data:RoleVO; error:string; message:string; success:boolean |
| `ResultRunResult` | object | - | code:integer/int32; data:RunResult; error:string; message:string; success:boolean |
| `ResultSearchResponse` | object | - | code:integer/int32; data:SearchResponse; error:string; message:string; success:boolean |
| `ResultSetString` | object | - | code:integer/int32; data:array<string>; error:string; message:string; success:boolean |
| `ResultSpaceView` | object | - | code:integer/int32; data:SpaceView; error:string; message:string; success:boolean |
| `ResultString` | object | - | code:integer/int32; data:string; error:string; message:string; success:boolean |
| `ResultStudentResponse` | object | - | code:integer/int32; data:StudentResponse; error:string; message:string; success:boolean |
| `ResultStudyPlanResponse` | object | - | code:integer/int32; data:StudyPlanResponse; error:string; message:string; success:boolean |
| `ResultStudyRecordResponse` | object | - | code:integer/int32; data:StudyRecordResponse; error:string; message:string; success:boolean |
| `ResultSubjectResponse` | object | - | code:integer/int32; data:SubjectResponse; error:string; message:string; success:boolean |
| `ResultSwitchContextResponse` | object | - | code:integer/int32; data:SwitchContextResponse; error:string; message:string; success:boolean |
| `ResultTenantVO` | object | - | code:integer/int32; data:TenantVO; error:string; message:string; success:boolean |
| `ResultTextbookResponse` | object | - | code:integer/int32; data:TextbookResponse; error:string; message:string; success:boolean |
| `ResultToolApproval` | object | - | code:integer/int32; data:ToolApproval; error:string; message:string; success:boolean |
| `ResultTrendResponse` | object | - | code:integer/int32; data:TrendResponse; error:string; message:string; success:boolean |
| `ResultTurnDecision` | object | - | code:integer/int32; data:TurnDecision; error:string; message:string; success:boolean |
| `ResultUploadResult` | object | - | code:integer/int32; data:UploadResult; error:string; message:string; success:boolean |
| `ResultUploadSession` | object | - | code:integer/int32; data:UploadSession; error:string; message:string; success:boolean |
| `ResultUserVO` | object | - | code:integer/int32; data:UserVO; error:string; message:string; success:boolean |
| `ResultValidateCaptchaResponse` | object | - | code:integer/int32; data:ValidateCaptchaResponse; error:string; message:string; success:boolean |
| `ResultVisionResult` | object | - | code:integer/int32; data:VisionResult; error:string; message:string; success:boolean |
| `ResultVoid` | object | - | code:integer/int32; data:-; error:string; message:string; success:boolean |
| `ResultWrongQuestionResponse` | object | - | code:integer/int32; data:WrongQuestionResponse; error:string; message:string; success:boolean |
| `RetryRequest` | object | - | model:string; platform:string |
| `ReviewRequest` | object | knowledgeId, studentId | completedAt:string/date-time; id:integer/int64; knowledgeId*:integer/int64; resultScore:number/double; reviewDate:string/date; reviewRound:integer/int32; status:integer/int32; studentId*:integer/int64 |
| `ReviewTaskResponse` | object | - | completedAt:string/date-time; id:integer/int64; knowledgeId:integer/int64; knowledgeName:string; resultScore:number/double; reviewDate:string; reviewRound:integer/int32; status:integer/int32; statusDesc:string; studentId:integer/int64 |
| `RolePageRequest` | object | - | filterConditions:array<FilterCondition>; isAsc:string; name:string; orderByColumn:string; orderFields:array<OrderField>; pageNum:integer/int32; pageSize:integer/int32 |
| `RoleRequest` | object | code, name, tenantId | code*:string; name*:string; permissions:array<integer/int64>; remark:string; status:string; tenantId*:integer/int64 |
| `RoleVO` | object | - | code:string; createTime:string/date-time; delFlag:integer/int32; id:integer/int64; name:string; permissions:array<integer/int64>; remark:string; status:integer/int32; tenantId:integer/int64; updateTime:string/date-time |
| `RouteMenuVO` | object | - | children:array<RouteMenuVO>; component:string; icon:string; id:integer/int64; meta:MetaVO; name:string; path:string; pid:integer/int64; redirect:string; status:integer/int32; type:string |
| `RouteRequest` | object | - | fallbackOnError:boolean; maxTokens:integer/int64; name:string; orderedModels:array<string>; timeoutMs:integer/int32 |
| `RunRequest` | object | - | spaceId:integer/int64; topK:integer/int32 |
| `RunResult` | object | - | caseCount:integer/int32; cases:array<CaseResult>; citationAccuracy:number/double; mrr:number/double; recallAtK:number/double; spaceId:integer/int64; topK:integer/int32 |
| `RuntimeStatus` | object | - | appHome:string; health:string; healthDetail:string; managed:boolean; mode:string; pid:integer/int64; port:integer/int32; uptimeMillis:integer/int64; version:string |
| `SearchRequest` | object | query, spaceId | mode:string; query*:string; rerank:boolean; spaceId*:integer/int64; threshold:number/double; topK:integer/int32 |
| `SearchResponse` | object | - | hits:array<HybridHit>; mode:string; spaceId:integer/int64 |
| `SecretChange` | object | - | action:string; value:string |
| `ServerSentEventAiRunEvent` | - | - | - |
| `ServerSentEventGenerationEvent` | - | - | - |
| `SessionResponse` | object | - | authenticated:boolean; csrfToken:string |
| `SetTimezoneRequest` | object | timezone | timezone*:string |
| `SpaceView` | object | - | accessMode:string; activeIndexVersion:integer/int64; bindingMode:string; chunkOverlap:integer/int32; chunkSize:integer/int32; chunkStrategy:string; code:string; createTime:string/date-time; description:string; difficultyScaleId:integer/int64; domainCode:string; embeddingProfile:string; id:integer/int64; name:string; rerankProfile:string; reviewMode:string; status:integer/int32; updateTime:string/date-time |
| `StateGraphAgentState` | object | - | channels:object; stateFactory:AgentStateFactoryAgentState; stateSerializer:StateSerializerAgentState |
| `StateSerializerAgentState` | - | - | - |
| `StudentRequest` | object | grade, name, userId | className:string; grade*:integer/int32; gradeLevel:string; id:integer/int64; name*:string; school:string; status:integer/int32; studentNo:string; userId*:integer/int64 |
| `StudentResponse` | object | - | className:string; gender:integer/int32; grade:integer/int32; gradeLevel:string; id:integer/int64; name:string; school:string; studentNo:string; userId:integer/int64 |
| `StudyPlanRequest` | object | studentId | endDate:string/date; id:integer/int64; name:string; startDate:string/date; status:string; studentId*:integer/int64 |
| `StudyPlanResponse` | object | - | completedItems:integer/int32; endDate:string; id:integer/int64; items:array<DailyTaskResponse>; name:string; startDate:string; status:integer/int32; statusDesc:string; studentId:integer/int64; totalItems:integer/int32 |
| `StudyRecordRequest` | object | knowledgeId, recordType, studentId | accuracy:number/double; durationSec:integer/int32; id:integer/int64; knowledgeId*:integer/int64; questionId:integer/int64; recordType*:string; score:number/double; studentId*:integer/int64 |
| `StudyRecordResponse` | object | - | accuracy:number/double; createTime:string/date-time; durationSec:integer/int32; id:integer/int64; knowledgeId:integer/int64; questionId:integer/int64; recordType:string; score:number/double; studentId:integer/int64 |
| `SubjectRequest` | object | code, name | code*:string; description:string; gradeLevel:string; icon:string; id:integer/int64; name*:string; sortOrder:integer/int32; status:integer/int32 |
| `SubjectResponse` | object | - | code:string; gradeLevel:string; icon:string; id:integer/int64; name:string; sortOrder:integer/int32 |
| `SwitchContextResponse` | object | - | tenants:array<TenantInfoVO>; userInfo:UserVO |
| `SwitchRoleRequest` | object | roleId | roleId*:integer/int64 |
| `SwitchTenantRequest` | object | tenantId | tenantId*:integer/int64 |
| `TenantContextVO` | object | - | roleCode:string; tenantId:integer/int64; tenantName:string |
| `TenantId` | object | - | value:integer/int64 |
| `TenantInfoVO` | object | - | code:string; id:integer/int64; name:string; pathName:string |
| `TenantPageRequest` | object | - | code:string; filterConditions:array<FilterCondition>; isAsc:string; name:string; orderByColumn:string; orderFields:array<OrderField>; pageNum:integer/int32; pageSize:integer/int32; status:integer/int32 |
| `TenantRequest` | object | code, name | address:string; adminPassword:string; adminRoleName:string; adminUsername:string; authCodeIds:array<integer/int64>; code*:string; contactName:string; contactPhone:string; domain:string; email:string; intro:string; leader:string; menuIds:array<integer/int64>; name*:string; order:integer/int32; parentId:integer/int64; remark:string; status:string |
| `TenantVO` | object | - | address:string; children:array<TenantVO>; code:string; contactName:string; contactPhone:string; createTime:string/date-time; domain:string; email:string; id:integer/int64; intro:string; leader:string; name:string; order:integer/int32; parentId:integer/int64; phone:string; remark:string; status:integer/int32; updateTime:string/date-time |
| `TestRequest` | object | - | requiredFeatures:array<string> |
| `TextbookRequest` | object | grade, name, publisher, subjectCode | author:string; edition:string; grade*:integer/int32; id:integer/int64; isbn:string; name*:string; publisher*:string; status:integer/int32; subjectCode*:string |
| `TextbookResponse` | object | - | grade:integer/int32; id:integer/int64; isbn:string; name:string; publisher:string; subjectCode:string |
| `TimezoneOptionVO` | object | - | label:string; value:string |
| `ToolApproval` | object | - | argumentsRedacted:string; createdAt:string/date-time; decidedAt:string/date-time; expiresAt:string/date-time; id:string; ownerUserId:integer/int64; runId:string; status:string(PENDING,APPROVED,REJECTED,EXPIRED); tenantId:integer/int64; toolName:string |
| `TranslateRequest` | object | - | provider:string; sourceLanguage:string; targetLanguage:string; text:string |
| `TrendResponse` | object | - | dates:array<string>; values:array<number/double> |
| `TtsRequest` | object | - | format:string; provider:string; text:string; voice:string |
| `TurnDecision` | object | - | exhausted:boolean; participant:Participant; reason:string; remainingTokens:integer/int32; remainingTurns:integer/int32 |
| `TurnRequest` | object | - | completedSpeakerIds:array<string>; consumedTokens:integer/int32; requestedSpeakerId:string |
| `TurnRunRequest` | object | - | completedSpeakerIds:array<string>; consumedTokens:integer/int32; content:string; conversationId:string; model:string; platform:string; requestedSpeakerId:string |
| `UpdateConversationRequest` | object | - | status:string; title:string |
| `UpdatePointRequest` | object | - | category:string; description:string; difficultyLevel:integer/int32; name:string; tags:string |
| `UpdateSpaceRequest` | object | - | accessMode:string; bindingMode:string; chunkOverlap:integer/int32; chunkSize:integer/int32; chunkStrategy:string; description:string; difficultyScaleId:integer/int64; domainCode:string; embeddingProfile:string; name:string; rerankProfile:string; reviewMode:string; status:integer/int32 |
| `UploadResult` | object | - | document:DocumentView; duplicate:boolean; jobId:integer/int64; versionId:integer/int64 |
| `UploadSession` | object | - | chunkSize:integer/int32; fileName:string; sessionId:string; size:integer/int64; spaceId:integer/int64; totalChunks:integer/int32; uploadedChunks:array<integer/int32> |
| `UserId` | object | - | value:integer/int64 |
| `UserPageRequest` | object | - | filterConditions:array<FilterCondition>; isAsc:string; orderByColumn:string; orderFields:array<OrderField>; pageNum:integer/int32; pageSize:integer/int32; username:string |
| `UserRequest` | object | tenantId, username | address:string; avatar:string; email:string; gender:string; nickName:string; password:string; phone:string; postIds:array<integer/int64>; remark:string; roleIds:array<integer/int64>; status:string; tenantId*:integer/int64; username*:string |
| `UserTenantAssignmentVO` | object | - | roleCode:string; roleId:integer/int64; roleName:string; tenantId:integer/int64; tenantName:string |
| `UserTenantRoleRequest` | object | roleId, tenantId | roleId*:integer/int64; tenantId*:integer/int64 |
| `UserVO` | object | - | address:string; avatar:string; createTime:string/date-time; currentRole:RoleVO; currentTenantId:integer/int64; delFlag:integer/int32; email:string; extInfo:string; gender:string; homeTenantId:integer/int64; id:integer/int64; nickName:string; phone:string; remark:string; roleIds:array<integer/int64>; roles:array<RoleVO>; status:string; subTenants:array<TenantContextVO>; switchMode:string; tenants:array<TenantInfoVO>; updateTime:string/date-time; username:string |
| `ValidateCaptchaRequest` | object | - | code:string; key:string |
| `ValidateCaptchaResponse` | object | - | message:string; success:boolean |
| `ValidationResponse` | object | - | issues:array<string>; valid:boolean |
| `VersionRequest` | object | versionNumber | copyFromVersionId:integer/int64; description:string; versionNumber*:string |
| `VersionView` | object | - | checksum:string; createTime:string/date-time; documentId:integer/int64; fileSize:integer/int64; id:integer/int64; lifecycleStatus:string; mimeType:string; modelProfile:string; objectKey:string; parseStatus:string; publishedAt:string/date-time; spaceId:integer/int64; title:string; versionNo:integer/int32 |
| `VisionResult` | object | - | labels:array<string>; text:string |
| `WeakPointResponse` | object | - | knowledgeId:integer/int64; knowledgeName:string; mastery:number/double |
| `WrongQuestionRequest` | object | questionId, studentId | correctTimes:integer/int32; id:integer/int64; knowledgeId:integer/int64; questionId*:integer/int64; status:integer/int32; studentAnswer:string; studentId*:integer/int64 |
| `WrongQuestionResponse` | object | - | correctAnswer:string; correctTimes:integer/int32; id:integer/int64; knowledgeId:integer/int64; questionId:integer/int64; questionTitle:string; studentAnswer:string; studentId:integer/int64 |

## 维护与验证

```powershell
python scripts/docs/generate_reference_docs.py --openapi-url http://127.0.0.1:9000/v3/api-docs
```

生成后应运行前端 `pnpm run test:contract`，确保前端方法与路径仍被该 OpenAPI 契约覆盖。

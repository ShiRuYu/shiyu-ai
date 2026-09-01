# API 接口参考

> 本文档由 `scripts/docs/generate_reference_docs.py` 从 SpringDoc OpenAPI 自动生成。
> 生成源：`E:\Dev\shiyu\shiyu-ui\tests\contracts\shiyu-ai-openapi.json`；OpenAPI：`3.1.0`；服务版本：`0.1`。

## 契约约定

- 浏览器开发环境使用 `/api` 作为 Vite 代理前缀；后端控制器路径本身不包含 `/api`。
- 除登录、注册、验证码等公开入口外，请求使用 `Authorization: Bearer <accessToken>`。
- 普通 JSON 接口通常返回 `Result<T>`；流式接口按 OpenAPI 标注返回 SSE 或二进制内容。
- `requestBody` 与响应栏保留 OpenAPI schema 名称，具体字段见“组件模型”。

## 接口清单

### Agent Definition

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/agent/agents/create` | Create | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:AgentRequest | 200=*/*:ResultAgentVO |
| POST | `/api/agent/agents/delete` | Delete | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| POST | `/api/agent/agents/delete/by-agent-id` | Delete Agent | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string | 200=*/*:ResultVoid |
| GET | `/api/agent/agents/detail` | Get by Id | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultAgentDetailVO |
| GET | `/api/agent/agents/detail/by-agent-id` | Get Agent | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string | 200=*/*:ResultAgentDefinition |
| GET | `/api/agent/agents/list` | List Agents | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListAgentDefinition |
| GET | `/api/agent/agents/node-types` | Get Node Types | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListNodeTypeMetaVO |
| GET | `/api/agent/agents/node-types/detail` | Get Node Type | 登录态；细粒度权限见权限矩阵 | nodeType[query,必填]:string | 200=*/*:ResultNodeTypeMetaVO |
| GET | `/api/agent/agents/options` | List All Options | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListIdNameOptionVO |
| GET | `/api/agent/agents/page` | Get Page | 登录态；细粒度权限见权限矩阵 | pageNo[query,可选]:integer/int32; pageSize[query,可选]:integer/int32; name[query,可选]:string; status[query,可选]:integer/int32 | 200=*/*:ResultPageDataAgentVO |
| POST | `/api/agent/agents/register` | Register Agent | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:RegisterAgentRequest | 200=*/*:ResultMapStringObject |
| POST | `/api/agent/agents/status` | Update Status | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; status[query,必填]:integer/int32 | 200=*/*:ResultVoid |
| POST | `/api/agent/agents/update` | Update | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:AgentRequest | 200=*/*:ResultAgentVO |
| POST | `/api/agent/agents/version/switch` | Switch Version | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; version[query,必填]:string | 200=*/*:ResultVoid |

### Agent Version

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/agent/versions/activate` | Activate | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| POST | `/api/agent/versions/archive` | Archive | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| POST | `/api/agent/versions/copy` | Copy | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; body[必填]=application/json:VersionRequest | 200=*/*:ResultAgentVersionVO |
| POST | `/api/agent/versions/create` | Create Version | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; body[必填]=application/json:VersionRequest | 200=*/*:ResultAgentVersionVO |
| POST | `/api/agent/versions/delete` | Delete Version | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/agent/versions/detail` | Get Version Detail | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64 | 200=*/*:ResultAgentVersionDetailVO |
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
| GET | `/api/agent/versions/list` | Get Versions | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string | 200=*/*:ResultListAgentVersionVO |
| POST | `/api/agent/versions/publish` | Publish | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| POST | `/api/agent/versions/update` | Update Version | 登录态；细粒度权限见权限矩阵 | agentId[query,必填]:string; versionId[query,必填]:integer/int64; body[必填]=application/json:VersionRequest | 200=*/*:ResultAgentVersionVO |

### Ai Model

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/model/models/batch-delete` | Delete Batch | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:array<integer/int64> | 200=*/*:ResultVoid |
| POST | `/api/model/models/create` | Create | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:AiModelRequest | 200=*/*:ResultAiModelVO |
| POST | `/api/model/models/delete` | Delete | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/model/models/detail` | Get by Id | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultAiModelVO |
| GET | `/api/model/models/options` | Get Options | 登录态；细粒度权限见权限矩阵 | platformId[query,可选]:integer/int64 | 200=*/*:ResultListIdNameOptionVO |
| GET | `/api/model/models/page` | Get Page | 登录态；细粒度权限见权限矩阵 | platformId[query,可选]:integer/int64; pageNo[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataAiModelVO |
| GET | `/api/model/models/platform` | Get by Platform Id | 登录态；细粒度权限见权限矩阵 | platformId[query,必填]:integer/int64 | 200=*/*:ResultListAiModelVO |
| GET | `/api/model/models/platform/by-code` | Get by Platform Code | 登录态；细粒度权限见权限矩阵 | platformCode[query,必填]:string | 200=*/*:ResultListAiModelResponse |
| GET | `/api/model/models/platform/default` | Get Default By Platform Id | 登录态；细粒度权限见权限矩阵 | platformId[query,必填]:integer/int64 | 200=*/*:ResultAiModelVO |
| POST | `/api/model/models/set-default` | Set Default | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultAiModelVO |
| POST | `/api/model/models/update` | Update | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:AiModelRequest | 200=*/*:ResultAiModelVO |

### Ai Platform

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/model/providers/code` | Get by Code | 登录态；细粒度权限见权限矩阵 | code[query,必填]:string | 200=*/*:ResultAiPlatformResponse |
| POST | `/api/model/providers/create` | Create | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:AiPlatformRequest | 200=*/*:ResultAiPlatformVO |
| GET | `/api/model/providers/default` | Get Default | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultAiPlatformResponse |
| POST | `/api/model/providers/delete` | Delete | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/model/providers/detail` | Get by Id | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultAiPlatformResponse |
| GET | `/api/model/providers/enabled` | Get All Enabled | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListAiPlatformVO |
| GET | `/api/model/providers/options` | Get Options | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListIdNameOptionVO |
| GET | `/api/model/providers/page` | Get Page | 登录态；细粒度权限见权限矩阵 | name[query,可选]:string; code[query,可选]:string; pageNo[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataAiPlatformVO |
| POST | `/api/model/providers/reload` | Reload | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultVoid |
| POST | `/api/model/providers/set-default` | Set Default | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultAiPlatformVO |
| POST | `/api/model/providers/update` | Update | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:AiPlatformRequest | 200=*/*:ResultAiPlatformVO |

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
| POST | `/api/iam/auth-codes/create` | Create Auth Code | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:AuthCodeRequest | 200=*/*:ResultAuthCodeResponse |
| POST | `/api/iam/auth-codes/delete` | Delete Auth Code | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/iam/auth-codes/list` | List Auth Codes | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListAuthCodeOptionVO |
| GET | `/api/iam/auth-codes/options` | Auth code options | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListAuthCodeOptionVO |
| GET | `/api/iam/auth-codes/page` | Page Auth Codes | 登录态；细粒度权限见权限矩阵 | request[query,必填]:AuthCodePageRequest | 200=*/*:ResultPageDataAuthCodeOptionVO |
| POST | `/api/iam/auth-codes/roles/grant` | Grant role auth codes | 登录态；细粒度权限见权限矩阵 | roleId[query,必填]:integer/int64; tenantId[query,必填]:integer/int64; body[必填]=application/json:array<integer/int64> | 200=*/*:ResultVoid |
| GET | `/api/iam/auth-codes/roles/list` | List role auth codes | 登录态；细粒度权限见权限矩阵 | roleId[query,必填]:integer/int64; tenantId[query,必填]:integer/int64 | 200=*/*:ResultListString |
| POST | `/api/iam/auth-codes/roles/replace` | Replace role auth codes | 登录态；细粒度权限见权限矩阵 | roleId[query,必填]:integer/int64; tenantId[query,必填]:integer/int64; body[必填]=application/json:array<string> | 200=*/*:ResultVoid |
| POST | `/api/iam/auth-codes/roles/revoke` | Revoke role auth code | 登录态；细粒度权限见权限矩阵 | roleId[query,必填]:integer/int64; tenantId[query,必填]:integer/int64; authCodeId[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| POST | `/api/iam/auth-codes/update` | Update Auth Code | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:AuthCodeRequest | 200=*/*:ResultVoid |

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
| GET | `/api/conversation/chat-products/characters/{id}` | character | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultCharacterAsset |
| DELETE | `/api/conversation/chat-products/characters/{id}` | deleteCharacter | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultVoid |
| GET | `/api/conversation/chat-products/characters/{id}/png` | exportCharacter | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=image/png:string/byte |
| GET | `/api/conversation/chat-products/groups` | groups | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListGroupChatAsset |
| POST | `/api/conversation/chat-products/groups` | createGroup | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:GroupRequest | 200=*/*:ResultGroupChatAsset |
| GET | `/api/conversation/chat-products/groups/{id}` | group | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultGroupChatAsset |
| DELETE | `/api/conversation/chat-products/groups/{id}` | deleteGroup | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultVoid |
| POST | `/api/conversation/chat-products/groups/{id}/next-speaker` | nextSpeaker | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; body[可选]=application/json:TurnRequest | 200=*/*:ResultTurnDecision |
| POST | `/api/conversation/chat-products/groups/{id}/turn` | runTurn | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; body[必填]=application/json:TurnRunRequest | 200=*/*:ResultGroupTurnRun |
| GET | `/api/conversation/chat-products/lorebooks` | lorebooks | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListLorebookAsset |
| POST | `/api/conversation/chat-products/lorebooks` | createLorebook | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:LorebookEntry | 200=*/*:ResultLorebookAsset |
| GET | `/api/conversation/chat-products/lorebooks/{id}` | lorebook | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultLorebookAsset |
| DELETE | `/api/conversation/chat-products/lorebooks/{id}` | deleteLorebook | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultVoid |
| GET | `/api/conversation/chat-products/personas` | personas | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListPersonaAsset |
| POST | `/api/conversation/chat-products/personas` | createPersona | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:Persona | 200=*/*:ResultPersonaAsset |
| GET | `/api/conversation/chat-products/personas/{id}` | persona | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultPersonaAsset |
| DELETE | `/api/conversation/chat-products/personas/{id}` | deletePersona | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultVoid |
| POST | `/api/conversation/chat-products/prompt-studio/preview` | preview_1 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:PromptPreviewRequest | 200=*/*:ResultPromptPreview |
| GET | `/api/conversation/chat-products/prompt-studio/templates` | prompts | 登录态；细粒度权限见权限矩阵 | templateId[query,可选]:string | 200=*/*:ResultListPromptTemplateVersion |
| POST | `/api/conversation/chat-products/prompt-studio/templates` | createPrompt | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:PromptRequest | 200=*/*:ResultPromptTemplateVersion |
| POST | `/api/conversation/chat-products/prompt-studio/templates/{templateId}/diff` | diffPrompt | 登录态；细粒度权限见权限矩阵 | templateId[path,必填]:string; body[必填]=application/json:DiffRequest | 200=*/*:ResultPromptDiff |
| POST | `/api/conversation/chat-products/prompt-studio/templates/{templateId}/publish` | publishPrompt | 登录态；细粒度权限见权限矩阵 | templateId[path,必填]:string; body[必填]=application/json:PublishRequest | 200=*/*:ResultPromptTemplateVersion |
| POST | `/api/conversation/chat-products/prompt-studio/templates/{templateId}/test` | testPrompt | 登录态；细粒度权限见权限矩阵 | templateId[path,必填]:string; body[必填]=application/json:PromptTestRequest | 200=*/*:ResultPromptTestRun |

### Conversation Platform

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/conversation/conversations` | list_3 | 登录态；细粒度权限见权限矩阵 | limit[query,可选]:integer/int32; offset[query,可选]:integer/int32 | 200=*/*:ResultListConversation |
| POST | `/api/conversation/conversations` | create_19 | 登录态；细粒度权限见权限矩阵 | Idempotency-Key[header,可选]:string; body[必填]=application/json:CreateConversationRequest | 200=*/*:ResultConversation |
| POST | `/api/conversation/conversations/import` | importConversation | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ImportRequest/text/plain:ImportRequest | 200=*/*:ResultConversation |
| POST | `/api/conversation/conversations/import/preview` | importPreview | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ImportRequest | 200=*/*:ResultPreview |
| GET | `/api/conversation/conversations/{id}` | detail | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultConversation |
| DELETE | `/api/conversation/conversations/{id}` | delete_18 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultVoid |
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
| POST | `/api/iam/dicts/batch-delete` | Delete Dicts | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:array<integer/int64> | 200=*/*:ResultVoid |
| POST | `/api/iam/dicts/create` | Create Dict | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:DictRequest | 200=*/*:ResultDictVO |
| POST | `/api/iam/dicts/delete` | Delete Dict | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/iam/dicts/list` | Get Dict List | 登录态；细粒度权限见权限矩阵 | request[query,必填]:DictPageRequest | 200=*/*:ResultPageDataDictVO |
| GET | `/api/iam/dicts/type` | Get Dict By Type | 登录态；细粒度权限见权限矩阵 | dictType[query,必填]:string | 200=*/*:ResultListDictVO |
| POST | `/api/iam/dicts/update` | Update Dict | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:DictRequest | 200=*/*:ResultDictVO |

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
| GET | `/api/iam/files/config` | 获取文件存储配置 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultMapStringObject |
| GET | `/api/iam/files/download` | 下载文件 | 登录态；细粒度权限见权限矩阵 | key[query,必填]:string | 200=*/*:string/binary |
| GET | `/api/iam/files/list` | 获取文件列表 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListFileView |
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
| GET | `/api/iam/menus/all` | Get All Menus | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListRouteMenuVO |
| GET | `/api/iam/menus/children` | Get Menu Children | 登录态；细粒度权限见权限矩阵 | parentId[query,必填]:integer/int64 | 200=*/*:ResultListRouteMenuVO |
| POST | `/api/iam/menus/create` | Create Menu | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:MenuRequest | 200=*/*:ResultVoid |
| POST | `/api/iam/menus/delete` | Delete Menu | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/iam/menus/list` | Get System Menu List | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListMenuVO |
| GET | `/api/iam/menus/name-exists` | Is Menu Name Exists | 登录态；细粒度权限见权限矩阵 | name[query,必填]:string; id[query,可选]:integer/int64 | 200=*/*:ResultBoolean |
| GET | `/api/iam/menus/page` | Get System Menu Page | 登录态；细粒度权限见权限矩阵 | request[query,必填]:MenuPageRequest | 200=*/*:ResultPageDataMenuVO |
| GET | `/api/iam/menus/path-exists` | Is Menu Path Exists | 登录态；细粒度权限见权限矩阵 | path[query,必填]:string; id[query,可选]:integer/int64 | 200=*/*:ResultBoolean |
| GET | `/api/iam/menus/permissions` | Get Menu Permissions Tree | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListRouteMenuVO |
| GET | `/api/iam/menus/roots` | Get Menu Roots | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListRouteMenuVO |
| GET | `/api/iam/menus/tree` | Get All Tree | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListRouteMenuVO |
| POST | `/api/iam/menus/update` | Update Menu | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:MenuRequest | 200=*/*:ResultVoid |

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
| GET | `/api/education/chapter/children` | listByParentId | 登录态；细粒度权限见权限矩阵 | parentId[query,必填]:integer/int64 | 200=*/*:ResultListChapterResponse |
| POST | `/api/education/chapter/create` | create_17 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ChapterRequest | 200=*/*:ResultChapterResponse |
| POST | `/api/education/chapter/delete` | delete_15 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/chapter/detail` | getById_12 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultChapterResponse |
| POST | `/api/education/chapter/knowledge/bind` | replaceKnowledgeIds | 登录态；细粒度权限见权限矩阵 | chapterId[query,必填]:integer/int64; body[必填]=application/json:array<integer/int64> | 200=*/*:ResultVoid |
| GET | `/api/education/chapter/knowledge/list` | listKnowledgeIds | 登录态；细粒度权限见权限矩阵 | chapterId[query,必填]:integer/int64 | 200=*/*:ResultListLong |
| GET | `/api/education/chapter/textbook` | listByTextbookId | 登录态；细粒度权限见权限矩阵 | textbookId[query,必填]:integer/int64 | 200=*/*:ResultListChapterResponse |
| GET | `/api/education/chapter/tree` | getChapterTree | 登录态；细粒度权限见权限矩阵 | textbookId[query,必填]:integer/int64 | 200=*/*:ResultListChapterResponse |
| POST | `/api/education/chapter/update` | update_15 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:ChapterRequest | 200=*/*:ResultVoid |

### course-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/education/course/create` | create_16 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:CourseRequest | 200=*/*:ResultCourseResponse |
| POST | `/api/education/course/delete` | delete_14 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/course/detail` | getById_11 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultCourseResponse |
| GET | `/api/education/course/grade` | listByGrade | 登录态；细粒度权限见权限矩阵 | grade[query,必填]:integer/int32 | 200=*/*:ResultListCourseResponse |
| POST | `/api/education/course/learn` | startLearning | 登录态；细粒度权限见权限矩阵 | courseId[query,必填]:integer/int64; studentId[query,必填]:integer/int64 | 200=*/*:ResultCourseResponse |
| GET | `/api/education/course/list` | list_14 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataCourseResponse |
| GET | `/api/education/course/subject` | listBySubjectCode_2 | 登录态；细粒度权限见权限矩阵 | subjectCode[query,必填]:string | 200=*/*:ResultListCourseResponse |
| POST | `/api/education/course/update` | update_14 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:CourseRequest | 200=*/*:ResultVoid |

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
| POST | `/api/education/exam/create` | create_15 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ExamRequest | 200=*/*:ResultExamResponse |
| POST | `/api/education/exam/delete` | delete_13 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/exam/detail` | getById_10 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultExamResponse |
| GET | `/api/education/exam/list` | list_13 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataExamResponse |
| GET | `/api/education/exam/subject` | listBySubjectCode_1 | 登录态；细粒度权限见权限矩阵 | subjectCode[query,必填]:string | 200=*/*:ResultListExamResponse |
| GET | `/api/education/exam/teacher` | listByTeacherId | 登录态；细粒度权限见权限矩阵 | teacherId[query,必填]:integer/int64 | 200=*/*:ResultListExamResponse |
| POST | `/api/education/exam/update` | update_13 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:ExamRequest | 200=*/*:ResultVoid |

### generation-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/conversation/generations/{id}/cancel` | cancel_1 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultVoid |
| GET | `/api/conversation/generations/{id}/events` | stream | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string; afterSeq[query,可选]:integer/int32; follow[query,可选]:boolean; waitMs[query,可选]:integer/int32; Last-Event-ID[header,可选]:string | 200=text/event-stream:array<ServerSentEventGenerationEvent> |

### intent-def-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/agent/intents/batch-delete` | deleteBatch_1 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:array<integer/int64> | 200=*/*:ResultVoid |
| POST | `/api/agent/intents/create` | create_20 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:IntentDefRequest | 200=*/*:ResultIntentDefVO |
| POST | `/api/agent/intents/delete` | delete_16 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/agent/intents/detail` | detail_1 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultIntentDefVO |
| GET | `/api/agent/intents/options` | options_2 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListIdNameOptionVO |
| GET | `/api/agent/intents/page` | page_7 | 登录态；细粒度权限见权限矩阵 | agentId[query,可选]:string; name[query,可选]:string; code[query,可选]:string; category[query,可选]:string; pageNo[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataIntentDefVO |
| POST | `/api/agent/intents/update` | update_16 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:IntentDefRequest | 200=*/*:ResultIntentDefVO |

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
| POST | `/api/model/routes` | save | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:RouteRequest | 200=*/*:ResultModelRoutePolicy |
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
| GET | `/api/conversation/prompts` | list_2 | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListPromptTemplate |
| POST | `/api/conversation/prompts` | create_18 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:CreateRequest | 200=*/*:ResultPromptTemplate |
| POST | `/api/conversation/prompts/preview` | preview | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:PreviewRequest | 200=*/*:ResultPromptPreview |
| POST | `/api/conversation/prompts/{id}/publish` | publish_2 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:string | 200=*/*:ResultPromptTemplate |

### question-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/education/question/create` | create_14 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:QuestionRequest | 200=*/*:ResultQuestionResponse |
| POST | `/api/education/question/delete` | delete_12 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/question/detail` | getById_9 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultQuestionResponse |
| GET | `/api/education/question/difficulty` | listByDifficulty | 登录态；细粒度权限见权限矩阵 | difficulty[query,必填]:integer/int32 | 200=*/*:ResultListQuestionResponse |
| GET | `/api/education/question/list` | list_12 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataQuestionResponse |
| GET | `/api/education/question/subject-grade` | listBySubjectAndGrade_1 | 登录态；细粒度权限见权限矩阵 | subjectCode[query,必填]:string; grade[query,必填]:integer/int32 | 200=*/*:ResultListQuestionResponse |
| GET | `/api/education/question/type` | listByType_1 | 登录态；细粒度权限见权限矩阵 | type[query,必填]:string | 200=*/*:ResultListQuestionResponse |
| POST | `/api/education/question/update` | update_12 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:QuestionRequest | 200=*/*:ResultVoid |

### resource-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/education/resource/create` | create_13 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ResourceRequest | 200=*/*:ResultResourceResponse |
| POST | `/api/education/resource/delete` | delete_11 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/resource/detail` | getById_8 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultResourceResponse |
| GET | `/api/education/resource/list` | list_11 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataResourceResponse |
| GET | `/api/education/resource/subject` | listBySubjectCode | 登录态；细粒度权限见权限矩阵 | subjectCode[query,必填]:string | 200=*/*:ResultListResourceResponse |
| GET | `/api/education/resource/type` | listByType | 登录态；细粒度权限见权限矩阵 | type[query,必填]:string | 200=*/*:ResultListResourceResponse |
| POST | `/api/education/resource/update` | update_11 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:ResourceRequest | 200=*/*:ResultVoid |

### review-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/education/review/complete` | complete | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:CompleteReviewRequest | 200=*/*:ResultVoid |
| POST | `/api/education/review/create` | create_12 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:ReviewRequest | 200=*/*:ResultReviewTaskResponse |
| POST | `/api/education/review/delete` | delete_10 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/review/detail` | getById_7 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultReviewTaskResponse |
| GET | `/api/education/review/list` | list_10 | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64; status[query,必填]:integer/int32 | 200=*/*:ResultListReviewTaskResponse |
| GET | `/api/education/review/today` | listTodayTasks | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListReviewTaskResponse |
| POST | `/api/education/review/update` | update_10 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:ReviewRequest | 200=*/*:ResultVoid |

### role-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/iam/roles/all` | getAllRoles | 登录态；细粒度权限见权限矩阵 | status[query,可选]:string; tenantId[query,必填]:integer/int64 | 200=*/*:ResultListRoleVO |
| POST | `/api/iam/roles/create` | createRole | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:RoleRequest | 200=*/*:ResultVoid |
| POST | `/api/iam/roles/delete` | deleteRole | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/iam/roles/detail` | getRoleDetail | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; tenantId[query,必填]:integer/int64 | 200=*/*:ResultRoleVO |
| GET | `/api/iam/roles/list` | getRoleList | 登录态；细粒度权限见权限矩阵 | r[query,必填]:RolePageRequest | 200=*/*:ResultPageDataRoleVO |
| POST | `/api/iam/roles/menus/replace` | replaceRoleMenus | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; tenantId[query,必填]:integer/int64; body[必填]=application/json:array<integer/int64> | 200=*/*:ResultVoid |
| POST | `/api/iam/roles/update` | updateRole | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:RoleRequest | 200=*/*:ResultVoid |
| POST | `/api/iam/roles/users/add` | assignUserRoles | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:AssignUserRolesRequest | 200=*/*:ResultVoid |
| POST | `/api/iam/roles/users/remove` | removeUserRoles | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:AssignUserRolesRequest | 200=*/*:ResultVoid |

### student-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/education/students/create` | create_11 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:StudentRequest | 200=*/*:ResultStudentResponse |
| POST | `/api/education/students/delete` | delete_9 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/students/detail` | getById_6 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultStudentResponse |
| GET | `/api/education/students/list` | list_9 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataStudentResponse |
| POST | `/api/education/students/update` | update_9 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:StudentRequest | 200=*/*:ResultVoid |
| GET | `/api/education/students/user` | getByUserId | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64 | 200=*/*:ResultStudentResponse |

### study-plan-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/education/study-plan/active` | listActiveByStudent | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListStudyPlanResponse |
| POST | `/api/education/study-plan/create` | create_10 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:StudyPlanRequest | 200=*/*:ResultStudyPlanResponse |
| POST | `/api/education/study-plan/delete` | delete_8 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/study-plan/detail` | getById_5 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultStudyPlanResponse |
| GET | `/api/education/study-plan/student` | listByStudentId_1 | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListStudyPlanResponse |
| GET | `/api/education/study-plan/today-tasks` | getTodayTasks | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListDailyTaskResponse |
| POST | `/api/education/study-plan/update` | update_8 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:StudyPlanRequest | 200=*/*:ResultVoid |

### subject-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/education/subject/code` | getByCode_1 | 登录态；细粒度权限见权限矩阵 | code[query,必填]:string | 200=*/*:ResultSubjectResponse |
| POST | `/api/education/subject/create` | create_9 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:SubjectRequest | 200=*/*:ResultSubjectResponse |
| POST | `/api/education/subject/delete` | delete_7 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/subject/detail` | getById_4 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultSubjectResponse |
| GET | `/api/education/subject/grade-level` | listByGradeLevel | 登录态；细粒度权限见权限矩阵 | gradeLevel[query,必填]:string | 200=*/*:ResultListSubjectResponse |
| GET | `/api/education/subject/list` | list_8 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataSubjectResponse |
| POST | `/api/education/subject/update` | update_7 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:SubjectRequest | 200=*/*:ResultVoid |

### tenant-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/iam/tenants/create` | createTenant | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:TenantRequest | 200=*/*:ResultVoid |
| POST | `/api/iam/tenants/delete` | deleteTenant | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/iam/tenants/detail` | getTenantById | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultTenantVO |
| GET | `/api/iam/tenants/list` | getAllTenants | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultListTenantVO |
| GET | `/api/iam/tenants/page` | getTenantPage | 登录态；细粒度权限见权限矩阵 | r[query,必填]:TenantPageRequest | 200=*/*:ResultPageDataTenantVO |
| POST | `/api/iam/tenants/update` | updateTenant | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:TenantRequest | 200=*/*:ResultVoid |

### textbook-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/education/textbook/create` | create_8 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:TextbookRequest | 200=*/*:ResultTextbookResponse |
| POST | `/api/education/textbook/delete` | delete_6 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/textbook/detail` | getById_3 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultTextbookResponse |
| GET | `/api/education/textbook/list` | list_7 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32 | 200=*/*:ResultPageDataTextbookResponse |
| GET | `/api/education/textbook/subject-grade` | listBySubjectAndGrade | 登录态；细粒度权限见权限矩阵 | subjectCode[query,必填]:string; grade[query,必填]:integer/int32 | 200=*/*:ResultListTextbookResponse |
| POST | `/api/education/textbook/update` | update_6 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:TextbookRequest | 200=*/*:ResultVoid |

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
| GET | `/api/agent/runs/{runId}/approvals` | list_4 | 登录态；细粒度权限见权限矩阵 | runId[path,必填]:string | 200=*/*:ResultListToolApproval |
| POST | `/api/agent/runs/{runId}/approvals` | request | 登录态；细粒度权限见权限矩阵 | runId[path,必填]:string; body[必填]=application/json:Request | 200=*/*:ResultToolApproval |

### user-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/iam/users/create` | createUser | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:UserRequest | 200=*/*:ResultMapStringObject |
| POST | `/api/iam/users/delete` | deleteUser | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/iam/users/detail` | getUserInfo | 登录态；细粒度权限见权限矩阵 | - | 200=*/*:ResultUserVO |
| GET | `/api/iam/users/list` | getUserList | 登录态；细粒度权限见权限矩阵 | r[query,必填]:UserPageRequest | 200=*/*:ResultPageDataUserVO |
| POST | `/api/iam/users/password/change` | changePassword | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64; body[必填]=application/json:ChangePasswordRequest | 200=*/*:ResultVoid |
| POST | `/api/iam/users/password/reset` | resetPassword | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64; body[必填]=application/json:ResetPasswordRequest | 200=*/*:ResultVoid |
| GET | `/api/iam/users/tenant-assignments` | getTenantAssignments | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64 | 200=*/*:ResultListUserTenantAssignmentVO |
| POST | `/api/iam/users/tenant-assignments/replace` | replaceTenantAssignments | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64; body[必填]=application/json:array<UserTenantRoleRequest> | 200=*/*:ResultVoid |
| POST | `/api/iam/users/update` | updateUser | 登录态；细粒度权限见权限矩阵 | userId[query,必填]:integer/int64; body[必填]=application/json:UserRequest | 200=*/*:ResultVoid |

### wrong-question-controller

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/education/wrong-question/create` | create_7 | 登录态；细粒度权限见权限矩阵 | body[必填]=application/json:WrongQuestionRequest | 200=*/*:ResultWrongQuestionResponse |
| POST | `/api/education/wrong-question/delete` | delete_5 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultVoid |
| GET | `/api/education/wrong-question/detail` | getById_2 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64 | 200=*/*:ResultWrongQuestionResponse |
| GET | `/api/education/wrong-question/student` | listByStudentId | 登录态；细粒度权限见权限矩阵 | studentId[query,必填]:integer/int64 | 200=*/*:ResultListWrongQuestionResponse |
| POST | `/api/education/wrong-question/update` | update_5 | 登录态；细粒度权限见权限矩阵 | id[query,必填]:integer/int64; body[必填]=application/json:WrongQuestionRequest | 200=*/*:ResultVoid |

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
| GET | `/api/knowledge/ingestion-jobs` | page_4 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32; spaceId[query,可选]:integer/int64; status[query,可选]:string; version[header,可选]:string | 200=*/*:ResultPageDataJobView |
| GET | `/api/knowledge/ingestion-jobs/{id}` | get_2 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultJobView |
| POST | `/api/knowledge/ingestion-jobs/{id}/cancel` | cancel | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultVoid |
| POST | `/api/knowledge/ingestion-jobs/{id}/retry` | retry | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultVoid |

### 知识关系

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/knowledge/points/{pointId}/relations` | list_1 | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListRelationView |
| POST | `/api/knowledge/points/{pointId}/relations` | create_4 | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:RelationRequest | 200=*/*:ResultVoid |
| DELETE | `/api/knowledge/points/{pointId}/relations/{targetId}` | delete_20 | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; targetId[path,必填]:integer/int64; type[query,必填]:string(PRE,NEXT,INCLUDE,RELATED,SIMILAR,BELONG); version[header,可选]:string | 200=*/*:ResultVoid |

### 知识平台审计

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/knowledge/audits` | page_5 | 登录态；细粒度权限见权限矩阵 | pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32; spaceId[query,可选]:integer/int64; version[header,可选]:string | 200=*/*:ResultPageDataKnowledgeAuditResponse |

### 知识引擎运维

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| POST | `/api/knowledge/system/backup` | backup | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string | 200=*/*:ResultBackupResult |
| POST | `/api/knowledge/system/restore-check` | restoreCheck | 登录态；细粒度权限见权限矩阵 | fileName[query,必填]:string; version[header,可选]:string | 200=*/*:ResultRestoreCheckResult |
| GET | `/api/knowledge/system/status` | status | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string | 200=*/*:ResultMapStringObject |

### 知识文档

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/knowledge/documents/upload-sessions/{sessionId}` | uploadStatus | 登录态；细粒度权限见权限矩阵 | sessionId[path,必填]:string; version[header,可选]:string | 200=*/*:ResultUploadSession |
| DELETE | `/api/knowledge/documents/upload-sessions/{sessionId}` | cancelUpload | 登录态；细粒度权限见权限矩阵 | sessionId[path,必填]:string; version[header,可选]:string | 200=*/*:ResultVoid |
| POST | `/api/knowledge/documents/upload-sessions/{sessionId}/chunks/{index}` | uploadChunk | 登录态；细粒度权限见权限矩阵 | sessionId[path,必填]:string; index[path,必填]:integer/int32; totalChunks[query,必填]:integer/int32; version[header,可选]:string; body[可选]=multipart/form-data:object | 200=*/*:ResultUploadSession |
| POST | `/api/knowledge/documents/upload-sessions/{sessionId}/complete` | completeUpload | 登录态；细粒度权限见权限矩阵 | sessionId[path,必填]:string; version[header,可选]:string | 200=*/*:ResultUploadResult |
| GET | `/api/knowledge/documents/{id}` | get_3 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultDocumentView |
| DELETE | `/api/knowledge/documents/{id}` | delete_19 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultVoid |
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
| GET | `/api/knowledge/points/{id}` | get_1 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultPointView |
| PUT | `/api/knowledge/points/{id}` | update_1 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:UpdatePointRequest | 200=*/*:ResultPointView |
| DELETE | `/api/knowledge/points/{id}` | delete_1 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultVoid |
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
| GET | `/api/knowledge/spaces/{id}` | get | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultSpaceView |
| PUT | `/api/knowledge/spaces/{id}` | update | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:UpdateSpaceRequest | 200=*/*:ResultSpaceView |
| DELETE | `/api/knowledge/spaces/{id}` | delete | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultVoid |
| GET | `/api/knowledge/spaces/{id}/difficulty-scale` | difficultyScale | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultDifficultyScaleView |
| GET | `/api/knowledge/spaces/{id}/members` | members | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListMemberView |
| PUT | `/api/knowledge/spaces/{id}/members` | replaceMembers | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string; body[必填]=application/json:array<MemberRequest> | 200=*/*:ResultVoid |

### 知识评测

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/knowledge/evaluations` | page_3 | 登录态；细粒度权限见权限矩阵 | spaceId[query,必填]:integer/int64; pageNum[query,可选]:integer/int32; pageSize[query,可选]:integer/int32; version[header,可选]:string | 200=*/*:ResultPageDataCaseView |
| POST | `/api/knowledge/evaluations` | create_5 | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string; body[必填]=application/json:CreateCaseRequest | 200=*/*:ResultCaseView |
| POST | `/api/knowledge/evaluations/run` | run | 登录态；细粒度权限见权限矩阵 | version[header,可选]:string; body[必填]=application/json:RunRequest | 200=*/*:ResultRunResult |
| DELETE | `/api/knowledge/evaluations/{id}` | delete_21 | 登录态；细粒度权限见权限矩阵 | id[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultVoid |

### 知识路径

| 方法 | 路径 | 摘要 | 鉴权 | 请求 | 响应 |
|---|---|---|---|---|---|
| GET | `/api/knowledge/points/path` | findPath | 登录态；细粒度权限见权限矩阵 | fromId[query,必填]:integer/int64; toId[query,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListLong |
| GET | `/api/knowledge/points/{pointId}/path` | path | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; version[header,可选]:string | 200=*/*:ResultListLong |
| GET | `/api/knowledge/points/{pointId}/prerequisites` | prerequisites | 登录态；细粒度权限见权限矩阵 | pointId[path,必填]:integer/int64; masteredIds[query,可选]:array<integer/int64>; version[header,可选]:string | 200=*/*:ResultListLong |

## 组件模型

| Schema | 类型 | 必填字段 | 字段定义 |
|---|---|---|---|
| `AbilityRadarResponse` | object | - | studentId:integer/int64; knowledgeId:integer/int64; abilities:object; overallMastery:number/double |
| `ActiveLeafRequest` | object | - | messageId:string |
| `AgentDefinition` | object | - | agentId:string; name:string; description:string; extInfo:object; currentVersion:string; createdAt:integer/int64; updatedAt:integer/int64; startNodeId:string; versions:object |
| `AgentDetailVO` | object | - | id:integer/int64; agentId:string; name:string; description:string; currentVersion:string; status:integer/int32; extInfo:object; versions:array<AgentVersionVO>; createTime:string/date-time; updateTime:string/date-time |
| `AgentRequest` | object | agentId, name | agentId*:string; name*:string; description:string; status:integer/int32 |
| `AgentStateFactoryAgentState` | - | - | - |
| `AgentVO` | object | - | id:integer/int64; agentId:string; name:string; description:string; currentVersion:string; status:integer/int32; extInfo:object; createTime:string/date-time; updateTime:string/date-time |
| `AgentVersion` | object | - | versionNumber:string; description:string; createdAt:integer/int64 |
| `AgentVersionDetailVO` | object | - | id:integer/int64; agentId:string; versionNumber:string; description:string; status:integer/int32; statusDesc:string; graphConfig:GraphConfigVO; canvasConfig:string; createTime:string/date-time; updateTime:string/date-time |
| `AgentVersionVO` | object | - | id:integer/int64; agentId:string; versionNumber:string; description:string; status:integer/int32; statusDesc:string; createTime:string/date-time; updateTime:string/date-time |
| `AiApp` | object | - | id:string; tenantId:TenantId; ownerUserId:UserId; name:string; description:string; status:string; publishedVersionId:string; createdAt:string/date-time; updatedAt:string/date-time |
| `AiAppPreview` | object | - | appId:string; appVersionId:string; status:string; promptHash:string; model:string; configuration:object; executable:boolean |
| `AiAppVersion` | object | - | id:string; appId:string; tenantId:TenantId; version:string; configJson:string; status:string; createdAt:string/date-time; publishedAt:string/date-time |
| `AiModelRequest` | object | modelName | platformId:integer/int64; modelName*:string; displayName:string; description:string; modelConfig:string; isDefault:string; sort:integer/int32; status:string |
| `AiModelResponse` | object | - | id:integer/int64; platformId:integer/int64; modelName:string; displayName:string; description:string; modelConfig:string; platformName:string; isDefault:string; sort:integer/int32; status:string; createTime:string/date-time; updateTime:string/date-time |
| `AiModelVO` | object | - | id:integer/int64; platformId:integer/int64; modelName:string; displayName:string; description:string; modelConfig:string; platformName:string; isDefault:string; sort:integer/int32; status:string; createTime:string/date-time; updateTime:string/date-time |
| `AiPlatformRequest` | object | code, name | name*:string; code*:string; adapterType:string; baseUrl:string; apiKey:string; temperature:number/double; maxTokens:integer/int32; maxRetries:integer/int32; availableModels:string; extraConfig:string; isDefault:string; status:string; sort:integer/int32; remark:string |
| `AiPlatformResponse` | object | - | id:integer/int64; name:string; code:string; adapterType:string; baseUrl:string; temperature:number/double; maxTokens:integer/int32; maxRetries:integer/int32; availableModels:string; extraConfig:string; isDefault:string; status:string; sort:integer/int32; remark:string; createTime:string/date-time; updateTime:string/date-time |
| `AiPlatformVO` | object | - | id:integer/int64; name:string; code:string; adapterType:string; baseUrl:string; temperature:number/double; maxTokens:integer/int32; maxRetries:integer/int32; availableModels:string; extraConfig:string; isDefault:string; status:string; sort:integer/int32; remark:string; createTime:string/date-time; updateTime:string/date-time |
| `AiRun` | object | - | id:string; tenantId:TenantId; ownerUserId:UserId; appId:string; appVersionId:string; sourceType:string(CONVERSATION,GENERATION,AGENT,KNOWLEDGE,MEMORY,TOOL,API); sourceId:string; parentRunId:string; traceId:string; conversationId:string; generationId:string; executionId:string; model:string; promptHash:string; status:string(CREATED,RUNNING,COMPLETED,FAILED,CANCELLED); promptTokens:integer/int64; completionTokens:integer/int64; estimatedUsage:boolean; costSnapshot:string; createdAt:string/date-time; completedAt:string/date-time; errorCode:string; version:integer/int64; lastEventSeq:integer/int64 |
| `AiRunEvent` | object | - | runId:string; tenantId:TenantId; seq:integer/int64; type:string(RUN_STARTED,TURN_STARTED,TURN_COMPLETED,STEP_STARTED,STEP_COMPLETED,PROMPT_ASSEMBLED,RETRIEVAL_STARTED,RETRIEVAL_COMPLETED,MODEL_STARTED,MODEL_BLOCK_STARTED,MODEL_DELTA,MODEL_REASONING_DELTA,MODEL_TOOL_CALL_DELTA,MODEL_BLOCK_COMPLETED,TOOL_REQUESTED,TOOL_APPROVAL_REQUIRED,TOOL_APPROVAL_DECIDED,TOOL_COMPLETED,MEMORY_READ,MEMORY_WRITE,MODEL_USAGE,MODEL_COMPLETED,RUN_COMPLETED,RUN_FAILED,RUN_CANCELLED); schemaVersion:integer/int32; turnId:string; stepId:string; parentEventSeq:integer/int64; conversationId:string; generationId:string; executionId:string; appId:string; appVersionId:string; providerRequestId:string; traceId:string; payload:string; redacted:boolean; createdAt:string/date-time |
| `AppExecutionRequest` | object | - | prompt:string; appVersionId:string; input:object |
| `AppRequest` | object | - | name:string; description:string |
| `AssignUserRolesRequest` | object | tenantId | userIds:array<integer/int64>; tenantId*:integer/int64 |
| `AuthCodeOptionVO` | object | - | id:integer/int64; name:string; code:string; module:string; resource:string; action:string; status:integer/int32; createTime:string/date-time |
| `AuthCodePageRequest` | object | - | pageSize:integer/int32; pageNum:integer/int32; orderByColumn:string; isAsc:string; orderFields:array<OrderField>; filterConditions:array<FilterCondition>; code:string; name:string |
| `AuthCodeRequest` | object | code | code*:string; name:string |
| `AuthCodeResponse` | object | - | id:integer/int64; code:string; name:string; status:integer/int32; createTime:string/date-time; updateTime:string/date-time |
| `BackupResult` | object | - | fileName:string; size:integer/int64; createdAt:string |
| `BaseNode` | object | - | config:NodeConfig; executionHistoryService:ExecutionHistoryService; requiredInputs:array<NodeInputParam> |
| `BeginRequest` | object | fileName | fileName*:string; contentType:string; size:integer/int64; checksum:string; title:string |
| `CaptchaVO` | object | - | key:string; image:string; expireTime:integer/int64 |
| `CaseRequest` | object | - | input:string; expected:string; metadata:object |
| `CaseResult` | object | - | caseId:integer/int64; question:string; recallAtK:number/double; reciprocalRank:number/double; citationAccuracy:number/double; expectedDocumentIds:array<integer/int64>; returnedDocumentIds:array<integer/int64> |
| `CaseView` | object | - | id:integer/int64; spaceId:integer/int64; question:string; expectedDocIds:string; expectedAnswer:string |
| `ChangePasswordRequest` | object | newPassword, oldPassword | oldPassword*:string; newPassword*:string |
| `ChannelObject` | object | - | default:-; reducer:ReducerObject |
| `ChapterRequest` | object | name, textbookId | id:integer/int64; textbookId*:integer/int64; name*:string; parentId:integer/int64; chapterOrder:integer/int32; status:integer/int32 |
| `ChapterResponse` | object | - | id:integer/int64; textbookId:integer/int64; parentId:integer/int64; name:string; chapterOrder:integer/int32; children:array<ChapterResponse> |
| `CharacterAsset` | object | - | id:string; tenantId:integer/int64; ownerUserId:integer/int64; card:CharacterCardV2; visibility:string; pngData:string/byte; createdAt:string/date-time; updatedAt:string/date-time |
| `CharacterCardV2` | object | - | spec:string; name:string; description:string; scenario:string; firstMessage:string; exampleDialogues:array<string>; systemPrompt:string; extensions:object; version:integer/int32 |
| `CharacterRequest` | object | - | card:CharacterCardV2; visibility:string |
| `ChatCompletionRequest` | object | - | model:string; platform:string; messages:array<object>; stream:boolean; store:boolean; temperature:number/double; maxOutputTokens:integer/int32; tools:array<object>; conversationId:string; reasoningEffort:string |
| `CodeLoginRequest` | object | captchaKey, code, phone | phone*:string; code*:string; captchaKey*:string |
| `CompileConfig` | - | - | - |
| `CompiledGraphAgentState` | object | - | stateGraph:StateGraphAgentState; maxIterations:integer/int32; compileConfig:CompileConfig |
| `CompleteReviewRequest` | object | - | studentId:integer/int64; resultScore:number/double |
| `ConditionEdge` | object | - | from:string; defaultTarget:string; functionCondition:-; nodeMappings:object; predicateConditions:array<PredicateCondition> |
| `ConditionalEdgeDTO` | object | - | defaultTarget:string; nodeMappings:object; conditionType:string |
| `ContentPart` | object | - | type:string; text:string; mediaUri:string; mimeType:string; metadata:object |
| `Conversation` | object | - | id:string; tenantId:integer/int64; ownerUserId:integer/int64; sceneType:string; title:string; status:string(ACTIVE,ARCHIVED,DELETED); parentConversationId:string; branchFromMessageId:string; activeLeafMessageId:string; rollingSummary:string; platform:string; model:string; version:integer/int64; createdAt:string/date-time; updatedAt:string/date-time |
| `ConversationMessage` | object | - | id:string; conversationId:string; parentMessageId:string; sourceMessageId:string; role:string(SYSTEM,USER,ASSISTANT,TOOL); contentParts:array<ContentPart>; toolCall:object; status:string(PENDING,STREAMING,COMPLETED,CANCELLED,FAILED,DELETED); sequence:integer/int32; generationId:string; createdAt:string/date-time; updatedAt:string/date-time |
| `CourseRequest` | object | name | id:integer/int64; name*:string; subjectCode:string; grade:integer/int32; description:string; coverUrl:string; textbookId:integer/int64; teacherId:integer/int64; totalHours:integer/int32; status:integer/int32 |
| `CourseResponse` | object | - | id:integer/int64; name:string; description:string; subjectCode:string; grade:integer/int32; textbookId:integer/int64; teacherId:integer/int64; coverUrl:string; totalHours:integer/int32; status:integer/int32 |
| `CreateCaseRequest` | object | question | spaceId:integer/int64; question*:string; expectedDocIds:string; expectedAnswer:string |
| `CreateConversationRequest` | object | - | sceneType:string; title:string; platform:string; model:string; systemPrompt:string |
| `CreatePointRequest` | object | code, name | code*:string; name*:string; description:string; difficultyLevel:integer/int32; category:string; tags:string |
| `CreateRequest` | object | - | name:string; template:string; variables:array<string> |
| `CreateSpaceRequest` | object | code, name | code*:string; name*:string; domainCode:string; description:string; accessMode:string; reviewMode:string; bindingMode:string; difficultyScaleId:integer/int64; embeddingProfile:string; rerankProfile:string; chunkStrategy:string; chunkSize:integer/int32; chunkOverlap:integer/int32 |
| `DailyTaskResponse` | object | - | id:integer/int64; knowledgeId:integer/int64; knowledgeName:string; planDate:string; status:integer/int32; statusDesc:string; orderNo:integer/int32 |
| `DataSourceConfig` | object | - | type:string; url:string; dictType:string; labelKey:string; valueKey:string; dependsOn:string |
| `DatasetRequest` | object | - | name:string; description:string |
| `DictPageRequest` | object | - | pageSize:integer/int32; pageNum:integer/int32; orderByColumn:string; isAsc:string; orderFields:array<OrderField>; filterConditions:array<FilterCondition> |
| `DictRequest` | object | - | dictType:string; dictLabel:string; dictValue:string; dictSort:integer/int32; cssClass:string; listClass:string; isDefault:string; remark:string; status:integer/int32 |
| `DictVO` | object | - | id:integer/int64; tenantId:integer/int64; dictType:string; dictLabel:string; dictValue:string; dictSort:integer/int32; cssClass:string; listClass:string; isDefault:string; status:string; remark:string; createTime:string/date-time; updateTime:string/date-time |
| `DiffRequest` | object | - | fromVersion:integer/int32; toVersion:integer/int32 |
| `DifficultyLevelView` | object | - | level:integer/int32; label:string; description:string |
| `DifficultyScaleView` | object | - | id:integer/int64; code:string; name:string; description:string; levelCount:integer/int32; levels:array<DifficultyLevelView> |
| `DocumentRelationRequest` | object | - | documentId:integer/int64; relationType:string |
| `DocumentRelationView` | object | - | id:integer/int64; sourceDocumentId:integer/int64; targetDocumentId:integer/int64; relationType:string; targetTitle:string |
| `DocumentSummary` | object | - | id:integer/int64; spaceId:integer/int64; title:string; docType:string; lifecycleStatus:string; parseStatus:string |
| `DocumentView` | object | - | id:integer/int64; spaceId:integer/int64; currentVersionId:integer/int64; title:string; docType:string; source:string; lifecycleStatus:string; parseStatus:string; objectKey:string; mimeType:string; fileSize:integer/int64; checksum:string; createTime:string/date-time; updateTime:string/date-time |
| `EdgeRequest` | object | sourceNodeId, targetNodeId | sourceNodeId*:string; targetNodeId*:string; edgeType:string; conditionMappings:object; defaultTarget:string; conditionType:string |
| `EditRequest` | object | - | content:string |
| `EvalCase` | object | - | id:string; datasetId:string; tenantId:integer/int64; input:string; expected:string; metadata:object; createdAt:string/date-time |
| `EvalDataset` | object | - | id:string; tenantId:integer/int64; ownerUserId:integer/int64; name:string; description:string; createdAt:string/date-time |
| `EvalResult` | object | - | caseId:string; metric:string(EXACT_MATCH,CONTAINS,JSON_SCHEMA,TOOL_CALL_SCHEMA,CITATION_COVERAGE,RETRIEVAL_HIT,TOKEN_BUDGET,COST_BUDGET); score:number/double; passed:boolean; detail:string |
| `EvalRun` | object | - | id:string; datasetId:string; tenantId:integer/int64; ownerUserId:integer/int64; appVersionId:string; metric:string(EXACT_MATCH,CONTAINS,JSON_SCHEMA,TOOL_CALL_SCHEMA,CITATION_COVERAGE,RETRIEVAL_HIT,TOKEN_BUDGET,COST_BUDGET); status:string; passRate:number/double; results:array<EvalResult>; createdAt:string/date-time; completedAt:string/date-time |
| `EventRequest` | object | - | namespace:string; subjectType:string; subjectId:string; eventType:string; content:string; occurredAt:string/date-time; sourceType:string; sourceId:string; attributes:object; confidence:number/double; importance:number/double; confirmationPolicy:string(AUTO,REQUIRED,DISABLED) |
| `ExamRequest` | object | durationMin, grade, name, subjectCode, totalScore, type | id:integer/int64; name*:string; type*:string; subjectCode*:string; grade*:integer/int32; teacherId:integer/int64; durationMin*:integer/int32; totalScore*:integer/int32; status:integer/int32 |
| `ExamResponse` | object | - | id:integer/int64; name:string; type:string; subjectCode:string; grade:integer/int32; teacherId:integer/int64; durationMin:integer/int32; totalScore:integer/int32; status:integer/int32 |
| `ExecutionHistoryService` | - | - | - |
| `FieldMeta` | object | - | key:string; label:string; type:string; defaultValue:-; required:boolean; options:object; description:string; source:DataSourceConfig |
| `FileView` | object | - | key:string; name:string; size:integer/int64; contentType:string; lastModified:string/date-time; url:string; storageType:string |
| `FilterCondition` | object | - | field:string; operator:string(EQ,NE,GT,GE,LT,LE,LIKE,IN,NOT_IN,IS_NULL,IS_NOT_NULL); value:- |
| `ForgetPasswordRequest` | object | captchaKey, code, email, newPassword | email*:string/email; newPassword*:string; code*:string; captchaKey*:string |
| `GenerateRequest` | object | - | provider:string; prompt:string; format:string |
| `GenerationRun` | object | - | id:string; conversationId:string; inputMessageId:string; assistantMessageId:string; speakerId:string; platform:string; model:string; status:string(CREATED,RUNNING,COMPLETED,CANCELLED,FAILED); promptTokens:integer/int64; completionTokens:integer/int64; latencyMs:integer/int64; errorCode:string; lastEventSequence:integer/int32; cancelRequested:boolean; version:integer/int64; createdAt:string/date-time; updatedAt:string/date-time; runtimeRunId:string |
| `Graph` | object | - | name:string; description:string; nodes:object; edges:object; conditionalEdges:object; channels:object; startNode:string; endNode:string; compiledGraph:CompiledGraphAgentState; compiled:boolean |
| `GraphConfigRequest` | object | - | name:string; description:string; startNode:string; endNode:string; nodes:object; edges:object; conditionalEdges:object |
| `GraphConfigVO` | object | - | name:string; description:string; startNode:string; endNode:string; nodes:object; edges:object; conditionalEdges:object |
| `GraphValidationVO` | object | - | valid:boolean; errors:array<string>; warnings:array<string> |
| `GroupChat` | object | - | id:string; name:string; participants:array<Participant>; speakerPolicy:string(MANUAL,ROUND_ROBIN,MODEL_ROUTED); maxTurns:integer/int32; tokenBudget:integer/int32 |
| `GroupChatAsset` | object | - | id:string; tenantId:integer/int64; ownerUserId:integer/int64; group:GroupChat; createdAt:string/date-time; updatedAt:string/date-time |
| `GroupRequest` | object | - | name:string; participants:array<Participant>; speakerPolicy:string(MANUAL,ROUND_ROBIN,MODEL_ROUTED); maxTurns:integer/int32; tokenBudget:integer/int32 |
| `GroupTurnRun` | object | - | decision:TurnDecision; generationId:string; speakerId:string |
| `HybridHit` | object | - | chunkId:integer/int64; documentId:integer/int64; content:string; highlight:string; bm25Score:number/double; vectorScore:number/double; rrfScore:number/double; rerankScore:number/double |
| `HybridRecommendResponse` | object | - | studentId:integer/int64; knowledgeTop:array<KnowledgeRecommendResponse>; questionTop:array<QuestionRecommendResponse>; resourceTop:array<ResourceRecommendResponse>; reviewTop:array<QuestionRecommendResponse>; overallAdvice:string; generateTime:integer/int64 |
| `IdNameOptionVO` | object | - | id:integer/int64; name:string; code:string; value:string |
| `ImageRequest` | object | - | provider:string; imageBase64:string; mimeType:string; instruction:string |
| `ImageResult` | object | - | objectKey:string; mimeType:string; width:integer/int32; height:integer/int32 |
| `ImportRequest` | object | - | format:string; previewToken:string; title:string; sceneType:string; platform:string; model:string; systemPrompt:string; content:string |
| `ImportUrlRequest` | object | url | url*:string; title:string |
| `ImportedMessage` | object | - | role:string; content:string |
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
| `LorebookAsset` | object | - | id:string; tenantId:integer/int64; ownerUserId:integer/int64; entry:LorebookEntry; createdAt:string/date-time; updatedAt:string/date-time |
| `LorebookEntry` | object | - | id:string; keys:array<string>; content:string; priority:integer/int32; insertionPosition:string; tokenBudget:integer/int32; enabled:boolean |
| `McpToolDescriptor` | object | - | name:string; description:string; serverId:string; parameters:object; tags:array<string>; category:string; builtin:boolean; registeredAt:integer/int64 |
| `MemberRequest` | object | principalId, principalType, spaceRole | principalType*:string; principalId*:integer/int64; spaceRole*:string |
| `MemberView` | object | - | id:integer/int64; spaceId:integer/int64; principalType:string; principalId:integer/int64; spaceRole:string |
| `MemoryEdge` | object | - | id:string; tenantId:TenantId; sourceNodeId:string; targetNodeId:string; graphType:string(TEMPORAL,SEMANTIC,CAUSAL,ENTITY); relationType:string; directed:boolean; weight:number/double; confidence:number/double; origin:string(RULE,DOMAIN,MODEL); evidenceSource:string; active:boolean; createdAt:string/date-time |
| `MemoryEvent` | object | - | id:string; tenantId:TenantId; namespace:string; subjectType:string; subjectId:string; eventType:string; content:string; occurredAt:string/date-time; sourceType:string; sourceId:string; attributes:object; confidence:number/double; importance:number/double; status:string(CANDIDATE,ACTIVE,SUPERSEDED,REVOKED); confirmationPolicy:string(AUTO,REQUIRED,DISABLED); createdAt:string/date-time; updatedAt:string/date-time |
| `MemoryPath` | object | - | event:MemoryEvent; score:number/double; edges:array<MemoryEdge> |
| `MemoryRetrievalResult` | object | - | paths:array<MemoryPath>; traceId:string |
| `MemoryRetrievalTrace` | object | - | id:string; tenantId:TenantId; namespace:string; queryText:string; anchorEventIds:array<string>; graphWeights:object; relationPaths:array<array<string>>; filteredEventIds:array<string>; resultEventIds:array<string>; createdAt:string/date-time |
| `MenuPageRequest` | object | - | pageSize:integer/int32; pageNum:integer/int32; orderByColumn:string; isAsc:string; orderFields:array<OrderField>; filterConditions:array<FilterCondition>; name:string; code:string; type:string; status:integer/int32 |
| `MenuRequest` | object | name, type | name*:string; code:string; type*:string; path:string; redirect:string; icon:string; component:string; layout:string; keepAlive:boolean; method:string; description:string; show:boolean; status:string; order:integer/int32; pid:integer/int64 |
| `MenuVO` | object | - | id:integer/int64; name:string; code:string; type:string; path:string; redirect:string; icon:string; component:string; layout:string; keepAlive:boolean; method:string; description:string; show:boolean; status:integer/int32; order:integer/int32; delFlag:integer/int32; createTime:string/date-time; updateTime:string/date-time; children:array<MenuVO>; pid:integer/int64 |
| `MessageRequest` | object | - | content:string; platform:string; model:string |
| `MetaVO` | object | - | title:string; activeIcon:string; activePath:string; affixTab:boolean; affixTabOrder:integer/int32; authority:array<string>; badge:string; badgeType:string; badgeVariants:string; fullPathKey:boolean; hideChildrenInMenu:boolean; hideInBreadcrumb:boolean; hideInMenu:boolean; hideInTab:boolean; icon:string; iframeSrc:string; ignoreAccess:boolean; keepAlive:boolean; link:string; loaded:boolean; maxNumOfOpenTab:integer/int32; menuVisibleWithForbidden:boolean; noBasicLayout:boolean; openInNewWindow:boolean; order:integer/int32; query:- |
| `ModelProviderCapabilities` | object | - | provider:string; model:string; features:array<string>; contextWindow:integer/int32; streaming:boolean; tools:boolean; parallelTools:boolean; multimodal:boolean; jsonSchema:boolean; reasoningLevels:array<string>; maxOutputTokens:integer/int32; streamUsage:boolean; cacheUsage:boolean; cancellation:boolean |
| `ModelRoutePolicy` | object | - | id:string; tenantId:integer/int64; name:string; orderedModels:array<string>; timeoutMs:integer/int32; fallbackOnError:boolean; maxTokens:integer/int64 |
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
| `ParameterInfo` | object | - | type:string; description:string; required:boolean; defaultValue:- |
| `Participant` | object | - | id:string; displayName:string; characterId:string |
| `Persona` | object | - | id:string; ownerUserId:integer/int64; name:string; identity:string; tone:string; visibility:string; attributes:object |
| `PersonaAsset` | object | - | id:string; tenantId:integer/int64; ownerUserId:integer/int64; persona:Persona; createdAt:string/date-time; updatedAt:string/date-time |
| `PluginInfoVO` | object | - | id:string; name:string; version:string; description:string; state:string; loadedAt:string |
| `PluginMarketEntry` | object | - | id:string; version:string; source:string; manifest:string; signature:string; publisherKey:string; permissions:array<string>; checksum:string; updatePolicy:string; publishedAt:string/date-time; enabled:boolean |
| `PointView` | object | - | id:integer/int64; spaceId:integer/int64; code:string; name:string; description:string; difficultyLevel:integer/int32; category:string; tags:string |
| `PredicateCondition` | object | - | predicate:-; target:string |
| `Preview` | object | - | token:string; expiresAt:string/date-time; messages:array<ImportedMessage> |
| `PreviewRequest` | object | - | template:string; variables:object |
| `PromptDiff` | object | - | templateId:string; fromVersion:integer/int32; toVersion:integer/int32; changes:array<string> |
| `PromptPreview` | object | - | content:string; estimatedTokens:integer/int64; variables:array<string> |
| `PromptPreviewRequest` | object | - | body:string; variables:object |
| `PromptRequest` | object | - | templateId:string; version:integer/int32; status:string; body:string; variableSchema:object; testCases:array<string> |
| `PromptTemplate` | object | - | id:string; tenantId:integer/int64; ownerUserId:integer/int64; name:string; template:string; variables:array<string>; status:string; createdAt:string/date-time; updatedAt:string/date-time |
| `PromptTemplateVersion` | object | - | id:string; templateId:string; version:integer/int32; status:string; body:string; variableSchema:object; testCases:array<string>; createdAt:string/date-time; publishedAt:string/date-time |
| `PromptTestRequest` | object | - | version:integer/int32; variables:object |
| `PromptTestRun` | object | - | templateId:string; version:integer/int32; renderedCases:array<string>; estimatedTokens:integer/int32 |
| `ProviderHealth` | object | - | provider:string; model:string; healthy:boolean; consecutiveFailures:integer/int32; checkedAt:string/date-time; message:string |
| `PublishRequest` | object | - | version:integer/int32 |
| `QueryRequest` | object | - | namespace:string; subjectType:string; subjectId:string; text:string; graphTypes:array<string(TEMPORAL,SEMANTIC,CAUSAL,ENTITY)>; from:string/date-time; to:string/date-time; maxDepth:integer/int32; maxNodes:integer/int32; maxTokens:integer/int32; intent:string(SEMANTIC,TEMPORAL,CAUSAL,ENTITY,HYBRID) |
| `QuestionRecommendResponse` | object | - | questionId:integer/int64; title:string; type:string; difficulty:integer/int32; knowledgeId:integer/int64; knowledgeName:string; recommendType:string; reason:string; score:integer/int32 |
| `QuestionRequest` | object | answer, difficulty, grade, subjectCode, title, type | id:integer/int64; title*:string; type*:string; code:string; subjectCode*:string; grade*:integer/int32; difficulty*:integer/int32; abilityDimension:string; options:string; answer*:string; analysis:string; tags:string; status:integer/int32 |
| `QuestionResponse` | object | - | id:integer/int64; code:string; type:string; subjectCode:string; grade:integer/int32; difficulty:integer/int32; abilityDimension:string; title:string; options:string; answer:string; analysis:string; tags:string; usedCount:integer/int64 |
| `RebuildRequest` | object | spaceId | spaceId*:integer/int64 |
| `ReducerObject` | - | - | - |
| `RefreshTokenRequest` | object | accessToken | accessToken*:string |
| `RegisterAgentRequest` | object | - | agentId:string; name:string; description:string; versionNumber:string; versionDescription:string; graph:Graph |
| `RelationRequest` | object | sourceId, targetId, type | sourceId*:integer/int64; targetId*:integer/int64; type*:string(PRE,NEXT,INCLUDE,RELATED,SIMILAR,BELONG); weight:number/double |
| `RelationView` | object | - | sourceId:integer/int64; targetId:integer/int64; relationType:string; weight:number/double; source:KnowledgeResponse; target:KnowledgeResponse |
| `ReplaceDocumentRelationsRequest` | object | relations | relations*:array<DocumentRelationRequest> |
| `ReplacePointsRequest` | object | pointIds | pointIds*:array<integer/int64>; relationType:string |
| `ReplaceRequest` | object | documentIds | documentIds*:array<integer/int64>; relationType:string |
| `Request` | object | - | toolName:string; argumentsRedacted:string |
| `ResetPasswordRequest` | object | - | password:string |
| `ResourceRecommendResponse` | object | - | resourceId:integer/int64; title:string; type:string; knowledgeId:integer/int64; knowledgeName:string; recommendType:string; reason:string; score:integer/int32 |
| `ResourceRequest` | object | name, type, url | id:integer/int64; name*:string; type*:string; subjectCode:string; grade:integer/int32; difficulty:integer/int32; coverUrl:string; url*:string; description:string; status:integer/int32 |
| `ResourceResponse` | object | - | id:integer/int64; name:string; type:string; url:string; subjectCode:string; grade:integer/int32; difficulty:integer/int32; coverUrl:string; description:string; viewCount:integer/int64 |
| `ResponsesRequest` | object | - | model:string; platform:string; input:-; stream:boolean; store:boolean; temperature:number/double; maxOutputTokens:integer/int32; tools:array<object>; conversationId:string; reasoningEffort:string |
| `RestoreCheckResult` | object | - | valid:boolean; entries:integer/int64; errors:array<string> |
| `ResultAbilityRadarResponse` | object | - | code:integer/int32; data:AbilityRadarResponse; message:string; error:string; success:boolean |
| `ResultAgentDefinition` | object | - | code:integer/int32; data:AgentDefinition; message:string; error:string; success:boolean |
| `ResultAgentDetailVO` | object | - | code:integer/int32; data:AgentDetailVO; message:string; error:string; success:boolean |
| `ResultAgentVO` | object | - | code:integer/int32; data:AgentVO; message:string; error:string; success:boolean |
| `ResultAgentVersionDetailVO` | object | - | code:integer/int32; data:AgentVersionDetailVO; message:string; error:string; success:boolean |
| `ResultAgentVersionVO` | object | - | code:integer/int32; data:AgentVersionVO; message:string; error:string; success:boolean |
| `ResultAiApp` | object | - | code:integer/int32; data:AiApp; message:string; error:string; success:boolean |
| `ResultAiAppPreview` | object | - | code:integer/int32; data:AiAppPreview; message:string; error:string; success:boolean |
| `ResultAiAppVersion` | object | - | code:integer/int32; data:AiAppVersion; message:string; error:string; success:boolean |
| `ResultAiModelVO` | object | - | code:integer/int32; data:AiModelVO; message:string; error:string; success:boolean |
| `ResultAiPlatformResponse` | object | - | code:integer/int32; data:AiPlatformResponse; message:string; error:string; success:boolean |
| `ResultAiPlatformVO` | object | - | code:integer/int32; data:AiPlatformVO; message:string; error:string; success:boolean |
| `ResultAiRun` | object | - | code:integer/int32; data:AiRun; message:string; error:string; success:boolean |
| `ResultAuthCodeResponse` | object | - | code:integer/int32; data:AuthCodeResponse; message:string; error:string; success:boolean |
| `ResultBackupResult` | object | - | code:integer/int32; data:BackupResult; message:string; error:string; success:boolean |
| `ResultBoolean` | object | - | code:integer/int32; data:boolean; message:string; error:string; success:boolean |
| `ResultCaptchaVO` | object | - | code:integer/int32; data:CaptchaVO; message:string; error:string; success:boolean |
| `ResultCaseView` | object | - | code:integer/int32; data:CaseView; message:string; error:string; success:boolean |
| `ResultChapterResponse` | object | - | code:integer/int32; data:ChapterResponse; message:string; error:string; success:boolean |
| `ResultCharacterAsset` | object | - | code:integer/int32; data:CharacterAsset; message:string; error:string; success:boolean |
| `ResultConversation` | object | - | code:integer/int32; data:Conversation; message:string; error:string; success:boolean |
| `ResultConversationMessage` | object | - | code:integer/int32; data:ConversationMessage; message:string; error:string; success:boolean |
| `ResultCourseResponse` | object | - | code:integer/int32; data:CourseResponse; message:string; error:string; success:boolean |
| `ResultDictVO` | object | - | code:integer/int32; data:DictVO; message:string; error:string; success:boolean |
| `ResultDifficultyScaleView` | object | - | code:integer/int32; data:DifficultyScaleView; message:string; error:string; success:boolean |
| `ResultDocumentView` | object | - | code:integer/int32; data:DocumentView; message:string; error:string; success:boolean |
| `ResultEvalCase` | object | - | code:integer/int32; data:EvalCase; message:string; error:string; success:boolean |
| `ResultEvalDataset` | object | - | code:integer/int32; data:EvalDataset; message:string; error:string; success:boolean |
| `ResultEvalRun` | object | - | code:integer/int32; data:EvalRun; message:string; error:string; success:boolean |
| `ResultExamResponse` | object | - | code:integer/int32; data:ExamResponse; message:string; error:string; success:boolean |
| `ResultFileView` | object | - | code:integer/int32; data:FileView; message:string; error:string; success:boolean |
| `ResultGenerationRun` | object | - | code:integer/int32; data:GenerationRun; message:string; error:string; success:boolean |
| `ResultGraphValidationVO` | object | - | code:integer/int32; data:GraphValidationVO; message:string; error:string; success:boolean |
| `ResultGroupChatAsset` | object | - | code:integer/int32; data:GroupChatAsset; message:string; error:string; success:boolean |
| `ResultGroupTurnRun` | object | - | code:integer/int32; data:GroupTurnRun; message:string; error:string; success:boolean |
| `ResultHybridRecommendResponse` | object | - | code:integer/int32; data:HybridRecommendResponse; message:string; error:string; success:boolean |
| `ResultImageResult` | object | - | code:integer/int32; data:ImageResult; message:string; error:string; success:boolean |
| `ResultIntentDefVO` | object | - | code:integer/int32; data:IntentDefVO; message:string; error:string; success:boolean |
| `ResultJobView` | object | - | code:integer/int32; data:JobView; message:string; error:string; success:boolean |
| `ResultKnowledgeGraphResponse` | object | - | code:integer/int32; data:KnowledgeGraphResponse; message:string; error:string; success:boolean |
| `ResultListAgentDefinition` | object | - | code:integer/int32; data:array<AgentDefinition>; message:string; error:string; success:boolean |
| `ResultListAgentVersionVO` | object | - | code:integer/int32; data:array<AgentVersionVO>; message:string; error:string; success:boolean |
| `ResultListAiApp` | object | - | code:integer/int32; data:array<AiApp>; message:string; error:string; success:boolean |
| `ResultListAiAppVersion` | object | - | code:integer/int32; data:array<AiAppVersion>; message:string; error:string; success:boolean |
| `ResultListAiModelResponse` | object | - | code:integer/int32; data:array<AiModelResponse>; message:string; error:string; success:boolean |
| `ResultListAiModelVO` | object | - | code:integer/int32; data:array<AiModelVO>; message:string; error:string; success:boolean |
| `ResultListAiPlatformVO` | object | - | code:integer/int32; data:array<AiPlatformVO>; message:string; error:string; success:boolean |
| `ResultListAiRun` | object | - | code:integer/int32; data:array<AiRun>; message:string; error:string; success:boolean |
| `ResultListAiRunEvent` | object | - | code:integer/int32; data:array<AiRunEvent>; message:string; error:string; success:boolean |
| `ResultListAuthCodeOptionVO` | object | - | code:integer/int32; data:array<AuthCodeOptionVO>; message:string; error:string; success:boolean |
| `ResultListChapterResponse` | object | - | code:integer/int32; data:array<ChapterResponse>; message:string; error:string; success:boolean |
| `ResultListCharacterAsset` | object | - | code:integer/int32; data:array<CharacterAsset>; message:string; error:string; success:boolean |
| `ResultListConversation` | object | - | code:integer/int32; data:array<Conversation>; message:string; error:string; success:boolean |
| `ResultListConversationMessage` | object | - | code:integer/int32; data:array<ConversationMessage>; message:string; error:string; success:boolean |
| `ResultListCourseResponse` | object | - | code:integer/int32; data:array<CourseResponse>; message:string; error:string; success:boolean |
| `ResultListDailyTaskResponse` | object | - | code:integer/int32; data:array<DailyTaskResponse>; message:string; error:string; success:boolean |
| `ResultListDictVO` | object | - | code:integer/int32; data:array<DictVO>; message:string; error:string; success:boolean |
| `ResultListDocumentRelationView` | object | - | code:integer/int32; data:array<DocumentRelationView>; message:string; error:string; success:boolean |
| `ResultListDocumentSummary` | object | - | code:integer/int32; data:array<DocumentSummary>; message:string; error:string; success:boolean |
| `ResultListEvalCase` | object | - | code:integer/int32; data:array<EvalCase>; message:string; error:string; success:boolean |
| `ResultListEvalResult` | object | - | code:integer/int32; data:array<EvalResult>; message:string; error:string; success:boolean |
| `ResultListExamResponse` | object | - | code:integer/int32; data:array<ExamResponse>; message:string; error:string; success:boolean |
| `ResultListFileView` | object | - | code:integer/int32; data:array<FileView>; message:string; error:string; success:boolean |
| `ResultListGroupChatAsset` | object | - | code:integer/int32; data:array<GroupChatAsset>; message:string; error:string; success:boolean |
| `ResultListIdNameOptionVO` | object | - | code:integer/int32; data:array<IdNameOptionVO>; message:string; error:string; success:boolean |
| `ResultListKnowledgeRecommendResponse` | object | - | code:integer/int32; data:array<KnowledgeRecommendResponse>; message:string; error:string; success:boolean |
| `ResultListLong` | object | - | code:integer/int32; data:array<integer/int64>; message:string; error:string; success:boolean |
| `ResultListLorebookAsset` | object | - | code:integer/int32; data:array<LorebookAsset>; message:string; error:string; success:boolean |
| `ResultListMapStringObject` | object | - | code:integer/int32; data:array<object>; message:string; error:string; success:boolean |
| `ResultListMcpToolDescriptor` | object | - | code:integer/int32; data:array<McpToolDescriptor>; message:string; error:string; success:boolean |
| `ResultListMemberView` | object | - | code:integer/int32; data:array<MemberView>; message:string; error:string; success:boolean |
| `ResultListMemoryEdge` | object | - | code:integer/int32; data:array<MemoryEdge>; message:string; error:string; success:boolean |
| `ResultListMenuVO` | object | - | code:integer/int32; data:array<MenuVO>; message:string; error:string; success:boolean |
| `ResultListModelProviderCapabilities` | object | - | code:integer/int32; data:array<ModelProviderCapabilities>; message:string; error:string; success:boolean |
| `ResultListModelRoutePolicy` | object | - | code:integer/int32; data:array<ModelRoutePolicy>; message:string; error:string; success:boolean |
| `ResultListNodeTypeMetaVO` | object | - | code:integer/int32; data:array<NodeTypeMetaVO>; message:string; error:string; success:boolean |
| `ResultListPersonaAsset` | object | - | code:integer/int32; data:array<PersonaAsset>; message:string; error:string; success:boolean |
| `ResultListPluginInfoVO` | object | - | code:integer/int32; data:array<PluginInfoVO>; message:string; error:string; success:boolean |
| `ResultListPluginMarketEntry` | object | - | code:integer/int32; data:array<PluginMarketEntry>; message:string; error:string; success:boolean |
| `ResultListPromptTemplate` | object | - | code:integer/int32; data:array<PromptTemplate>; message:string; error:string; success:boolean |
| `ResultListPromptTemplateVersion` | object | - | code:integer/int32; data:array<PromptTemplateVersion>; message:string; error:string; success:boolean |
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
| `ResultListTenantInfoVO` | object | - | code:integer/int32; data:array<TenantInfoVO>; message:string; error:string; success:boolean |
| `ResultListTenantVO` | object | - | code:integer/int32; data:array<TenantVO>; message:string; error:string; success:boolean |
| `ResultListTextbookResponse` | object | - | code:integer/int32; data:array<TextbookResponse>; message:string; error:string; success:boolean |
| `ResultListTimezoneOptionVO` | object | - | code:integer/int32; data:array<TimezoneOptionVO>; message:string; error:string; success:boolean |
| `ResultListToolApproval` | object | - | code:integer/int32; data:array<ToolApproval>; message:string; error:string; success:boolean |
| `ResultListUserTenantAssignmentVO` | object | - | code:integer/int32; data:array<UserTenantAssignmentVO>; message:string; error:string; success:boolean |
| `ResultListVersionView` | object | - | code:integer/int32; data:array<VersionView>; message:string; error:string; success:boolean |
| `ResultListWeakPointResponse` | object | - | code:integer/int32; data:array<WeakPointResponse>; message:string; error:string; success:boolean |
| `ResultListWrongQuestionResponse` | object | - | code:integer/int32; data:array<WrongQuestionResponse>; message:string; error:string; success:boolean |
| `ResultLoginResponseVO` | object | - | code:integer/int32; data:LoginResponseVO; message:string; error:string; success:boolean |
| `ResultLong` | object | - | code:integer/int32; data:integer/int64; message:string; error:string; success:boolean |
| `ResultLorebookAsset` | object | - | code:integer/int32; data:LorebookAsset; message:string; error:string; success:boolean |
| `ResultMapStringObject` | object | - | code:integer/int32; data:object; message:string; error:string; success:boolean |
| `ResultMapStringString` | object | - | code:integer/int32; data:object; message:string; error:string; success:boolean |
| `ResultMcpToolDescriptor` | object | - | code:integer/int32; data:McpToolDescriptor; message:string; error:string; success:boolean |
| `ResultMemoryEvent` | object | - | code:integer/int32; data:MemoryEvent; message:string; error:string; success:boolean |
| `ResultMemoryRetrievalResult` | object | - | code:integer/int32; data:MemoryRetrievalResult; message:string; error:string; success:boolean |
| `ResultMemoryRetrievalTrace` | object | - | code:integer/int32; data:MemoryRetrievalTrace; message:string; error:string; success:boolean |
| `ResultModelProviderCapabilities` | object | - | code:integer/int32; data:ModelProviderCapabilities; message:string; error:string; success:boolean |
| `ResultModelRoutePolicy` | object | - | code:integer/int32; data:ModelRoutePolicy; message:string; error:string; success:boolean |
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
| `ResultPageDataMenuVO` | object | - | code:integer/int32; data:PageDataMenuVO; message:string; error:string; success:boolean |
| `ResultPageDataPointView` | object | - | code:integer/int32; data:PageDataPointView; message:string; error:string; success:boolean |
| `ResultPageDataQuestionResponse` | object | - | code:integer/int32; data:PageDataQuestionResponse; message:string; error:string; success:boolean |
| `ResultPageDataResourceResponse` | object | - | code:integer/int32; data:PageDataResourceResponse; message:string; error:string; success:boolean |
| `ResultPageDataRoleVO` | object | - | code:integer/int32; data:PageDataRoleVO; message:string; error:string; success:boolean |
| `ResultPageDataSpaceView` | object | - | code:integer/int32; data:PageDataSpaceView; message:string; error:string; success:boolean |
| `ResultPageDataStudentResponse` | object | - | code:integer/int32; data:PageDataStudentResponse; message:string; error:string; success:boolean |
| `ResultPageDataSubjectResponse` | object | - | code:integer/int32; data:PageDataSubjectResponse; message:string; error:string; success:boolean |
| `ResultPageDataTenantVO` | object | - | code:integer/int32; data:PageDataTenantVO; message:string; error:string; success:boolean |
| `ResultPageDataTextbookResponse` | object | - | code:integer/int32; data:PageDataTextbookResponse; message:string; error:string; success:boolean |
| `ResultPageDataUserVO` | object | - | code:integer/int32; data:PageDataUserVO; message:string; error:string; success:boolean |
| `ResultPersonaAsset` | object | - | code:integer/int32; data:PersonaAsset; message:string; error:string; success:boolean |
| `ResultPluginMarketEntry` | object | - | code:integer/int32; data:PluginMarketEntry; message:string; error:string; success:boolean |
| `ResultPointView` | object | - | code:integer/int32; data:PointView; message:string; error:string; success:boolean |
| `ResultPreview` | object | - | code:integer/int32; data:Preview; message:string; error:string; success:boolean |
| `ResultPromptDiff` | object | - | code:integer/int32; data:PromptDiff; message:string; error:string; success:boolean |
| `ResultPromptPreview` | object | - | code:integer/int32; data:PromptPreview; message:string; error:string; success:boolean |
| `ResultPromptTemplate` | object | - | code:integer/int32; data:PromptTemplate; message:string; error:string; success:boolean |
| `ResultPromptTemplateVersion` | object | - | code:integer/int32; data:PromptTemplateVersion; message:string; error:string; success:boolean |
| `ResultPromptTestRun` | object | - | code:integer/int32; data:PromptTestRun; message:string; error:string; success:boolean |
| `ResultProviderHealth` | object | - | code:integer/int32; data:ProviderHealth; message:string; error:string; success:boolean |
| `ResultQuestionResponse` | object | - | code:integer/int32; data:QuestionResponse; message:string; error:string; success:boolean |
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
| `ResultTenantVO` | object | - | code:integer/int32; data:TenantVO; message:string; error:string; success:boolean |
| `ResultTextbookResponse` | object | - | code:integer/int32; data:TextbookResponse; message:string; error:string; success:boolean |
| `ResultToolApproval` | object | - | code:integer/int32; data:ToolApproval; message:string; error:string; success:boolean |
| `ResultTrendResponse` | object | - | code:integer/int32; data:TrendResponse; message:string; error:string; success:boolean |
| `ResultTurnDecision` | object | - | code:integer/int32; data:TurnDecision; message:string; error:string; success:boolean |
| `ResultUploadResult` | object | - | code:integer/int32; data:UploadResult; message:string; error:string; success:boolean |
| `ResultUploadSession` | object | - | code:integer/int32; data:UploadSession; message:string; error:string; success:boolean |
| `ResultUserVO` | object | - | code:integer/int32; data:UserVO; message:string; error:string; success:boolean |
| `ResultValidateCaptchaResponse` | object | - | code:integer/int32; data:ValidateCaptchaResponse; message:string; error:string; success:boolean |
| `ResultVisionResult` | object | - | code:integer/int32; data:VisionResult; message:string; error:string; success:boolean |
| `ResultVoid` | object | - | code:integer/int32; data:-; message:string; error:string; success:boolean |
| `ResultWrongQuestionResponse` | object | - | code:integer/int32; data:WrongQuestionResponse; message:string; error:string; success:boolean |
| `RetryRequest` | object | - | platform:string; model:string |
| `ReviewRequest` | object | knowledgeId, studentId | id:integer/int64; studentId*:integer/int64; knowledgeId*:integer/int64; status:integer/int32; reviewDate:string/date; reviewRound:integer/int32; resultScore:number/double; completedAt:string/date-time |
| `ReviewTaskResponse` | object | - | id:integer/int64; studentId:integer/int64; knowledgeId:integer/int64; knowledgeName:string; reviewRound:integer/int32; reviewDate:string; status:integer/int32; statusDesc:string; resultScore:number/double; completedAt:string/date-time |
| `RolePageRequest` | object | - | pageSize:integer/int32; pageNum:integer/int32; orderByColumn:string; isAsc:string; orderFields:array<OrderField>; filterConditions:array<FilterCondition>; name:string |
| `RoleRequest` | object | code, name, tenantId | code*:string; tenantId*:integer/int64; name*:string; status:string; remark:string; permissions:array<integer/int64> |
| `RoleVO` | object | - | id:integer/int64; tenantId:integer/int64; code:string; name:string; status:integer/int32; remark:string; delFlag:integer/int32; createTime:string/date-time; updateTime:string/date-time; permissions:array<integer/int64> |
| `RouteMenuVO` | object | - | id:integer/int64; pid:integer/int64; name:string; path:string; component:string; type:string; status:integer/int32; icon:string; redirect:string; meta:MetaVO; children:array<RouteMenuVO> |
| `RouteRequest` | object | - | name:string; orderedModels:array<string>; timeoutMs:integer/int32; fallbackOnError:boolean; maxTokens:integer/int64 |
| `RunRequest` | object | - | spaceId:integer/int64; topK:integer/int32 |
| `RunResult` | object | - | spaceId:integer/int64; caseCount:integer/int32; topK:integer/int32; recallAtK:number/double; mrr:number/double; citationAccuracy:number/double; cases:array<CaseResult> |
| `SearchRequest` | object | query, spaceId | spaceId*:integer/int64; query*:string; mode:string; topK:integer/int32; threshold:number/double; rerank:boolean |
| `SearchResponse` | object | - | spaceId:integer/int64; mode:string; hits:array<HybridHit> |
| `ServerSentEventAiRunEvent` | - | - | - |
| `ServerSentEventGenerationEvent` | - | - | - |
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
| `TenantContextVO` | object | - | tenantId:integer/int64; tenantName:string; roleCode:string |
| `TenantId` | object | - | value:integer/int64 |
| `TenantInfoVO` | object | - | id:integer/int64; code:string; name:string; pathName:string |
| `TenantPageRequest` | object | - | pageSize:integer/int32; pageNum:integer/int32; orderByColumn:string; isAsc:string; orderFields:array<OrderField>; filterConditions:array<FilterCondition>; name:string; code:string; status:integer/int32 |
| `TenantRequest` | object | code, name | parentId:integer/int64; code*:string; name*:string; contactName:string; contactPhone:string; address:string; domain:string; intro:string; order:integer/int32; leader:string; email:string; remark:string; status:string; menuIds:array<integer/int64>; authCodeIds:array<integer/int64>; adminRoleName:string; adminUsername:string; adminPassword:string |
| `TenantVO` | object | - | id:integer/int64; parentId:integer/int64; code:string; name:string; contactName:string; contactPhone:string; address:string; domain:string; intro:string; order:integer/int32; leader:string; phone:string; email:string; remark:string; status:integer/int32; createTime:string/date-time; updateTime:string/date-time; children:array<TenantVO> |
| `TestRequest` | object | - | requiredFeatures:array<string> |
| `TextbookRequest` | object | grade, name, publisher, subjectCode | id:integer/int64; name*:string; subjectCode*:string; grade*:integer/int32; publisher*:string; author:string; edition:string; isbn:string; status:integer/int32 |
| `TextbookResponse` | object | - | id:integer/int64; name:string; subjectCode:string; grade:integer/int32; publisher:string; isbn:string |
| `TimezoneOptionVO` | object | - | label:string; value:string |
| `ToolApproval` | object | - | id:string; runId:string; tenantId:integer/int64; ownerUserId:integer/int64; toolName:string; argumentsRedacted:string; status:string(PENDING,APPROVED,REJECTED,EXPIRED); createdAt:string/date-time; decidedAt:string/date-time; expiresAt:string/date-time |
| `TranslateRequest` | object | - | provider:string; text:string; sourceLanguage:string; targetLanguage:string |
| `TrendResponse` | object | - | dates:array<string>; values:array<number/double> |
| `TtsRequest` | object | - | provider:string; text:string; voice:string; format:string |
| `TurnDecision` | object | - | exhausted:boolean; reason:string; participant:Participant; remainingTurns:integer/int32; remainingTokens:integer/int32 |
| `TurnRequest` | object | - | completedSpeakerIds:array<string>; requestedSpeakerId:string; consumedTokens:integer/int32 |
| `TurnRunRequest` | object | - | conversationId:string; content:string; platform:string; model:string; completedSpeakerIds:array<string>; requestedSpeakerId:string; consumedTokens:integer/int32 |
| `UpdateConversationRequest` | object | - | title:string; status:string |
| `UpdatePointRequest` | object | - | name:string; description:string; difficultyLevel:integer/int32; category:string; tags:string |
| `UpdateSpaceRequest` | object | - | name:string; description:string; domainCode:string; accessMode:string; reviewMode:string; bindingMode:string; difficultyScaleId:integer/int64; embeddingProfile:string; rerankProfile:string; chunkStrategy:string; chunkSize:integer/int32; chunkOverlap:integer/int32; status:integer/int32 |
| `UploadResult` | object | - | document:DocumentView; versionId:integer/int64; jobId:integer/int64; duplicate:boolean |
| `UploadSession` | object | - | sessionId:string; spaceId:integer/int64; fileName:string; size:integer/int64; totalChunks:integer/int32; uploadedChunks:array<integer/int32>; chunkSize:integer/int32 |
| `UserId` | object | - | value:integer/int64 |
| `UserPageRequest` | object | - | pageSize:integer/int32; pageNum:integer/int32; orderByColumn:string; isAsc:string; orderFields:array<OrderField>; filterConditions:array<FilterCondition>; username:string |
| `UserRequest` | object | tenantId, username | username*:string; password:string; nickName:string; email:string; phone:string; gender:string; avatar:string; address:string; status:string; remark:string; roleIds:array<integer/int64>; tenantId*:integer/int64; postIds:array<integer/int64> |
| `UserTenantAssignmentVO` | object | - | tenantId:integer/int64; tenantName:string; roleId:integer/int64; roleName:string; roleCode:string |
| `UserTenantRoleRequest` | object | roleId, tenantId | tenantId*:integer/int64; roleId*:integer/int64 |
| `UserVO` | object | - | id:integer/int64; username:string; status:string; delFlag:integer/int32; createTime:string/date-time; updateTime:string/date-time; nickName:string; gender:string; avatar:string; address:string; email:string; phone:string; remark:string; roles:array<RoleVO>; roleIds:array<integer/int64>; currentRole:RoleVO; extInfo:string; tenants:array<TenantInfoVO>; subTenants:array<TenantContextVO>; currentTenantId:integer/int64; homeTenantId:integer/int64; switchMode:string |
| `ValidateCaptchaRequest` | object | - | key:string; code:string |
| `ValidateCaptchaResponse` | object | - | success:boolean; message:string |
| `VersionRequest` | object | versionNumber | versionNumber*:string; description:string; copyFromVersionId:integer/int64 |
| `VersionView` | object | - | id:integer/int64; documentId:integer/int64; spaceId:integer/int64; versionNo:integer/int32; title:string; lifecycleStatus:string; parseStatus:string; objectKey:string; mimeType:string; fileSize:integer/int64; checksum:string; modelProfile:string; publishedAt:string/date-time; createTime:string/date-time |
| `VisionResult` | object | - | text:string; labels:array<string> |
| `WeakPointResponse` | object | - | knowledgeId:integer/int64; knowledgeName:string; mastery:number/double |
| `WrongQuestionRequest` | object | questionId, studentId | id:integer/int64; studentId*:integer/int64; questionId*:integer/int64; knowledgeId:integer/int64; studentAnswer:string; correctTimes:integer/int32; status:integer/int32 |
| `WrongQuestionResponse` | object | - | id:integer/int64; studentId:integer/int64; questionId:integer/int64; knowledgeId:integer/int64; questionTitle:string; studentAnswer:string; correctAnswer:string; correctTimes:integer/int32 |

## 维护与验证

```powershell
python scripts/docs/generate_reference_docs.py --openapi-url http://127.0.0.1:9000/v3/api-docs
```

生成后应运行前端 `pnpm run test:contract`，确保前端方法与路径仍被该 OpenAPI 契约覆盖。

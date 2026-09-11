# JavaDoc 扫描报告（2026-09-11）

## 扫描范围

扫描 `shiyu-ai` 下所有非备份、非 `_validation`、非 `target` 的 `src/main/java/**/*.java` 文件，识别接口声明、公共 contract DTO、record 和跨模块端口。

扫描结果：

- 包含接口定义的生产源码文件：269 个
- 重点公共 contract/API 接口：已逐一检查
- 本次补充 JavaDoc 的源码文件：26 个
- 重点公共 contract/API 接口仍缺少类型级 JavaDoc：0 个

全量扫描中剩余未补类型级注释的接口主要是 MyBatis mapper、实现模块内部 repository/service port、SPI marker 或框架回调。它们不属于跨模块公共契约；本次不批量添加重复性的空泛注释，以避免将实现细节误标成稳定 API。

## 已覆盖的边界

- Model：`ChatEngine`、`ModelRoutingPort`、`ModelCatalogPort`、`EmbeddingService`、`ChatRequest`、`ChatResponse`
- Agent：`NodeCreator`、`AiRunRepository`、`ContextPolicy`、`ContextRetrievalPort`、`ContextAssemblyPort`、`ContextQuery`
- Conversation：`GenerationAdmission`、`GenerationUsageSink`、`GenerationRun`、会话/生成/幂等/聊天产品 repository port
- Knowledge：`KnowledgeRetrievalService`
- Memory：`MemoryIngestionPort`、`MemoryQueryPort`
- Governance：`UsageGovernance`
- Infrastructure：`DomainEventPublisher`、`FileStorage`、`ContentSecurityScanner`

除类型级注释外，关键方法还补充了参数语义、返回值、事件或租户隔离说明；没有修改业务逻辑、方法签名或序列化结构。

## 验证命令

```powershell
rg -l '\binterface\b' shiyu-ai -g '*.java' -g '!target' -g '!_validation' -g '!data-*'
git -C shiyu-ai diff --check
mvn -pl modules/domains/model/shiyu-model-contract -am test
```

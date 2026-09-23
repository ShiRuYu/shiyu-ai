# shiyu-model-contract 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-model-contract`
- **分类**：领域模块 · Contract

## 作用

定义跨领域使用模型推理、路由、目录和嵌入能力时共享的数据结构与端口。

## 契约内容

- `ChatEngine` 与结构化聊天消息/请求/响应模型定义聊天调用契约。
- `ModelRoutingPort` 和 `ModelCatalogPort` 提供模型选择与目录查询入口，隐藏 provider 选择和配置存储细节。
- `EmbeddingService` 提供文本嵌入协作接口，供知识等模块构建索引或执行语义检索。
- 契约不包含模型厂商 SDK、密钥、HTTP 适配、配置持久化和具体路由策略实现。

## 边界

Agent、知识和会话等协作模块依赖这些类型；契约不依赖 model implementation。

## 主要包

`com.shiyu.ai.model.contract`、`com.shiyu.ai.model.contract.api`、`com.shiyu.ai.model.contract.model`

## 内部模块依赖

`shiyu-shared-kernel`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/model/shiyu-model-contract -am test -Ddependency-check.skip=true`

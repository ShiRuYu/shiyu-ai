# shiyu-model-implementation 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-model-implementation`
- **分类**：领域模块 · Implementation

## 作用

实现模型配置、provider 适配与路由，以及聊天、嵌入和多媒体 AI 能力。

## 功能说明

- 模型管理：维护平台连接和模型目录，提供模型增删改查、能力配置、健康检查与可用性信息。
- Provider 与路由：适配 DeepSeek HTTP 接口和 Ollama 等模型平台，根据配置执行模型选择、路由及能力匹配。
- 推理能力：实现结构化聊天及流式事件、文本嵌入，并支持语音合成、翻译、图像理解和图像生成等多媒体任务。
- 安全配置：通过 `shiyu.ai` 配置模型 provider 与密钥来源；密钥不作为普通模型响应数据返回。
- Web 与持久化：模型 Controller 暴露平台、模型与推理相关 API，MyBatis 存储平台和模型配置；H2 schema 提供开发基线。
- 其他领域通过 `shiyu-model-contract` 使用聊天、目录、路由和嵌入能力，不依赖 provider adapter 或配置实体。

## 边界

provider 密钥、供应商协议和路由策略属于本模块实现；其他领域只依赖模型 contract，并按各自授权规则控制调用入口。

## 主要包

`com.shiyu.ai.model.implementation`、`com.shiyu.ai.model.implementation.application`、`com.shiyu.ai.model.implementation.domain`、`com.shiyu.ai.model.implementation.infrastructure`、`com.shiyu.ai.model.implementation.persistence`、`com.shiyu.ai.model.implementation.web`

## 内部模块依赖

`shiyu-model-contract`、`shiyu-shared-kernel`、`shiyu-common-mybatis`、`shiyu-common-web`、`shiyu-common-foundation`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/model/shiyu-model-implementation -am test -Ddependency-check.skip=true`

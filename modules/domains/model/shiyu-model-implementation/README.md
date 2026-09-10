# shiyu-model-implementation 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-model-implementation`
- **分类**：领域模块 · Implementation
- **源码规模**：生产 Java 50 个，测试 Java 16 个

## 作用

Model 模型实现模块，承载该领域的应用服务、领域模型和基础设施适配。

## 职责

- 负责多 LLM 平台、模型配置、路由、聊天和嵌入适配。
- 在领域内部组织 application、domain、persistence、infrastructure 和 web 等实现职责。
- 通过 contract、shared-kernel 与公共基础设施协作，对外暴露领域服务。

## 边界

实现细节只在组合根和需要该能力的应用层装配；其他领域应依赖 contract。

## 主要包

`com.shiyu.ai.model.implementation`、`com.shiyu.ai.model.implementation.application`、`com.shiyu.ai.model.implementation.domain`、`com.shiyu.ai.model.implementation.infrastructure`、`com.shiyu.ai.model.implementation.persistence`、`com.shiyu.ai.model.implementation.web`

## 内部模块依赖

`shiyu-model-contract`、`shiyu-shared-kernel`、`shiyu-common-mybatis`、`shiyu-common-web`、`shiyu-common-core`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/model/shiyu-model-implementation -am test -Ddependency-check.skip=true`

根目录执行完整构建时，本模块由根 `pom.xml` 纳入 30 个项目的 Maven reactor。

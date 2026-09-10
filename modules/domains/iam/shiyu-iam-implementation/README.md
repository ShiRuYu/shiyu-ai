# shiyu-iam-implementation 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-iam-implementation`
- **分类**：领域模块 · Implementation
- **源码规模**：生产 Java 145 个，测试 Java 38 个

## 作用

IAM 身份与权限实现模块，承载该领域的应用服务、领域模型和基础设施适配。

## 职责

- 负责用户、租户、身份认证、授权和会话安全。
- 在领域内部组织 application、domain、persistence、infrastructure 和 web 等实现职责。
- 通过 contract、shared-kernel 与公共基础设施协作，对外暴露领域服务。

## 边界

实现细节只在组合根和需要该能力的应用层装配；其他领域应依赖 contract。

## 主要包

`com.shiyu.ai.iam.implementation`、`com.shiyu.ai.iam.implementation.api`、`com.shiyu.ai.iam.implementation.application`、`com.shiyu.ai.iam.implementation.config`、`com.shiyu.ai.iam.implementation.domain`、`com.shiyu.ai.iam.implementation.handler`、`com.shiyu.ai.iam.implementation.persistence`、`com.shiyu.ai.iam.implementation.port` 等（共 13 个包根）

## 内部模块依赖

`shiyu-iam-contract`、`shiyu-knowledge-contract`、`shiyu-shared-kernel`、`shiyu-common-web`、`shiyu-common-core`、`shiyu-common-mybatis`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/iam/shiyu-iam-implementation -am test -Ddependency-check.skip=true`

根目录执行完整构建时，本模块由根 `pom.xml` 纳入 30 个项目的 Maven reactor。

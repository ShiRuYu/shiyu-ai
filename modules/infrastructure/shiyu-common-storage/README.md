# shiyu-common-storage 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-common-storage`
- **分类**：基础设施模块
- **源码规模**：生产 Java 26 个，测试 Java 6 个

## 作用

提供文件、备份、元数据、租约、限流、安全和向量文件存储能力。

## 职责

- 提供文件、备份、元数据、租约、限流、安全和向量文件存储能力。
- 保持与业务领域解耦，通过稳定接口为多个领域提供横切能力。
- 避免把具体业务用例、领域实体或领域数据库表放入公共基础设施。

## 边界

业务模块可以依赖公共基础设施；基础设施不反向依赖领域 implementation。

## 主要包

`com.shiyu.ai.common.storage.api`、`com.shiyu.ai.common.storage.backup`、`com.shiyu.ai.common.storage.config`、`com.shiyu.ai.common.storage.file`、`com.shiyu.ai.common.storage.lease`、`com.shiyu.ai.common.storage.metadata`、`com.shiyu.ai.common.storage.rate`、`com.shiyu.ai.common.storage.security` 等（共 10 个包根）

## 内部模块依赖

`shiyu-common-core`、`shiyu-common-web`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/infrastructure/shiyu-common-storage -am test -Ddependency-check.skip=true`

根目录执行完整构建时，本模块由根 `pom.xml` 纳入 30 个项目的 Maven reactor。

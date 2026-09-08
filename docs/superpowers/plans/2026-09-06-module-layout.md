# 后端目录分层迁移计划

目标：分类目录不再兼任 Maven 模块，根 POM 直接聚合叶子模块。

- 将业务域移至 modules/domains，共享内核移至 modules/shared。
- 将启动、应用装配及 HTTP 接入移至 modules/applications。
- 将六个公共技术模块移至 modules/infrastructure，架构测试移至 tests。
- 移除 domains、common 的聚合 POM；所有叶子模块直接继承根 POM，保持其原有有效构建配置。
- 同步源码测试、CI、脚本与前后端文档中的目录引用，保留 artifactId、Java 包名和 API。
- 验收：根 reactor 完整覆盖 29 个叶子模块，分类目录无 POM；架构检查、文档校验、全量 Maven verify、覆盖率和全新目录启动通过。

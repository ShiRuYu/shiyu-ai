# shiyu-education-implementation 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-education-implementation`
- **分类**：领域模块 · Implementation

## 作用

实现教育产品中的课程学习、练习评估、学习管理与教育资源能力，并通过教育 Web API 对应用提供服务。

## 功能说明

- 课程教学：课程、章节、科目和教材内容维护，并将课程内容组织为可学习的教学结构。
- 学习管理：学生档案、学习记录与进度、学习计划和任务、复习安排及完成状态。
- 练习评估：题库与考试、教育 Agent 节点、错题整理、能力分析及学习建议。
- 教育资源：学习资源上传和内容管理，并结合课程与学习状况生成混合推荐。
- Web/API 与持久化：教育 Controller 通过 `/api/education/**` 暴露接口；MyBatis 持久化实现课程、练习和学习数据。仓库资源提供 H2 schema 与 seed。
- 与 Agent、模型、知识能力的协作通过相应 contract；教育业务代码不直接取代平台 IAM、通用文件或向量基础设施。
- 通过 `shiyu.modules.education.enabled` 控制该业务模块装配；该开关控制模块是否可用，不改变已存在的数据。

## 边界

教育业务属于独立业务模块，不应迁入平台 IAM、模型或公共基础设施。跨领域复用只经 contract；对外 HTTP 路径仍由教育 Controller 声明。

## 主要包

`com.shiyu.ai.education.implementation`、`com.shiyu.ai.education.implementation.agent`、`com.shiyu.ai.education.implementation.application`、`com.shiyu.ai.education.implementation.domain`、`com.shiyu.ai.education.implementation.persistence`、`com.shiyu.ai.education.implementation.web`

## 内部模块依赖

`shiyu-education-contract`、`shiyu-shared-kernel`、`shiyu-agent-contract`、`shiyu-model-contract`、`shiyu-knowledge-contract`、`shiyu-common-foundation`、`shiyu-common-web`、`shiyu-common-mybatis`、`shiyu-common-storage`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/business/education/shiyu-education-implementation -am test -Ddependency-check.skip=true`

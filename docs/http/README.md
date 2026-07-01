# REST Client API 请求文件

本目录包含一组 `.http` 文件，每个文件都可独立使用。在 VS Code 中通过 **REST Client** 插件直接调试 shiyu-ai 项目的全部 API。

## 使用方法

### 1. 安装插件

在 VS Code 中安装 **REST Client** 插件（Humao 出品）。

### 2. 启动应用

```bash
cd shiyu-ai-bootstrap
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

应用默认监听 `http://localhost:9000`

### 3. 按顺序执行请求

打开任意 `.http` 文件：

```
第一步 → 文件顶部的 #0 登录请求（获取 Token，每个文件独立）
第二步 → 后续的具体 API 请求
```

**每个文件都已包含登录请求**，无需先打开 `00-认证中心.http`。

## 文件映射

| 文件 | 所属模块 | 接口数 | 说明 |
|------|---------|--------|------|
| 00-认证中心.http | shiyu-ai-auth | ~11 | 登录/登出/Token/验证码/租户切换 |
| 01-Agent引擎.http | shiyu-ai-agent | ~24 | Agent 注册/执行/版本/Graph 配置 |
| 02-知识图谱.http | shiyu-ai-knowledge | ~21 | 知识点CRUD/图谱/搜索/文档 |
| 03-教育业务域.http | shiyu-ai-education | ~35 | 学科/教材/章节/课程/题目/考试/复习/计划 |
| 04-教育Agent.http | shiyu-ai-agent(edu) | ~12 | Teacher/Practice/Exam/Review/Planner/Report |
| 05-AI对话与模型.http | shiyu-ai-core + agent | ~20 | Chat/平台/模型管理 |
| 06-用户与权限管理.http | shiyu-ai-auth | ~30 | 用户/角色/菜单/租户/字典/时区 |
| 07-人物记录与人脉.http | shiyu-ai-record | ~18 | Profile/Record/Timeline/Tag/Media |

## 变量说明

| 变量名 | 来源 | 说明 |
|--------|------|------|
| `{{baseUrl}}` | 文件顶部定义 | `http://localhost:9000` |
| `{{Login.response.body.$.data.accessToken}}` | 文件内 `# @name Login` 请求自动获取 | 后续请求的 Bearer Token |

> ⚠️ **重要**：VS Code REST Client 中 `@name` 变量仅在**同一个 `.http` 文件内**有效。
> 因此每个文件都包含独立的登录请求，互不依赖。打开任意文件，先执行顶部的登录请求即可。

## 种子数据支持

`seed_data__init.sql` 提供了完整的教育域测试数据，应用启动时自动加载，支持以下场景：

- **学生**: 小明（studentId=1, user=admin, 绝对值偏弱48.5）、小红（studentId=2, user=jack, 全面优秀83.5）、小华（studentId=3, user=vben, 初二物理生）

- **教师**: 张老师（teacherId=1, user=vben, MATH）、李老师（teacherId=2, user=admin, PHYSICS）

- **知识点**: 10 个数学知识点（ID=1~10, 自然数→导数），含12条关系边

- **题目**: 20 道含解析题目（数学10+物理5+英语5），覆盖CHOICE/FILL/SOLVE/JUDGE

- **考试**: 有理数单元测验 + 期中考试 + 声现象小测验 + 英语单元测试

- **复习任务**: 14 条艾宾浩斯排期任务（含 PENDING/COMPLETED/OVERDUE 状态）

- **学习计划**: 3 个计划（二次函数/导数/绝对值巩固）+ 16 条明细

- **错题本**: 6 条典型错误记录

- **知识文档**: 3 篇学习资料（绝对值讲义/数轴总结/有理数规则）

- **资源**: 12 个学习资源（视频/PPT/PDF/动画/音频）

- **课程/章节**: 5 门课程 + 10 课程章节 + 8 小节

1. **先验证基础 API**: 从 `00-认证中心` 开始，确认登录成功
2. **再验证教育域**: 使用 `03-教育业务域` 测试 CRUD
3. **最后验证 Agent**: 使用 `04-教育Agent` 测试 AI 能力（需要 LLM 配置）

# 识宇AI平台

## 项目简介

识宇AI平台是一个基于Java的多模块系统，包含认证、聊天、代理、语音合成等多个功能模块。该项目使用Spring Boot框架，并整合了MyBatis、LiteFlow、Sa-Token等技术。

## 模块说明

- **shiyu-auth**: 提供用户认证和权限管理功能。
- **shiyu-chat**: 实现聊天功能，支持与AI模型交互。
- **shiyu-agent**: 提供AI代理服务，支持多种AI模型。
- **shiyu-mcp**: 提供MCP工具服务，如天气查询。
- **shiyu-tts**: 文本转语音服务。
- **shiyu-common**: 公共库，包含核心工具类、异常处理、数据库访问等通用功能。

## 技术栈

- Spring Boot
- MyBatis
- LiteFlow
- Sa-Token
- Reactor (Flux)
- RestClient
- ThreadPoolTaskExecutor

## 快速开始

### 环境要求

- Java 17+
- Maven 3.8+
- Git

### 构建项目

```bash
git clone https://gitee.com/li21/shiyu-ai.git
cd shiyu-ai
mvn clean install
```

### 启动应用

每个模块都有自己的启动类，例如：

- `com.shiyu.ai.auth.ShiyuAuthApplication`
- `com.shiyu.ai.chat.ShiyuChatApplication`
- `com.shiyu.ai.agent.ShiyuAgentApplication`
- `com.shiyu.ai.mcp.ShiyuMcpApplication`
- `com.shiyu.ai.tts.ShiyuTtsApplication`

使用以下命令启动应用：

```bash
mvn spring-boot:run
```

## 功能特性

### 用户认证与权限管理

- 用户管理
- 角色管理
- 部门管理
- 菜单管理
- 租户管理

### AI聊天功能

- 支持多种AI模型（如OpenRouter、SiliconFlow）
- 支持流式和非流式聊天
- 支持意图识别和思维链处理

### AI代理服务

- 提供AI代理服务，支持多种AI模型
- 支持流式和非流式聊天

### MCP工具服务

- 天气查询
- 空气质量查询（模拟数据）

### 文本转语音服务

- 支持章节分割和TTS处理

## API文档

使用OpenAPI 3.0规范，启动应用后可通过以下URL访问API文档：

```
http://localhost:8080/swagger-ui.html
```

## 配置

配置文件位于各个模块的`src/main/resources`目录下，主要配置文件包括：

- `application.yml`
- `application-ai.yml`
- `application-mcp.yml`
- `log4j2.xml`

## 日志

日志配置使用Log4j2，日志文件默认输出到控制台。

## 异常处理

统一异常处理通过`@ControllerAdvice`实现，返回标准的错误信息格式。

## 安全性

使用Sa-Token进行权限控制，支持登录验证和权限拦截。

## 线程池配置

使用`ThreadPoolTaskExecutor`进行异步任务处理，配置在`ThreadPoolConfig`中。

## 开发规范

- 使用Lombok简化代码
- 使用MapStruct进行对象转换
- 使用Validation进行参数校验
- 使用XSS过滤防止跨站脚本攻击

## 测试

使用JUnit 5进行单元测试，测试类位于各个模块的`src/test/java`目录下。

## 部署

支持Docker部署，具体Dockerfile需根据实际需求编写。

## 贡献指南

欢迎贡献代码，请遵循以下步骤：

1. Fork仓库
2. 创建新分支
3. 提交代码
4. 创建Pull Request

## 许可证

本项目采用MIT许可证。详情请查看项目根目录下的LICENSE文件。
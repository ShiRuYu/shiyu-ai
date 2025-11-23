# ShiYu AI Platform

## Project Overview

The ShiYu AI Platform is a multi-module system built on Java, featuring functional modules such as authentication, chat, agent services, and text-to-speech. This project utilizes the Spring Boot framework and integrates technologies including MyBatis, LiteFlow, and Sa-Token.

## Module Descriptions

- **shiyu-auth**: Provides user authentication and permission management.
- **shiyu-chat**: Implements chat functionality with support for interaction with AI models.
- **shiyu-agent**: Offers AI agent services supporting multiple AI models.
- **shiyu-mcp**: Provides MCP tool services, such as weather queries.
- **shiyu-tts**: Text-to-speech service.
- **shiyu-common**: Common library containing core utilities, exception handling, database access, and other generic functions.

## Technology Stack

- Spring Boot
- MyBatis
- LiteFlow
- Sa-Token
- Reactor (Flux)
- RestClient
- ThreadPoolTaskExecutor

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- Git

### Build the Project

```bash
git clone https://gitee.com/li21/shiyu-ai.git
cd shiyu-ai
mvn clean install
```

### Run the Application

Each module has its own main application class, for example:

- `com.shiyu.ai.auth.ShiyuAuthApplication`
- `com.shiyu.ai.chat.ShiyuChatApplication`
- `com.shiyu.ai.agent.ShiyuAgentApplication`
- `com.shiyu.ai.mcp.ShiyuMcpApplication`
- `com.shiyu.ai.tts.ShiyuTtsApplication`

Start the application using the following command:

```bash
mvn spring-boot:run
```

## Features

### User Authentication and Permission Management

- User management
- Role management
- Department management
- Menu management
- Tenant management

### AI Chat Functionality

- Supports multiple AI models (e.g., OpenRouter, SiliconFlow)
- Supports both streaming and non-streaming chat
- Supports intent recognition and chain-of-thought processing

### AI Agent Service

- Provides AI agent services supporting multiple AI models
- Supports both streaming and non-streaming chat

### MCP Tool Services

- Weather query
- Air quality query (simulated data)

### Text-to-Speech Service

- Supports chapter segmentation and TTS processing

## API Documentation

Following the OpenAPI 3.0 specification, access the API documentation after starting the application via:

```
http://localhost:8080/swagger-ui.html
```

## Configuration

Configuration files are located in the `src/main/resources` directory of each module. Key configuration files include:

- `application.yml`
- `application-ai.yml`
- `application-mcp.yml`
- `log4j2.xml`

## Logging

Logging is configured using Log4j2; logs are output to the console by default.

## Exception Handling

Unified exception handling is implemented via `@ControllerAdvice`, returning standardized error response formats.

## Security

Permission control is managed using Sa-Token, supporting login validation and permission interception.

## Thread Pool Configuration

Asynchronous task processing is handled via `ThreadPoolTaskExecutor`, configured in `ThreadPoolConfig`.

## Development Guidelines

- Use Lombok to simplify code
- Use MapStruct for object mapping
- Use Validation for parameter validation
- Apply XSS filtering to prevent cross-site scripting attacks

## Testing

Unit tests are implemented using JUnit 5; test classes are located in the `src/test/java` directory of each module.

## Deployment

Docker deployment is supported; a custom Dockerfile must be created according to specific requirements.

## Contribution Guidelines

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a new branch
3. Commit your changes
4. Submit a Pull Request

## License

This project is licensed under the MIT License. See the LICENSE file in the project root directory for details.
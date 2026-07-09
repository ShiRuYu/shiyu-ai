# ShiYu AI Platform

> Enterprise-grade AI platform built on Java 21 + Spring Boot 4.x — Graph-based Agent orchestration, RAG knowledge engine, multi-platform LLM adaptation, MCP tool integration, intelligent education tutoring

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Module Description](#module-description)
- [Technology Stack](#technology-stack)
- [Quick Start](#quick-start)
- [Configuration Guide](#configuration-guide)
- [API Documentation](#api-documentation)
- [Development Guidelines](#development-guidelines)
- [Project Documentation](#project-documentation)
- [License](#license)

---

## Overview

**ShiYu AI** is an enterprise-grade intelligent platform for AI education scenarios, built with a modular monolith architecture. It covers core functionalities including AI conversation, Agent orchestration, knowledge base RAG, and intelligent education tutoring.

Key Features:

- **Graph-based Agent Orchestration** — State graph engine powered by `langgraph4j`, supporting 13 orchestratable node types
- **LiteFlow Workflow** — Rule engine for chat flow orchestration, supporting Direct / CoT / ToT strategies
- **RAG Knowledge Engine** — Document parsing + intelligent chunking + JVector HNSW vector retrieval + knowledge graph enhancement
- **Multi-platform LLM Adaptation** — Unified interface for OpenAI, Ollama, DeepSeek, SiliconFlow, OpenRouter
- **MCP Protocol Integration** — Tool service system based on Spring AI MCP
- **Multi-tenant RBAC** — Sa-Token authentication + tenant/workspace/role/menu permission system
- **Education Domain** — Bloom's taxonomy cognitive classification, Ebbinghaus forgetting curve review, intelligent exam generation, learning analytics
- **Observability** — OpenTelemetry + Micrometer + Prometheus full-stack tracing

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        shiyu-ai-bootstrap (:9000)                │
├────────┬────────┬──────────┬──────────┬──────────┬──────────────┤
│  auth  │ agent  │ education│ knowledge│  record  │    core      │
│  Auth  │ Agent  │Education │ Knowledge│  Record  │  AI Core     │
│  RBAC  │Engine  │  Domain  │  & RAG   │Management│ Chat/Model   │
├────────┴────────┴──────────┴──────────┴──────────┴──────────────┤
│                         shiyu-ai-dal (Data Access Layer)          │
│              DO / BO / Mapper / Repository                       │
├─────────────────────────────────────────────────────────────────┤
│                       shiyu-common (Common Infrastructure)        │
│     core │ web │ mybatis │ thread │ excel │ bom                  │
└─────────────────────────────────────────────────────────────────┘
```

### Agent Graph Orchestration Flow

```
User Input → AgentDefinition → Graph Compilation → StateGraph Execution →
  Intent Recognition(INTENT) → Conditional Branch(CONDITION) →
  LLM Call / Tool Call / RAG Retrieval / Memory Read-Write / Output Formatting
```

### RAG Retrieval Flow

```
User Query → Embedding Vectorization → JVector HNSW Retrieval →
  Knowledge Graph Context Enhancement → Context Concatenation → LLM Generation
```

---

## Module Description

### shiyu-ai-agent — Agent Orchestration Engine

Custom Agent state graph engine based on `langgraph4j`, supporting 13 orchestratable nodes:

| Node Type | Purpose |
|-----------|---------|
| `INTENT` | User intent recognition |
| `LLM_CALL` | LLM model invocation |
| `TOOL_CALL` | Tool function invocation |
| `RAG_RETRIEVAL` | Knowledge base retrieval |
| `RAG_ENHANCEMENT` | Post-retrieval enhancement |
| `SHORT_TERM_MEMORY` | Short-term memory read/write |
| `LONG_TERM_MEMORY` | Long-term memory read/write |
| `MEMORY_RETRIEVAL` | Cross-session memory retrieval |
| `CONDITION` | Conditional branch routing |
| `AGENT_CALL` | Sub-Agent invocation |
| `TRANSFORM` | Data transformation |
| `OUTPUT_FORMAT` | Output formatting |
| `DEFAULT` | Default node |

Core capabilities:
- **Dynamic Agent Registration** — Runtime registration/deregistration of Agent definitions
- **Version Management** — Multi-version coexistence with hot switching (DRAFT / PUBLISHED / ARCHIVED)
- **Sync/Stream Execution** — `POST /api/agent/{agentId}/execute` + SSE streaming endpoint
- **Node-level Retry & Timeout** — Independent retry strategy and timeout configuration per node
- **LiteFlow Workflow** — Complex flow orchestration for education scenarios (19 workflow components)

### shiyu-ai-core — AI Core

AI conversation engine and model management:

- **ChatEngine** — Unified conversation interface, supporting sync/stream + memory-augmented conversation
- **ModelManager** — Multi-platform model adapter management (loaded from DB at startup, supports hot updates)
- **MemoryService** — Short-term memory (conversation history) + long-term memory (persistence + importance decay)
- **ToolService** — MCP tool invocation service
- **EmbeddingService** — Local embedding model based on BGE-small-zh ONNX

### shiyu-ai-knowledge — Knowledge Engine

RAG retrieval-augmented generation and knowledge graph:

- **Document Management** — Knowledge point CRUD + relationship management (prerequisite/subsequent/includes/related/similar/belongs)
- **Vector Retrieval** — JVector-based (pure Java HNSW) vector storage with disk persistence
- **Knowledge Graph** — Graph-structured storage of knowledge point relationships, supporting parent/child/prerequisite/related queries
- **RAG Orchestration** — Vector retrieval + graph context enhancement → context concatenation → LLM generation
- **Chinese Chunking** — Document chunking strategy optimized for Chinese
- **Index Rebuild** — Asynchronous full vector index rebuild support

### shiyu-ai-education — Intelligent Education

AI tutoring system for K12 education scenarios:

- **Knowledge System** — Textbook/chapter/knowledge point three-level structure, supporting multiple textbook versions
- **Ability Assessment** — Bloom's taxonomy six cognitive dimensions (remember/understand/apply/analyze/evaluate/create)
- **Intelligent Exam Generation** — AI automatically generates exams based on weak knowledge points
- **Review Planning** — Ebbinghaus forgetting curve-driven spaced repetition (6-round review plan)
- **Learning Analytics** — Ability radar chart, learning trends, weak knowledge point analysis
- **Learning Path** — Personalized learning path recommendation based on knowledge graph

### shiyu-ai-auth — Authentication & Authorization

Enterprise-grade RBAC permission system:

- **Sa-Token** — Lightweight authentication framework, supporting login/permission/session management
- **Multi-tenant** — Tenant → Workspace → User three-level isolation
- **Permission Model** — User / Role / Menu / Workspace / Permission Code
- **Security Protection** — XSS filtering, CAPTCHA, login rate limiting, password encryption

### shiyu-ai-record — Record Management

Personal records and timeline:

- **Profile Management** — Profile management and member associations
- **Timeline** — Event timeline recording
- **Multimedia** — Image/video/audio attachment management
- **Tag System** — Flexible tag classification

### shiyu-ai-dal — Data Access Layer

Unified data access abstraction:

- **DO/BO Separation** — Data Objects (DO) and Business Objects (BO) layered separation
- **Repository Pattern** — Encapsulates MyBatis-Flex Mapper, returns BO externally
- **Multi-tenant Support** — Automatic injection of `tenant_id` filter conditions
- **H2/MySQL Dual Mode** — H2 file mode for development, MySQL for production

### shiyu-common — Common Infrastructure

| Sub-module | Functionality |
|------------|---------------|
| `core` | Unified `Result` response, pagination query, exception hierarchy, utilities, transaction hooks, event mechanism |
| `web` | XSS filtering, repeatable request stream, OpenAPI documentation, resource interceptor |
| `mybatis` | MyBatis-Flex encapsulation, `TenantEntity`, P6Spy SQL logging |
| `thread` | Thread pool management, virtual thread factory, OpenTelemetry context propagation, Micrometer metrics |
| `excel` | Excel import/export, dictionary conversion, enum conversion, cell merge strategy |
| `bom` | Maven BOM unified version declaration |

---

## Technology Stack

| Domain | Technology | Version |
|--------|------------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 4.1.0 |
| AI Framework | Spring AI | 2.0.0 |
| LLM Adaptation | LangChain4j | 1.16.3 |
| Agent Engine | LangGraph4j | 1.8.19 |
| Workflow Engine | LiteFlow | 2.16.0 |
| Authentication | Sa-Token | 1.45.0 |
| ORM | MyBatis-Flex | 1.11.7 |
| Database | H2 (dev) / MySQL (prod) | 2.4.240 / 9.4.0 |
| Connection Pool | Druid | 1.2.27 |
| Vector Search | JVector (HNSW) | 4.0.0-beta.6 |
| Embedding Model | BGE-small-zh (ONNX local) | — |
| Cache | Caffeine | 3.2.3 |
| Object Mapping | MapStruct-Plus + Lombok | 1.5.0 / 1.18.42 |
| Utilities | Hutool / Guava / Commons | 5.8.43 / 33.5.0 / 3.20.0 |
| API Docs | SpringDoc OpenAPI + Knife4j | 3.0.2 / 4.5.0 |
| Observability | OpenTelemetry + Micrometer + Prometheus | — |
| Reactive | Reactor (Flux) | — |
| Logging | Log4j2 | — |
| Scheduling | XXL-Job | 3.3.2 |
| Object Storage | AWS S3 SDK | 2.41.18 |
| Build | Maven | 3.8+ |

---

## Quick Start

### Prerequisites

- **JDK 21+** (project uses Java 21 features like virtual threads)
- **Maven 3.8+**
- **Git**

### Clone & Build

```bash
git clone https://github.com/ShiRuYu/shiyu-ai.git
cd shiyu-ai
mvn clean install -DskipTests
```

### Configure AI Platforms

Edit the configuration file `shiyu-ai-core/src/main/resources/config/config.yml`:

```yaml
shiyu:
  ai:
    ollama:
      base-url: http://localhost:11434
      model: gemma3:4b
    openai:
      base-url: https://api.openai.com/v1
      api-key: ${AI_OPENAI_API_KEY:}
    siliconflow:
      base-url: https://api.siliconflow.cn
      api-key: sk-xxxxxxxxxxxxxxxx
      model: THUDM/GLM-Z1-9B-0414
    deepseek:
      base-url: https://api.deepseek.com
      api-key: sk-xxxxxxxxxxxxxxxx
      model: deepseek-chat
```

### Start Application

The project is a monolithic application, started uniformly via `shiyu-ai-bootstrap`:

```bash
cd shiyu-ai-bootstrap
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

After startup, access:
- Application port: `http://localhost:9000`
- API documentation: `http://localhost:9000/doc.html`

### Maven Profiles

| Profile | Purpose | Default |
|---------|---------|---------|
| `dev` | Development environment (trace logging, H2 database) | ✅ |
| `prod` | Production environment (warn logging, MySQL database) | |

---

## Configuration Guide

### Application Configuration

```yaml
server:
  port: 9000

shiyu:
  ai:
    # Model platform configuration (can also be managed via database)
    ollama:
      base-url: http://localhost:11434
      model: gemma3:4b
    siliconflow:
      base-url: https://api.siliconflow.cn
      api-key: sk-xxx
      model: THUDM/GLM-Z1-9B-0414
  memory:
    enabled: true
    max-short-term-memories: 10
    max-long-term-memories: 50
  vector:
    type: hnsw          # hnsw / inmemory
    dimension: 512
    data-dir: ${app.home}/data/vector
```

### Environment Variables

| Variable | Description |
|----------|-------------|
| `AI_OPENAI_API_KEY` | OpenAI API Key |
| `APP_HOME` | Application data directory (H2 database, vector index storage location) |

---

## API Documentation

After starting the application, access:

```
http://localhost:9000/doc.html              # Knife4j documentation
http://localhost:9000/swagger-ui/index.html # Swagger UI
http://localhost:9000/v3/api-docs           # OpenAPI 3.0 JSON
```

APIs are grouped by module:

| Group | Path Prefix |
|-------|-------------|
| Agent | `/api/agent/**` |
| Authentication | `/api/auth/**` |
| Knowledge | `/api/knowledge/**` |
| Education | `/api/education/**` |
| Record | `/api/record/**` |
| System | `/api/system/**` |

---

## Development Guidelines

- **Lombok** — `@Data`, `@Slf4j`, `@RequiredArgsConstructor` to simplify code
- **MapStruct-Plus** — BO <-> VO object mapping
- **Jakarta Validation** — `@Valid` parameter validation groups (AddGroup / EditGroup / QueryGroup)
- **XSS Filtering** — Global XSS filter (Jsoup Safelist) to prevent cross-site scripting attacks
- **Unified Exception Handling** — `@ControllerAdvice` + business exception hierarchy + unified `Result<T>` response
- **Pagination Query** — `PageQuery` + `PageData<T>` unified pagination model
- **OpenTelemetry** — Thread pool context propagation + call chain tracing
- **Multi-tenant** — All business tables include `tenant_id` + `workspace_id`, automatic filtering

### Project Layering

```
controller/   <- REST interface layer (Request -> VO)
service/      <- Business logic layer (BO)
  impl/       <- Implementations
repository/   <- Repository layer (returns BO)
mapper/       <- MyBatis mapping interfaces (operates DO)
dal/
  dataobject/ <- Data Objects (DO, maps database rows)
  bo/         <- Business Objects (BO, returned by Repository)
  mapper/     <- Data access mappings
  repository/ <- Repository implementations
config/       <- Configuration classes
```

---

## Project Documentation

| Document | Description |
|----------|-------------|
| [Architecture Design Document (ADD)](./docs/architecture/shiyu-ai-architecture-design.md) | Enterprise architecture design, 20 chapters |
| [Refactoring Task List](./docs/refactoring-tasks.md) | 21 refactoring tasks with sub-task checklists |

---

## License

This project is licensed under the MIT License. See the [LICENSE](./LICENSE) file in the project root directory for details.

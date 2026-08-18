# ShiYu AI Platform

> **A multi-platform AI agent platform** — Built on a custom Agent orchestration engine, empowering rapid business agent development.
> Currently extended into two business domains: **Record (Personal Timeline & Media)** and **Education (Intelligent Tutoring)**.

---

## Positioning

ShiYu AI is not a single-purpose AI application. It is an **AI agent platform that connects to multiple LLM providers, centers on a customizable Agent engine, and extends into various business directions**.

The root Maven reactor currently contains 19 buildable modules. Version management is centralized in the root POM; the Observation shell and Excel module are outside the default build, while the Thread module remains active as the Knowledge Worker foundation.

```
                     ┌──────────────────┐
                     │ Multi-LLM Access │
                     │ OpenAI / DeepSeek│
                     │ Ollama / Silicon │
                     │  Flow / More...  │
                     └────────┬─────────┘
                              │ Unified Adapter
                     ┌────────▼─────────┐
                     │   Agent Engine   │ ◄── Platform Core
                     │ Graph · 13 Nodes │
                     │ Memory · Tools · │
                     │   RAG · Retry    │
                     └────────┬─────────┘
                              │
              ┌───────────────┼───────────────────┐
              │               │                   │
     ┌────────▼──────┐  ┌────▼────┐      ┌──────▼──────┐
     │  Record       │  │Education│      │   More...    │
     │  Timeline     │  │  K-12   │      │  Future      │
     │  Media · Tags │  │  Tutoring│      │  Extensions  │
     └───────────────┘  └─────────┘      └─────────────┘

         ▲  Platform Infrastructure: Knowledge · Vector Store · Usage
         │  Memory · Plugins · Tools · MCP · Model Management
         └────────────────────────────────────────────────┘
```

---

## Platform Infrastructure

Beneath the Agent engine, the platform provides a full suite of infrastructure capabilities that power Agent nodes and business extensions.

### Knowledge Engine (`shiyu-ai-knowledge`)

All-in-one knowledge service covering document management, RAG retrieval, and knowledge graphs:

- **Document Management** — Knowledge point CRUD + relationship management (prerequisite/subsequent/includes/related/similar/belongs)
- **Vector Retrieval** — Uses the vector module's isolated public API for HNSW retrieval
- **Knowledge Graph** — Graph-structured storage of knowledge point relationships
- **RAG Orchestration** — Vector retrieval → graph context enhancement → context assembly → LLM generation
- **Chinese Chunking** — Document splitting strategy optimized for Chinese text
- **Index Rebuild** — Asynchronous full vector index rebuild support

### Vector Store (`shiyu-ai-vector`)

Backend-neutral `VectorStore` and `VectorStoreProvider` APIs, backed by **JVector (pure Java HNSW)** by default and InMemory for tests and lightweight use cases:

- **HNSW Index** — Efficient approximate nearest neighbor search
- **Disk Persistence** — Vector indexes persisted to disk, survivable across restarts
- **Configurable Dimensions** — Dynamic vector dimension configuration (default 512)
- **Multiple Search Strategies** — Exact search + approximate search
- **Full CRUD** — Complete create/read/update/delete for vectors
- **Namespace Isolation** — Opens independent stores per tenant, knowledge space, and index version
- **Stable Module Boundary** — Knowledge and Memory depend only on public vector APIs
- **Pluggable Backends** — ChromaDB or Milvus can be added later as provider adapters

### Memory System (`shiyu-ai-memory`)

Two-tier intelligent memory service:

- **Short-term Memory** — Conversation context management with automatic truncation for coherent sessions
- **Long-term Memory** — Persistent storage with importance decay, retaining key information long-term
- **Cross-session Retrieval** — Retrieve relevant historical memories across different sessions
- **Compression Strategy** — Intelligent compression of long conversation histories (summarization/truncation)
- **SPI Extension** — Customizable memory storage backends

### Tool System — Tool & MCP (`shiyu-ai-tool`)

Standardized tool invocation service based on the **Spring AI MCP protocol**:

- **MCP Protocol Integration** — Standardized tool description and invocation protocol
- **Tool Registration** — Runtime register/deregister/update tool definitions
- **Tool Execution** — Secure sandboxed tool execution environment
- **Dynamic Discovery** — Auto-discover and register tools from MCP servers
- **Agent Integration** — Agent `TOOL_CALL` nodes directly invoke registered tools

### Model Management (`shiyu-ai-model`)

Unified multi-platform LLM model adaptation and management:

- **Multi-platform Adaptation** — OpenAI / DeepSeek / Ollama / SiliconFlow / OpenRouter
- **Hot Reload** — Dynamic platform switching at runtime, zero downtime
- **Model Routing** — Route to different models by scenario or tenant
- **Resilience** — Circuit breaking, backoff retry, graceful degradation
- **Embedding Model** — Built-in BGE-small-zh ONNX local embedding, zero external dependencies

### Usage Tracking (`shiyu-ai-usage`)

Full-stack usage metering and billing:

- **Token Metering** — Precise input/output token counting
- **Request Logging** — Complete records for every Agent / LLM / tool invocation
- **Real-time Push** — WebSocket-based live usage data streaming
- **Multi-dimensional Statistics** — Aggregated by user, tenant, model, time period

### Plugin System (`shiyu-ai-plugin`)

Lightweight plugin extension framework:

- **Plugin Lifecycle** — Full lifecycle management: load → enable → disable → unload
- **Sandbox Isolation** — Plugin execution in sandbox for platform security
- **SPI Registration** — SPI-based plugin discovery and registration
- **Dynamic Hot-Plug** — Install/uninstall plugins at runtime, no restart required

---

## Core Architecture

### Platform Access Layer — Multi-LLM Connectivity

A unified ModelManager adapter mechanism connects to all major LLM platforms through a single API:

| Platform | Access Method |
|----------|---------------|
| **OpenAI** | Standard OpenAI API |
| **DeepSeek** | DeepSeek API |
| **Ollama** | Local private deployment |
| **SiliconFlow** | China-accelerated access |
| **OpenRouter** | Multi-model routing |
| **More...** | Extensible adapters |

Platform configurations support **startup loading** and **runtime hot-reload** — switch models with zero downtime.

### Agent Engine — Graph-Based Custom Agent Building

Powered by a `langgraph4j` state graph engine with 13 pluggable node types — build Agents like assembling building blocks:

| Node Type | Purpose |
|-----------|---------|
| `INTENT` | User intent recognition & routing |
| `LLM_CALL` | LLM model invocation |
| `TOOL_CALL` | MCP tool invocation |
| `RAG_RETRIEVAL` | Knowledge base retrieval |
| `RAG_ENHANCEMENT` | Post-retrieval enhancement |
| `SHORT_TERM_MEMORY` | Short-term memory read/write |
| `LONG_TERM_MEMORY` | Long-term memory read/write |
| `MEMORY_RETRIEVAL` | Cross-session memory retrieval |
| `CONDITION` | Conditional branch routing |
| `AGENT_CALL` | Sub-Agent invocation (Agent nesting) |
| `TRANSFORM` | Data transformation |
| `OUTPUT_FORMAT` | Output formatting |
| `DEFAULT` | Default handler |

Engine capabilities:

- **Dynamic Registration** — Register/deregister/update Agents at runtime, no restart required
- **Version Management** — DRAFT / PUBLISHED / ARCHIVED multi-version coexistence
- **Sync/Stream Execution** — REST sync calls + SSE streaming output
- **Node-level Retry & Timeout** — Independent configuration per node
- **Nested Agents** — Agent within Agent for complex task decomposition
- **Execution Lifecycle** — PENDING → RUNNING → PAUSED → ... → COMPLETED / FAILED
- **Checkpoint Mechanism** — Node-level execution snapshots supporting pause/resume

---

## Business Extensions

### Extension 1: Record — Personal Timeline & Media

A lightweight **personal record and timeline** management system for journaling, note-taking, event archiving, and more.

- **Profiles** — Person/character profile management with member associations
- **Timeline** — Chronological event recording and timeline display
- **Media Management** — Image / video / audio attachment upload and management
- **Tag System** — Flexible classification and filtering with tags

### Extension 2: Education — Intelligent K-12 Tutoring

An **AI-powered tutoring system** for K-12 education, covering the full "learn → practice → test → evaluate → recommend" loop.

- **Knowledge System** — Textbook → Chapter → Knowledge Point hierarchy, multi-version textbooks
- **Ability Assessment** — Bloom's Taxonomy (remember/understand/apply/analyze/evaluate/create)
- **Intelligent Exam Generation** — AI generates exams targeting weak knowledge points
- **Review Planning** — Ebbinghaus Forgetting Curve driven 6-round spaced repetition
- **Learning Analytics** — Radar charts, learning trends, weak-point analysis
- **Learning Path** — Personalized path recommendation via knowledge graph

---

## Module Overview

### 🎯 Business Layer (scenario-facing capabilities)

| Module | Responsibility | Category |
|--------|---------------|----------|
| `shiyu-ai-education` | **Education Business**: Learn/Practice/Exam/Evaluate/Recommend + education-specific Agents (exam generation / review / report / teaching nodes) | **Business Extension** |
| `shiyu-ai-record` | **Record Business**: Profiles, Timeline, Media, Tags | **Business Extension** |

### ⚙️ Platform Layer (reusable AI capabilities)

| Module | Responsibility | Category |
|--------|---------------|----------|
| `shiyu-ai-agent` | **Agent Engine**: Graph orchestration, Node system, Execution lifecycle, Checkpoint, Retry/Timeout | **Platform Core** |
| `shiyu-ai-auth` | Auth & RBAC: Sa-Token, Multi-tenant | Platform Foundation |
| `shiyu-ai-model` | Model Management: Multi-platform adapters, Hot reload, Resilience, Embedding | Platform Infrastructure |
| `shiyu-ai-knowledge` | Knowledge Engine: Document management, RAG retrieval, Knowledge graph, Chunking, Retrieval/Audit/Evaluation | Platform Infrastructure |
| `shiyu-ai-vector` | Vector Store: JVector HNSW index, Disk persistence, Unified Provider API | Platform Infrastructure |
| `shiyu-ai-memory` | Memory System: Short/long-term memory, Compression, Cross-session retrieval, SPI | Platform Infrastructure |
| `shiyu-ai-tool` | Tool System: MCP protocol, Tool registration/invocation/execution | Platform Infrastructure |
| `shiyu-ai-plugin` | Plugin System: Lifecycle management, Sandbox isolation, Hot-plug | Platform Infrastructure |
| `shiyu-ai-usage` | Usage Tracking: Token metering, Real-time push, Multi-dimensional aggregation | Platform Infrastructure |

### 🧱 Infrastructure Layer (technology foundation)

| Module | Responsibility | Category |
|--------|---------------|----------|
| `shiyu-common/*` | Common: core (utils/Result/exceptions), web (XSS), mybatis (ORM), thread (pools), excel, storage (file storage) | Infrastructure |
| `shiyu-ai-dal` | Data Access Implementation: DO/Mapper/Repository impl + Flyway migrations | Infrastructure |
| `shiyu-ai-web` | REST adapters: Controllers, DTOs, WebSocket, OpenAPI | Infrastructure |
| `shiyu-ai-bootstrap` | Application boot entry: logging/observability/data retention | Infrastructure |

## Technology Stack

| Domain | Technology |
|--------|------------|
| Language | Java 21 (Virtual Threads) |
| Framework | Spring Boot 4.1 |
| AI Framework | Spring AI 2.0 + LangChain4j |
| Agent Engine | LangGraph4j |
| Agent Flow Graph | LangGraph4j + BaseNode |
| Authentication | Sa-Token |
| ORM | MyBatis-Flex |
| Database | H2 (dev) / MySQL (prod) |
| Vector Search | JVector (HNSW) |
| Embedding Model | BGE-small-zh (ONNX local) |
| MCP Protocol | Spring AI MCP |
| Cache | Caffeine |
| API Docs | SpringDoc OpenAPI (UI is an optional profile) |
| Observability | OpenTelemetry + Micrometer + Prometheus |
| Logging | Log4j2 |
| Scheduling | XXL-Job |

---

## Quick Start

### Prerequisites

- JDK 21+
- Maven 3.8+

### Clone & Build

```bash
git clone https://github.com/ShiRuYu/shiyu-ai.git
cd shiyu-ai
mvn clean install -DskipTests
```

### Configure an LLM Platform

Edit `infrastructure/shiyu-ai-bootstrap/src/main/resources/application.yml` and configure at least one platform:

```yaml
shiyu:
  ai:
    ollama:
      base-url: http://localhost:11434
      model: gemma3:4b
    deepseek:
      base-url: https://api.deepseek.com
      api-key: ${AI_DEEPSEEK_API_KEY:}
      model: deepseek-v4-flash
```

### Start

```bash
cd infrastructure/shiyu-ai-bootstrap
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Maven profiles (optional capability switches)

`infrastructure/shiyu-ai-bootstrap/pom.xml` defines three optional Maven profiles
that load dependencies on demand (excluded from production packages by default):

| Profile | Dependencies added | Purpose |
|---------|--------------------|---------|
| `observability` | `spring-boot-starter-actuator`, `spring-boot-starter-opentelemetry`, `micrometer-registry-prometheus` | Health/metrics endpoints, distributed tracing, Prometheus metrics |
| `api-docs-ui` | `springdoc-openapi-starter-webmvc-ui` | Swagger UI at `/swagger-ui.html`; without it the JSON docs at `/v3/api-docs` still work |
| `s3` | `software.amazon.awssdk:s3` | Object storage S3 SDK; needed when the storage type is `s3` / `minio` / `aliyun-oss` / `tencent-cos` |

Activation examples:

```bash
# Package with observability enabled
mvn -Pobservability -pl infrastructure/shiyu-ai-bootstrap -am -DskipTests package
# Run with several profiles at once
mvn -Pobservability,api-docs-ui,s3 spring-boot:run
```

Notes:

1. **`api-docs-ui` is active by default (`activeByDefault=true`)** — a plain
   `mvn spring-boot:run` gives you the Swagger UI. However, once any `-P` flag
   is given explicitly (e.g. `-Pobservability` or the root `-Pprod`), it is
   automatically disabled and must be listed explicitly again.
2. **Maven profile ≠ Spring profile** — `observability` only adds dependencies
   to the classpath; the matching `application-observability.yml` is loaded via
   `spring.config.activate.on-profile: observability`, so pass the Spring
   profile at runtime too:
   ```bash
   mvn -Pobservability spring-boot:run \
     -Dspring-boot.run.arguments="--spring.profiles.active=dev,observability"
   ```
3. **Relation to `dev` / `prod`** — the root POM's `dev` / `prod` profiles only
   set the environment id (`spring.profiles.active`, mapped to
   `application-dev.yml` / `application-prod.yml`) and are independent of the
   three switches above, so they can be combined freely (e.g.
   `-Pprod,observability,api-docs-ui`).

### Release packages

```powershell
# Windows cloud/base package
./scripts/package-cloud-windows.ps1
# Windows offline-model package (includes optional BGE/ONNX dependencies)
./scripts/package-offline-windows.ps1
```

On Linux, use the corresponding `scripts/package-cloud-linux.sh` and
`scripts/package-offline-linux.sh`. Production packages exclude observability,
the API documentation UI, and the S3 adapter by default; enable the
`observability`, `api-docs-ui`, or `s3` profiles explicitly when needed.

After startup:
- Application: `http://localhost:9000`
- API docs JSON: `http://localhost:9000/v3/api-docs`
- API docs UI: available only with the `api-docs-ui` profile

---

## API Documentation

| Group | Path Prefix | Category |
|-------|-------------|----------|
| Agent | `/v1/agents/**`, `/v1/agent-versions/**`, `/v1/agent-executions/**` | Platform Core |
| Model | `/v1/platform/models/**`, `/v1/platform/providers/**` | Platform Infrastructure |
| Knowledge | `/v1/knowledge/**` | Platform Infrastructure |
| Memory | `/memory/**` | Platform Infrastructure |
| Auth | `/v1/auth/**`, `/v1/system/users/**`, `/v1/system/roles/**`, `/v1/system/menus/**`, `/v1/system/tenants/**` | Platform Foundation |
| Usage | `/v1/usage/**` | Platform Infrastructure |
| Plugin | `/v1/plugins/**` | Platform Infrastructure |
| Education | `/v1/education/**` | Business Extension |
| Record | `/v1/record/**` | Business Extension |
| System | `/v1/system/**` | Infrastructure |

---

## Roadmap

```
Phase 1 (Complete)    Platform Foundation
  ├── Multi-LLM Access            ✅
  ├── Agent Graph Engine          ✅
  ├── Knowledge & RAG Engine      ✅
  ├── Vector Store                ✅
  ├── Memory System               ✅
  ├── MCP Tool System             ✅
  ├── Plugin System               ✅
  ├── Usage Tracking              ✅
  └── Multi-tenant RBAC           ✅

Phase 2 (Current)      Business Extensions
  ├── Record Management           ✅ Launched
  ├── Education Tutoring          ✅ Launched
  └── More directions...          🔜 Upcoming

Phase 3 (Planned)      Platform Enhancement
  ├── Agent Marketplace / Templates
  ├── Visual Orchestration UI
  ├── Richer MCP Tool Ecosystem
  └── Performance & Observability Deepening
```

---

## License

This project is licensed under the MIT License. See [LICENSE](./LICENSE) for details.

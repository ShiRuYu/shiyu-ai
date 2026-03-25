                ┌──────────────────────────┐
                │      Agent API 层        │
                └──────────┬───────────────┘
                           │
                ┌──────────▼───────────────┐
                │     Agent Engine         │
                │ (基于 LangGraph4j 封装)  │
                └──────────┬───────────────┘
                           │
     ┌─────────────────────┼─────────────────────┐
     │                     │                     │
┌────▼────┐        ┌──────▼─────┐       ┌───────▼──────┐
│ Tool系统 │        │ Memory系统 │       │  RAG系统      │
└────┬────┘        └──────┬─────┘       └───────┬──────┘
│                    │                     │
└──────────────┬─────┴──────────────┬─────┘
▼                    ▼
Model Router       Observability



public class AgentDefinition {

    private String name;

    private GraphDefinition graph;

    private List<String> tools;

    private MemoryConfig memory;

    private ModelConfig model;
}
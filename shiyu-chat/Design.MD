# AI Chat Backend Project Skeleton (Java + Spring Boot + LiteFlow + Timefold)

## 1. 项目目录结构

```
shiyu-chat/
 ├─ core/
 │   ├─ model/
 │   │   ├─ ModelAdapter.java         # 模型适配接口
 │   │   ├─ OpenAIAdapter.java        # OpenAI 模型实现
 │   │   └─ LocalLLMAdapter.java      # 本地模型实现
 │   ├─ strategy/
 │   │   ├─ CoTStrategy.java          # CoT 推理实现
 │   │   ├─ ToTStrategy.java          # ToT 推理实现
 │   │   └─ ReActStrategy.java        # ReAct 推理实现
 │   ├─ intent/
 │   │   └─ IntentService.java       # 意图识别
 │   ├─ context/
 │   │   └─ ChatContext.java       # 对话上下文管理
 │   └─ scorer/
 │       └─ ResultScorer.java        # 输出结果评分
 │
 ├─ liteflow/
 │   ├─ components/
 │   │   ├─ IntentCmp.java            # LiteFlow INTENT 节点实现
 │   │   ├─ StrategySelectCmp.java    # 策略选择节点
 │   │   ├─ ModelCallCmp.java         # 模型调用节点
 │   │   └─ TotSolveCmp.java          # Timefold 优化节点
 │   ├─ chain/chain.json              # LiteFlow 主链配置（JSON）
 │   └─ config/LiteFlowConfig.java    # LiteFlow 配置加载
 │
 ├─ solver/
 │   ├─ entity/
 │   │   ├─ ThoughtNode.java          # 思维节点定义
 │   │   └─ ThoughtSolution.java      # 求解方案定义
 │   ├─ service/
 │   │   └─ ThoughtSolverService.java # Timefold 求解器服务
 │   └─ config/SolverConfig.java      # Solver 配置
 │
 ├─ api/
 │   ├─ controller/ChatController.java # REST 接口
 │   ├─ dto/
 │   │   ├─ ChatRequest.java
 │   │   └─ ChatResponse.java
 │   └─ service/ChatEngine.java        # 对话引擎封装
 │
 ├─ common/
 │   ├─ util/LoggingUtil.java
 │   └─ config/AppConfig.java
 │
 ├─ resources/
 │   ├─ liteflow/chain/chain.json
 │   └─ application.yml
 └─ pom.xml
```

## 2. 核心示例类

### 2.1 ModelAdapter 接口

```java
public interface ModelAdapter {
    LMResponse generate(LMRequest request);
}
```

### 2.2 LiteFlow 节点示例（使用 GlobalContext）

```java
@LiteflowComponent("TOT_SOLVE")
public class ToTSolveCmp extends NodeComponent {

    @Autowired
    private ThoughtSolverService solverService;

    @Override
    public void process() throws Exception {
        GlobalContext context = this.getContextBean(GlobalContext.class);
        List<ThoughtNode> nodes = context.get("thoughtNodes");
        ThoughtSolution best = solverService.solve(nodes);
        context.set(GlobalContext.ChatBizKeyEnum.TOT_BEST_THOUGHT.getCode(), best);
    }

    @Override
    public boolean isContinueOnError() { return true; }
}
```

### 2.3 REST 控制器示例

```java
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatEngine chatEngine;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return chatEngine.process(request);
    }
}

```


### 2.4 Timefold 求解服务

```java
@LiteflowComponent("TOT_SOLVE")
@Service
public class ThoughtSolverService {

    public ThoughtSolution solve(List<ThoughtNode> nodes) {
        SolverFactory<ThoughtSolution> factory = SolverFactory.create(
                new SolverConfig()
                        .withSolutionClass(ThoughtSolution.class)
                        .withEntityClasses(ThoughtNode.class)
                        .withScoreDirectorFactory(
                                new ScoreDirectorFactoryConfig()
                                        .withEasyScoreCalculatorClass(ThoughtScoreCalculator.class)
                        )
        );

        Solver<ThoughtSolution> solver = factory.buildSolver();
        ThoughtSolution solution = new ThoughtSolution();
        solution.setNodes(nodes);

        return solver.solve(solution);
    }
}
```

### 3. LiteFlow JSON 配置示例
文件路径：resources/liteflow/chain/chain.json

每个链通过 nodes 列表定义执行节点顺序

节点 id 对应 LiteFlow 组件 @LiteflowComponent 注解
```json
{
  "chains": [
    {
      "name": "chatFlow",
      "nodes": [
        {"id": "INTENT"},
        {"id": "STRATEGY_SELECT"},
        {"id": "CHAIN_EXECUTE"}
      ]
    },
    {
      "name": "chain.tot",
      "nodes": [
        {"id": "MODEL_CALL_TOT"},
        {"id": "TOT_SOLVE"}
      ]
    },
    {
      "name": "chain.cot",
      "nodes": [
        {"id": "MODEL_CALL_TOT"}
      ]
    }
  ]
}

```

## 5. 使用说明
1. 在 application.yml 中配置 LiteFlow 配置路径和模型参数
2. 启动 Spring Boot 应用，访问 REST 接口 /chat 进行对话
3. LiteFlow 自动加载流程，选择策略链执行 CoT / ToT / ReAct。
4. ToT 流程通过 Timefold 优化多分支思维路径。
5. 输出最终结果到客户端，保存在 GlobalContext 的 TOT_BEST_THOUGHT key。

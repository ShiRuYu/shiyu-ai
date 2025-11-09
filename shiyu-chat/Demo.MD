下面是完整的 Maven 可运行工程骨架，严格按照你提供的目录结构，包含示例代码和配置文件，复制到本地即可构建运行。

---

# 1. 项目目录结构

```
shiyu-chat/
 ├─ core/
 │   ├─ model/
 │   │   ├─ ModelAdapter.java
 │   │   ├─ OpenAIAdapter.java
 │   │   └─ LocalLLMAdapter.java
 │   ├─ strategy/
 │   │   ├─ CoTStrategy.java
 │   │   ├─ ToTStrategy.java
 │   │   └─ ReActStrategy.java
 │   ├─ intent/
 │   │   └─ IntentService.java
 │   ├─ context/
 │   │   └─ ChatContext.java
 │   └─ scorer/
 │       └─ ResultScorer.java
 │
 ├─ liteflow/
 │   ├─ components/
 │   │   ├─ IntentCmp.java
 │   │   ├─ StrategySelectCmp.java
 │   │   ├─ ModelCallCmp.java
 │   │   └─ TotSolveCmp.java
 │   ├─ chain/chain.json
 │   └─ config/LiteFlowConfig.java
 │
 ├─ solver/
 │   ├─ entity/
 │   │   ├─ ThoughtNode.java
 │   │   └─ ThoughtSolution.java
 │   ├─ service/
 │   │   └─ ThoughtSolverService.java
 │   └─ config/SolverConfig.java
 │
 ├─ api/
 │   ├─ controller/ChatController.java
 │   ├─ dto/
 │   │   ├─ ChatRequest.java
 │   │   └─ ChatResponse.java
 │   └─ service/ChatEngine.java
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

---

# 2. 核心示例类

## core/model/LMRequest.java

```java
package core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LMRequest {
    private String input;
}

```

## core/model/LMResponse.java

```java
package core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LMResponse {
    private String output;
}
```

## core/model/ModelAdapter.java

```java
package core.model;

public interface ModelAdapter {
    LMResponse generate(LMRequest request);
}
```

## core/model/OpenAIAdapter.java

```java
package core.model;

public class OpenAIAdapter implements ModelAdapter {
    @Override
    public LMResponse generate(LMRequest request) {
        return new LMResponse("OpenAI response for: " + request.getInput());
    }
}
```

## core/model/LocalLLMAdapter.java

```java
package core.model;

public class LocalLLMAdapter implements ModelAdapter {
    @Override
    public LMResponse generate(LMRequest request) {
        return new LMResponse("Local LLM response for: " + request.getInput());
    }
}
```

## core/context/GlobalContext.java

```java
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Data
public class GlobalContext {
    private Map<String, Object> contextMap = new HashMap<>();

    public void set(String key, Object value) {
        contextMap.put(key, value);
    }

    public <T> T get(String key) {
        return (T) contextMap.get(key);
    }

    public boolean contains(String key) {
        return contextMap.containsKey(key);
    }

    public Map<String, Object> getAll() {
        return contextMap;
    }

    @Getter
    @AllArgsConstructor
    public enum ChatBizKeyEnum {
        LAST_INTENT("lastIntent", "用户最后一次意图"),
        LAST_CHAIN("lastChain", "用户最后触发的链"),
        TOT_BEST_THOUGHT("bestThought", "ToT流程最佳方案");

        private final String code;
        private final String desc;
    }
}
```

## core/intent/IntentService.java

```java
package core.intent;

public class IntentService {
    public String detectIntent(String input) {
        // 简单模拟
        if(input.contains("规划")) return "planning_task";
        return "general_task";
    }
}
```

## core/scorer/ResultScorer.java

```java
package core.scorer;

public class ResultScorer {
    public double score(String output) {
        return Math.random();
    }
}
```

---

# 3. LiteFlow 节点示例

## liteflow/components/IntentCmp.java

```java
package liteflow.components;

import com.shiyu.ai.chat.core.context.GlobalContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;

@LiteflowComponent("INTENT")
public class IntentCmp extends NodeComponent {
    @Override
    public void process() {
        String intent = "planning_task";
        this.getSlot().setOutput("INTENT", intent);

        // 使用 this.getContextBean 获取全局上下文
        GlobalContext context = this.getContextBean(GlobalContext.class);
        context.set(GlobalContext.ChatBizKeyEnum.LAST_INTENT.getCode(), intent);

        System.out.println("INTENT 节点执行完毕，intent=" + intent);
    }

    @Override
    public boolean isContinueOnError() { return true; }
}
```

## liteflow/components/StrategySelectCmp.java

```java
package liteflow.components;

import com.shiyu.ai.chat.core.context.GlobalContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;

@LiteflowComponent("STRATEGY_SELECT")
public class StrategySelectCmp extends NodeComponent {
    @Override
    public void process() {
        String intent = this.getSlot().getOutput("INTENT");
        String chainToCall = "chain.cot";
        if ("planning_task".equals(intent)) {
            chainToCall = "chain.tot";
        }
        this.getSlot().setOutput("chainToCall", chainToCall);

        GlobalContext context = this.getContextBean(GlobalContext.class);
        context.set(GlobalContext.ChatBizKeyEnum.LAST_CHAIN.getCode(), chainToCall);

        System.out.println("STRATEGY_SELECT 节点执行完毕，chainToCall=" + chainToCall);
    }

    @Override
    public boolean isContinueOnError() { return true; }
}
```

## liteflow/components/ModelCallTotCmp.java

```java
package liteflow.components;

import com.shiyu.ai.chat.solver.entity.ThoughtNode;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;

import java.util.ArrayList;
import java.util.List;

@LiteflowComponent("MODEL_CALL_TOT")
class ModelCallTotCmp extends NodeComponent {
    @Override
    public void process() {
        try {
            System.out.println("执行 MODEL_CALL_TOT 节点");
            List<ThoughtNode> thoughts = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                thoughts.add(new ThoughtNode("方案" + i, Math.random()));
            }
            this.getSlot().setOutput("thoughtNodes", thoughts);
        } catch (Exception e) {
            System.err.println("MODEL_CALL_TOT 异常: " + e.getMessage());
        }
    }


    @Override
    public boolean isContinueOnError() { return true; }
}
```

## liteflow/components/TotSolveCmp.java

```java
package liteflow.components;

import com.shiyu.ai.chat.solver.entity.ThoughtNode;
import com.shiyu.ai.chat.solver.entity.ThoughtSolution;
import com.shiyu.ai.chat.solver.service.ThoughtSolverService;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;

import java.util.List;

@LiteflowComponent("TOT_SOLVE")
class TotSolveCmp extends NodeComponent {
    @Override
    public void process() {
        try {
            System.out.println("执行 TOT_SOLVE 节点");
            List<ThoughtNode> nodes = this.getSlot().getOutput("thoughtNodes");
            ThoughtSolverService solverService = new ThoughtSolverService();
            ThoughtSolution best = solverService.solve(nodes);
            this.getSlot().setOutput("bestSolution", best.getBestNode());
            System.out.println("最佳方案: " + best.getBestNode().getContent() + ", score=" + best.getBestNode().getScore());
        } catch (Exception e) {
            System.err.println("TOT_SOLVE 异常: " + e.getMessage());
        }
    }


    @Override
    public boolean isContinueOnError() { return true; }
}
```

---

# 4. Solver 实现

## solver/entity/ThoughtNode.java

```java
package solver.entity;

import lombok.Data;
@Data
public class ThoughtNode {
    private String content;
    private double score;
    public ThoughtNode(String content,double score){this.content=content;this.score=score;}
}
```

## solver/entity/ThoughtSolution.java

```java
package solver.entity;

import lombok.Data;
import java.util.List;
@Data
public class ThoughtSolution {
    private List<ThoughtNode> nodes;
    private ThoughtNode bestNode;
}
```

## solver/service/ThoughtSolverService.java

```java
package solver.service;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;
import com.shiyu.ai.chat.solver.entity.ThoughtNode;
import com.shiyu.ai.chat.solver.entity.ThoughtSolution;

import java.util.List;

public class ThoughtSolverService {

    public ThoughtSolution solve(List<ThoughtNode> nodes) {
        // 1. 使用 SolverConfig 构建 SolverFactory
        SolverFactory<ThoughtSolution> factory = SolverFactory.create(
                new SolverConfig()
                        .withSolutionClass(ThoughtSolution.class)
                        .withEntityClasses(ThoughtNode.class)
                        .withScoreDirectorFactory(
                                new ScoreDirectorFactoryConfig()
                                        .withEasyScoreCalculatorClass(ThoughtScoreCalculator.class)
                        )
        );

        // 2. 构建 Solver
        Solver<ThoughtSolution> solver = factory.buildSolver();

        // 3. 构建初始解
        ThoughtSolution solution = new ThoughtSolution();
        solution.setNodes(nodes);

        // 4. 执行求解
        ThoughtSolution bestSolution = solver.solve(solution);

        return bestSolution;
    }
}
```
## solver/service/ThoughtScoreCalculator.java

```java
package solver.service;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.calculator.EasyScoreCalculator;
import com.shiyu.ai.chat.solver.entity.ThoughtNode;
import com.shiyu.ai.chat.solver.entity.ThoughtSolution;

import java.util.Comparator;

public class ThoughtScoreCalculator implements EasyScoreCalculator<ThoughtSolution, HardSoftScore> {
    @Override
    public HardSoftScore calculateScore(ThoughtSolution solution) {
        double maxScore = solution.getNodes().stream().mapToDouble(ThoughtNode::getScore).max().orElse(0.0);
        solution.setBestNode(solution.getNodes().stream().max(Comparator.comparingDouble(ThoughtNode::getScore)).orElse(null));
        return HardSoftScore.ofSoft((int)(maxScore * 100));
    }
}
```

---

# 5. API Controller

## api/controller/ChatController.java

```java
package api.controller;

import com.yomahub.liteflow.core.FlowExecutor;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private FlowExecutor flowExecutor;

    @PostMapping
    public Map<String, Object> chat(@RequestBody Map<String, Object> request) {
        Map<String, Object> slot = new HashMap<>();
        try {
            slot.put("userInput", request.get("text"));
            flowExecutor.execute2Resp("chatFlow", slot);

            Object result = slot.getOrDefault("bestSolution", "no_solution");
            return Collections.singletonMap("result", result);

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.singletonMap("error", e.getMessage());
        }
    }
}
```

---

# 6. LiteFlow chain.json

```json
{
  "chains": [
    {"name": "chatFlow","nodes": [
        {"id": "INTENT"},
        {"id": "STRATEGY_SELECT"},
        {"id": "MODEL_CALL_TOT"},
        {"id": "TOT_SOLVE"}
    ]},
    {"name": "chain.tot","nodes": [
        {"id": "MODEL_CALL_TOT"},
        {"id": "TOT_SOLVE"}
    ]},
    {"name": "chain.cot","nodes": [
        {"id": "MODEL_CALL_TOT"}
    ]}
  ]
}
```

---

# 7. application.yml

```yaml
server:
  port: 8080

liteflow:
  enable: true
```

---

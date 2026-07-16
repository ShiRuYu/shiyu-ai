package com.shiyu.ai.agent.workflow.component;

import com.shiyu.ai.agent.graph.Graph;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeFactory;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.workflow.context.TutorContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 辅导检查组件
 * <p>
 * 利用已通过 NodeCreator 注册的教育节点，通过 NodeFactory 动态构建辅导图。
 * 图拓扑缓存，避免每次 process() 重复创建。
 */
@Slf4j
@Component("tutorCheckCmp")
public class TutorCheckCmp extends NodeComponent {

    private final NodeFactory nodeFactory;
    private final AtomicReference<Graph> cachedGraph = new AtomicReference<>();

    public TutorCheckCmp(NodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }

    @Override
    public void process() throws Exception {
        TutorContext ctx = this.getContextBean(TutorContext.class);
        log.info("TutorCheckCmp: 执行辅导, studentId={}, knowledgeId={}",
                ctx.getStudentId(), ctx.getKnowledgeId());

        Graph g = cachedGraph.get();
        if (g == null) {
            synchronized (cachedGraph) {
                g = cachedGraph.get();
                if (g == null) {
                    g = buildTutorGraph();
                    cachedGraph.set(g);
                }
            }
        }

        HashMap<String, Object> input = new HashMap<>();
        input.put("studentId", ctx.getStudentId());
        input.put("knowledgeId", ctx.getKnowledgeId());
        try {
            var result = g.execute(input);
            ctx.setTeachContent((String) result.getOrDefault("teachContent", ""));
            ctx.setPracticeScore((Double) result.getOrDefault("practiceScore", 60.0));
            ctx.setReviewNeeded((Boolean) result.getOrDefault("reviewNeeded", false));
            log.info("TutorCheckCmp: 辅导完成");
        } catch (Exception e) {
            log.error("辅导执行失败", e);
        }
    }

    private Graph buildTutorGraph() {
        Graph g = new Graph();
        g.setName("tutorGraph");

        g.addNode("abilityQuery",   createNode(NodeType.ABILITY_QUERY));
        g.addNode("teach",          createNode(NodeType.EDUCATION_TEACH));
        g.addNode("practice",       createNode(NodeType.EDUCATION_PRACTICE));
        g.addNode("scoreAnalysis",  createNode(NodeType.SCORE_ANALYSIS));
        g.addNode("reviewSchedule", createNode(NodeType.REVIEW_SCHEDULE));

        g.addEdge("abilityQuery", "teach");
        g.addEdge("teach", "practice");
        g.addEdge("practice", "scoreAnalysis");
        g.addEdge("scoreAnalysis", "reviewSchedule");

        g.setStartNode("abilityQuery");
        g.setEndNode("reviewSchedule");
        return g;
    }

    private BaseNode createNode(NodeType type) {
        NodeConfig config = new NodeConfig();
        config.setNodeType(type);
        config.setNodeName(type.getName());
        return nodeFactory.createNode(config);
    }
}

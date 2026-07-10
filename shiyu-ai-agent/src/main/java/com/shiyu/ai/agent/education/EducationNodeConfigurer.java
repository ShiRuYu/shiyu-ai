package com.shiyu.ai.agent.education;

import com.shiyu.ai.agent.education.graph.*;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeFactory;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.education.service.AbilityService;
import com.shiyu.ai.education.service.ReviewService;
import com.shiyu.ai.knowledge.path.LearningPathService;
import com.shiyu.ai.education.domain.ReviewScheduler;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
import com.shiyu.ai.knowledge.service.KnowledgeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 教育域节点注册器
 *
 * 将教育图节点注册到 NodeFactory，使现有 Agent 引擎
 * （AgentController + AgentLoader + NodeFactory）能通过
 * graph_config JSON 动态创建教育节点并执行。
 *
 * 注册后，通过 DB 中 agent_def 的 graph_config 配置即可
 * 使用 POST /api/agent/{agentId}/execute 直接执行教育流程，
 * 无需额外的 Controller / Service 包装。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EducationNodeConfigurer {

    private final NodeFactory nodeFactory;
    private final ApplicationContext ctx;

    @PostConstruct
    public void registerEducationNodes() {
        log.info("EducationNodeConfigurer: 注册教育域节点到 NodeFactory");

        // 1. AbilityQueryNode — 查能力值
        nodeFactory.registerNodeType(NodeType.ABILITY_QUERY, NodeConfig.class, config -> {
            AbilityQueryNode node = new AbilityQueryNode(
                    ctx.getBean(KnowledgeService.class),
                    ctx.getBean(KnowledgeRelationService.class),
                    ctx.getBean(AbilityService.class));
            node.setConfig(config);
            return node;
        });

        // 2. EducationTeach — 教学讲解
        nodeFactory.registerNodeType(NodeType.EDUCATION_TEACH, NodeConfig.class, config -> {
            TeachNode node = new TeachNode(ctx.getBean(ChatEngine.class));
            node.setConfig(config);
            return node;
        });

        // 3. EducationPractice — 智能出题
        nodeFactory.registerNodeType(NodeType.EDUCATION_PRACTICE, NodeConfig.class, config -> {
            PracticeNode node = new PracticeNode(ctx.getBean(ChatEngine.class));
            node.setConfig(config);
            return node;
        });

        // 4. ScoreAnalysis — 评分分析
        nodeFactory.registerNodeType(NodeType.SCORE_ANALYSIS, NodeConfig.class, config -> {
            ScoreAnalysisNode node = new ScoreAnalysisNode(ctx.getBean(AbilityService.class));
            node.setConfig(config);
            return node;
        });

        // 5. ReviewSchedule — 复习安排
        nodeFactory.registerNodeType(NodeType.REVIEW_SCHEDULE, NodeConfig.class, config -> {
            ReviewScheduleNode node = new ReviewScheduleNode(
                    ctx.getBean(ReviewScheduler.class),
                    ctx.getBean(ReviewService.class));
            node.setConfig(config);
            return node;
        });

        // 6. PrereqCheck — 前置知识检查
        nodeFactory.registerNodeType(NodeType.PREREQ_CHECK, NodeConfig.class, config -> {
            PrereqCheckNode node = new PrereqCheckNode(
                    ctx.getBean(KnowledgeService.class),
                    ctx.getBean(KnowledgeRelationService.class),
                    ctx.getBean(LearningPathService.class));
            node.setConfig(config);
            return node;
        });

        log.info("EducationNodeConfigurer: 6 个教育域节点注册完成");
    }
}

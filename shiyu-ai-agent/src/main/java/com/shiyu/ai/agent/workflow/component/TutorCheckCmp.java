package com.shiyu.ai.agent.workflow.component;

import com.shiyu.ai.agent.graph.Graph;
import com.shiyu.ai.agent.graph.StateGraphBuilder;
import com.shiyu.ai.agent.education.graph.AbilityQueryNode;
import com.shiyu.ai.agent.education.graph.PrereqCheckNode;
import com.shiyu.ai.agent.education.graph.ScoreAnalysisNode;
import com.shiyu.ai.agent.education.graph.TeachNode;
import com.shiyu.ai.agent.education.graph.PracticeNode;
import com.shiyu.ai.agent.education.graph.ReviewScheduleNode;
import com.shiyu.ai.education.service.AbilityService;
import com.shiyu.ai.education.service.ReviewService;
import com.shiyu.ai.knowledge.path.LearningPathService;
import com.shiyu.ai.education.domain.ReviewScheduler;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
import com.shiyu.ai.knowledge.service.KnowledgeService;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.agent.workflow.context.TutorContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashMap;

@Slf4j
@Component("tutorCheckCmp")
@RequiredArgsConstructor
public class TutorCheckCmp extends NodeComponent {
    private final KnowledgeService knowledgeService;
    private final KnowledgeRelationService knowledgeRelationService;
    private final AbilityService abilityService;
    private final LearningPathService learningPathService;
    private final ChatEngine chatEngine;
    private final ReviewScheduler reviewScheduler;
    private final ReviewService reviewService;

    @Override
    public void process() throws Exception {
        TutorContext ctx = this.getContextBean(TutorContext.class);
        log.info("TutorCheckCmp: 执行辅导, studentId={}, knowledgeId={}",
                ctx.getStudentId(), ctx.getKnowledgeId());

        // Build tutor graph inline
        Graph g = new Graph(); g.setName("tutorGraph");
        g.addNode("abilityQuery", new AbilityQueryNode(knowledgeService, knowledgeRelationService, abilityService));
        g.addNode("teach", new TeachNode(chatEngine));
        g.addNode("practice", new PracticeNode(chatEngine));
        g.addNode("scoreAnalysis", new ScoreAnalysisNode(abilityService));
        g.addNode("reviewSchedule", new ReviewScheduleNode(reviewScheduler, reviewService));
        g.addEdge("abilityQuery", "teach"); g.addEdge("teach", "practice");
        g.addEdge("practice", "scoreAnalysis");
        g.setStartNode("abilityQuery"); g.setEndNode("reviewSchedule");

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
}
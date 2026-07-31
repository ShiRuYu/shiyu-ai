package com.shiyu.ai.agent.workflow.component;

import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.point.KnowledgePointService;
import com.shiyu.ai.knowledge.path.KnowledgePathService;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
import com.shiyu.ai.agent.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 检查前置知识组件
 *
 * 检测学生是否具备学习目标知识点所需的前置知识。
 * 若有缺失，记录到上下文中供后续流程处理。
 */
@Slf4j
@Component("checkKnowledgeCmp")
@RequiredArgsConstructor
public class CheckKnowledgeCmp extends NodeComponent {

    private final KnowledgePointService knowledgePointService;
    private final KnowledgeRelationService knowledgeRelationService;
    private final KnowledgePathService knowledgePathService;

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("CheckKnowledgeCmp: 检查前置知识, studentId={}, knowledgeId={}",
                ctx.getStudentId(), ctx.getKnowledgeId());

        // 1. 获取知识点详情
        KnowledgeResponse knowledge = knowledgePointService.getResponse(ctx.getKnowledgeId());
        ctx.setKnowledge(knowledge);

        // 2. 获取前置知识点列表
        List<KnowledgeResponse> prerequisites = Collections.emptyList();
        try {
            prerequisites = knowledgeRelationService.getPrerequisites(ctx.getKnowledgeId());
            log.info("CheckKnowledgeCmp: 获取到 {} 个前置知识点", prerequisites.size());
        } catch (Exception e) {
            log.warn("CheckKnowledgeCmp: 获取前置知识失败", e);
        }
        ctx.setPrerequisites(prerequisites);

        // 3. 检测缺失的前置知识
        Set<Long> masteredIds = Collections.emptySet(); // 后续可从学生记录中获取已掌握的知识点
        try {
            List<Long> missing = knowledgePathService.findMissingPrerequisites(
                    ctx.getKnowledgeId(), masteredIds);
            ctx.setMissingPrerequisiteIds(missing);
            if (!missing.isEmpty()) {
                log.info("CheckKnowledgeCmp: 缺失前置知识 {} 个: {}", missing.size(), missing);
            }
        } catch (Exception e) {
            log.warn("CheckKnowledgeCmp: 检测缺失前置知识失败", e);
        }
    }
}

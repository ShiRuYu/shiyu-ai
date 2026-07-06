package com.shiyu.ai.workflow.component;

import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.path.LearningPathService;
import com.shiyu.ai.knowledge.service.KnowledgeService;
import com.shiyu.ai.workflow.context.LearningContext;
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

    private final KnowledgeService knowledgeService;
    private final LearningPathService learningPathService;

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("CheckKnowledgeCmp: 检查前置知识, studentId={}, knowledgeId={}",
                ctx.getStudentId(), ctx.getKnowledgeId());

        // 1. 获取知识点详情
        KnowledgeResponse knowledge = knowledgeService.getById(ctx.getKnowledgeId());
        ctx.setKnowledge(knowledge);

        // 2. 获取前置知识点列表
        List<KnowledgeResponse> prerequisites = Collections.emptyList();
        try {
            // 通过 KnowledgeRelationService 获取前置知识点（简化处理）
            // 标准做法依赖 GraphStore，这里通过 KnowledgeService 的 Graph 获取
        } catch (Exception e) {
            log.warn("CheckKnowledgeCmp: 获取前置知识失败", e);
        }
        ctx.setPrerequisites(prerequisites);

        // 3. 检测缺失的前置知识
        Set<Long> masteredIds = Collections.emptySet(); // 后续可从学生记录中获取已掌握的知识点
        try {
            List<Long> missing = learningPathService.findMissingPrerequisites(
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

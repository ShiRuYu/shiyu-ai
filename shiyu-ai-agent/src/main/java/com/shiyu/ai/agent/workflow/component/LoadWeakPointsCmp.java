package com.shiyu.ai.agent.workflow.component;

import com.shiyu.ai.education.service.RecommendationService;
import com.shiyu.ai.agent.workflow.context.RecommendContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 加载薄弱知识点 — 使用增强后的 RecommendationService
 * <p>
 * 输入: RecommendContext.studentId
 * 输出: RecommendContext.weakKnowledgeIds
 */
@Slf4j
@Component("loadWeakPointsCmp")
@RequiredArgsConstructor
public class LoadWeakPointsCmp extends NodeComponent {

    private final RecommendationService recommendationService;

    @Override
    public void process() {
        RecommendContext ctx = this.getContextBean(RecommendContext.class);
        List<Long> weakIds = recommendationService.getWeakKnowledgeIds(ctx.getStudentId());
        ctx.setWeakKnowledgeIds(weakIds);
        log.info("薄弱知识点: {} 个", weakIds.size());
    }
}

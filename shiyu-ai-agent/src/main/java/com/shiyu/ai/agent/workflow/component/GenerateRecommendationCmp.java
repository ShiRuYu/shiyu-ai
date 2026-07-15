package com.shiyu.ai.agent.workflow.component;

import com.shiyu.ai.education.dto.HybridRecommendResponse;
import com.shiyu.ai.education.dto.KnowledgeRecommendResponse;
import com.shiyu.ai.education.service.RecommendationService;
import com.shiyu.ai.agent.workflow.context.RecommendContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 生成推荐建议 — 使用增强后的 RecommendationService
 * <p>
 * 输入: RecommendContext.studentId + weakKnowledgeIds
 * 输出: RecommendContext.recommendation (综合建议文本)
 *       RecommendContext.recommendType (推荐类型)
 */
@Slf4j
@Component("generateRecommendationCmp")
@RequiredArgsConstructor
public class GenerateRecommendationCmp extends NodeComponent {

    private final RecommendationService recommendationService;

    @Override
    public void process() {
        RecommendContext ctx = this.getContextBean(RecommendContext.class);
        Long studentId = ctx.getStudentId();

        if (ctx.getWeakKnowledgeIds().isEmpty()) {
            ctx.setRecommendation("暂无薄弱知识点，继续保持当前学习节奏！建议定期复习巩固已学知识点。");
            ctx.setRecommendType("ALL_CLEAR");
            return;
        }

        // 生成混合推荐摘要
        HybridRecommendResponse hybrid = recommendationService.hybridRecommend(studentId, null);

        StringBuilder sb = new StringBuilder();
        sb.append("📚 今日学习推荐\n\n");

        // 知识点推荐摘要
        List<KnowledgeRecommendResponse> kTopics = hybrid.knowledgeTop();
        if (!kTopics.isEmpty()) {
            sb.append("【薄弱知识点】共 ").append(kTopics.size()).append(" 个\n");
            for (int i = 0; i < Math.min(kTopics.size(), 3); i++) {
                var k = kTopics.get(i);
                sb.append("  ").append(i + 1).append(". ");
                sb.append(k.knowledgeName() != null ? k.knowledgeName() : "知识点#" + k.knowledgeId());
                sb.append(" 掌握度 ").append(String.format("%.0f%%", k.mastery()));
                sb.append("\n");
            }
        }

        int questionCount = hybrid.questionTop().size();
        int resourceCount = hybrid.resourceTop().size();
        int reviewCount = hybrid.reviewTop().size();

        sb.append("\n【推荐统计】\n");
        sb.append("  📝 推荐题目 ").append(questionCount).append(" 道\n");
        sb.append("  📖 推荐资源 ").append(resourceCount).append(" 个\n");
        if (reviewCount > 0) {
            sb.append("  🔄 到期复习 ").append(reviewCount).append(" 项\n");
        }

        if (hybrid.overallAdvice() != null) {
            sb.append("\n【综合建议】\n").append(hybrid.overallAdvice());
        }

        ctx.setRecommendation(sb.toString());
        ctx.setRecommendType("HYBRID");
        log.info("推荐建议已生成: 知识点={}, 题目={}, 资源={}, 复习={}",
                kTopics.size(), questionCount, resourceCount, reviewCount);
    }
}

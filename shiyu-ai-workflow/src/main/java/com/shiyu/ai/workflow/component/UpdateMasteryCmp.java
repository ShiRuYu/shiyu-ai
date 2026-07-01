package com.shiyu.ai.workflow.component;

import com.shiyu.ai.education.ability.AbilityService;
import com.shiyu.ai.education.domain.AbilityValue;
import com.shiyu.ai.education.domain.BloomTaxonomy;
import com.shiyu.ai.education.review.ReviewScheduler;
import com.shiyu.ai.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * 更新掌握度组件
 *
 * 根据本次学习结果更新学生的 Bloom 能力值，
 * 并安排艾宾浩斯复习任务。
 */
@Slf4j
@Component("updateMasteryCmp")
@RequiredArgsConstructor
public class UpdateMasteryCmp extends NodeComponent {

    private final AbilityService abilityService;
    private final ReviewScheduler reviewScheduler;

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("UpdateMasteryCmp: 更新能力值, studentId={}, knowledgeId={}",
                ctx.getStudentId(), ctx.getKnowledgeId());

        // 1. 获取学习前能力值
        AbilityValue beforeAbility = abilityService.get(
                ctx.getStudentId(), ctx.getKnowledgeId());
        ctx.setBeforeAbility(beforeAbility);

        // 2. 根据练习得分更新能力值
        double accuracy = ctx.getPracticeAccuracy() != null ? ctx.getPracticeAccuracy() : 0.0;
        // 更新 "应用" 维度
        abilityService.update(ctx.getStudentId(), ctx.getKnowledgeId(),
                BloomTaxonomy.APPLY, accuracy);
        // 同时更新记忆维度
        abilityService.update(ctx.getStudentId(), ctx.getKnowledgeId(),
                BloomTaxonomy.REMEMBER, Math.min(accuracy + 0.2, 1.0));

        // 3. 获取更新后的能力值
        AbilityValue afterAbility = abilityService.get(
                ctx.getStudentId(), ctx.getKnowledgeId());
        ctx.setAfterAbility(afterAbility);

        // 4. 安排艾宾浩斯复习任务
        List<ReviewScheduler.ReviewTask> reviewTasks = reviewScheduler.scheduleAfterLearning(
                ctx.getStudentId(), ctx.getKnowledgeId(), Instant.now());
        ctx.setReviewDates(reviewTasks.stream()
                .map(ReviewScheduler.ReviewTask::reviewDate)
                .toList());

        log.info("UpdateMasteryCmp: 能力值更新完成, overall={} -> {}",
                String.format("%.1f", beforeAbility.overallScore()),
                String.format("%.1f", afterAbility.overallScore()));
        log.info("UpdateMasteryCmp: 安排了 {} 轮复习", reviewTasks.size());
    }
}

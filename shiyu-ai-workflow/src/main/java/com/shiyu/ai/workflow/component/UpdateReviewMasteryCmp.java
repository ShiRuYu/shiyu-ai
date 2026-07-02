package com.shiyu.ai.workflow.component;

import com.shiyu.ai.education.ability.AbilityService;
import com.shiyu.ai.education.domain.BloomTaxonomy;
import com.shiyu.ai.education.review.ReviewService;
import com.shiyu.ai.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 更新复习掌握度组件
 */
@Slf4j
@Component("updateReviewMasteryCmp")
@RequiredArgsConstructor
public class UpdateReviewMasteryCmp extends NodeComponent {

    private final AbilityService abilityService;
    private final ReviewService reviewService;

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("UpdateReviewMasteryCmp: 更新复习掌握度, studentId={}", ctx.getStudentId());

        if (ctx.getStudentId() != null && ctx.getKnowledgeId() != null) {
            double accuracy = ctx.getPracticeAccuracy() != null ? ctx.getPracticeAccuracy() : 0.75;
            abilityService.update(ctx.getStudentId(), ctx.getKnowledgeId(), BloomTaxonomy.REMEMBER, accuracy);
            log.info("复习掌握度已更新: student={}, knowledge={}, accuracy={}",
                    ctx.getStudentId(), ctx.getKnowledgeId(), accuracy);
        }
    }
}

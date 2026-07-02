package com.shiyu.ai.workflow.component;

import com.shiyu.ai.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 复习评分组件
 */
@Slf4j
@Component("reviewScoreCmp")
@RequiredArgsConstructor
public class ReviewScoreCmp extends NodeComponent {

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("ReviewScoreCmp: 复习评分, studentId={}, knowledgeId={}",
                ctx.getStudentId(), ctx.getKnowledgeId());
        ctx.setPracticeScore(75.0);
        ctx.setPracticeAccuracy(0.75);
    }
}

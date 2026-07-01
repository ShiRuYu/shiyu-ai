package com.shiyu.ai.workflow.component;

import com.shiyu.ai.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("reviewScoreCmp")
public class ReviewScoreCmp extends NodeComponent {
    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("ReviewScoreCmp: 复习评分, studentId={}", ctx.getStudentId());
    }
}

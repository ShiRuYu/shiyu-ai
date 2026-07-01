package com.shiyu.ai.workflow.component;

import com.shiyu.ai.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("updateReviewMasteryCmp")
public class UpdateReviewMasteryCmp extends NodeComponent {
    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("UpdateReviewMasteryCmp: 更新复习掌握度, studentId={}", ctx.getStudentId());
    }
}
